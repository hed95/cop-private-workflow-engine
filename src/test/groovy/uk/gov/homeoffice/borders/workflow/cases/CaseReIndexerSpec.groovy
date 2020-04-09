package uk.gov.homeoffice.borders.workflow.cases

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import io.findify.s3mock.S3Mock
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.bulk.BulkResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import spock.util.concurrent.AsyncConditions
import uk.gov.homeoffice.borders.workflow.BaseSpec

import static com.github.tomakehurst.wiremock.client.WireMock.*

class CaseReIndexerSpec extends BaseSpec {
    @Autowired
    CaseReIndexer caseReIndexer

    @Autowired
    AmazonS3 amazonS3Client

    static S3Mock api = new S3Mock.Builder().withPort(8323).withInMemoryBackend().build()


    def setupSpec() {
        if (api != null) {
            api.start()
        }
    }

    def cleanupSpec() {
        if (api != null) {
            api.shutdown()
        }
    }

    def 'can reindex'() {
        def conds = new AsyncConditions()

        given: 'a s3 bucket'
        amazonS3Client.createBucket("test-events")

        and: 'data stored in the bucket'
        ObjectMetadata metadata = new ObjectMetadata()
        metadata.addUserMetadata('name', 'formNameA')
        metadata.addUserMetadata('title', 'formNameA')
        metadata.addUserMetadata('formversionid', 'formNameA')
        metadata.addUserMetadata('processdefinitionid', "test")
        metadata.addUserMetadata('processinstanceid', "test")

        amazonS3Client.putObject(new PutObjectRequest("test-events",
                "BF-20200120-124/eventAtBorder/20120101-xx@x.com.json",
                new ClassPathResource("data.json").getInputStream(), metadata))


        and: 'es set up'
        stubFor(post("/_bulk?timeout=1m")
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson('''
                                            {"index":{"_index":"20200120","_id":"BF-20200120-124/eventAtBorder/20120101-xx@x.com.json"}}
                                            {"textField":"API-A-20200120-17","textField2":"APPLES","submit":true,"lastName":"XXXXX-AAAA","businessKey":"businessKey","shiftDetailsContext":{"shifthours":1,"phone":"+44788756341","locationid":"1","enddatetime":"2020-11-22T16:42:05","teamid":"880bee2a-f381-4c81-be30-49e9340660f7","currentLocationName":null,"staffid":"112f6ed3-29e5-4517-a2be-1c1a09f21be3","email":"aminmc@gmail.com","shiftid":null,"startdatetime":"2019-11-20T16:46:05","shiftminutes":0,"roles":[]},"environmentContext":{"referenceDataUrl":"http://localhost:8000","workflowUrl":"http://localhost:8000","operationalDataUrl":"http://localhost:8000","privateUiUrl":"http://localhost:8001"},"form":{"formVersionId":"84a32079-8e8b-4042-91db-c75d1cc3933a","formId":"d8e0ea6d-e12d-4291-8938-b86db773527f","title":"l5vp40zwx1","name":"vloigdgx2mr","submissionDate":"2020-01-28T08:31:55.297Z","process":{"definitionId":"test:1:b51fe446-0aa1-11ea-b74f-0e03b1b23d15"}}}
                                            ''', true, true))

                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                       {
                                       "took": 30,
                                       "errors": false,
                                       "items": [
                                          {
                                             "index": {
                                                "_index": "test",
                                                "_type": "_doc",
                                                "_id": "1",
                                                "_version": 1,
                                                "result": "created",
                                                "_shards": {
                                                   "total": 2,
                                                   "successful": 1,
                                                   "failed": 0
                                                },
                                                "status": 201,
                                                "_seq_no" : 0,
                                                "_primary_term": 1
                                             }
                                          },
                                          {
                                             "delete": {
                                                "_index": "test",
                                                "_type": "_doc",
                                                "_id": "2",
                                                "_version": 1,
                                                "result": "not_found",
                                                "_shards": {
                                                   "total": 2,
                                                   "successful": 1,
                                                   "failed": 0
                                                },
                                                "status": 404,
                                                "_seq_no" : 1,
                                                "_primary_term" : 2
                                             }
                                          },
                                          {
                                             "create": {
                                                "_index": "test",
                                                "_type": "_doc",
                                                "_id": "3",
                                                "_version": 1,
                                                "result": "created",
                                                "_shards": {
                                                   "total": 2,
                                                   "successful": 1,
                                                   "failed": 0
                                                },
                                                "status": 201,
                                                "_seq_no" : 2,
                                                "_primary_term" : 3
                                             }
                                          },
                                          {
                                             "update": {
                                                "_index": "test",
                                                "_type": "_doc",
                                                "_id": "1",
                                                "_version": 2,
                                                "result": "updated",
                                                "_shards": {
                                                    "total": 2,
                                                    "successful": 1,
                                                    "failed": 0
                                                },
                                                "status": 200,
                                                "_seq_no" : 3,
                                                "_primary_term" : 4
                                             }
                                          }
                                       ]
                                    }
                                        """)))

        when: 'reindex is called'

        def listener = new ActionListener<BulkResponse>() {
            @Override
            void onResponse(BulkResponse bulkItemResponses) {
                conds.evaluate {
                    assert bulkItemResponses.getItems().length >=1
                }
            }

            @Override
            void onFailure(Exception e) {
                e.printStackTrace()
                conds.evaluate {
                   assert false
                }
            }
        }
        caseReIndexer.reindex("BF-20200120-124", Optional.of(listener))

        then:
        conds.await(20)
    }


}
