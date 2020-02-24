package uk.gov.homeoffice.borders.workflow.cases

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import io.findify.s3mock.S3Mock
import org.apache.commons.io.IOUtils
import org.camunda.spin.Spin
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import uk.gov.homeoffice.borders.workflow.BaseSpec
import uk.gov.homeoffice.borders.workflow.process.ProcessApplicationService
import uk.gov.homeoffice.borders.workflow.process.ProcessStartDto

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class CaseApiControllerSpec extends BaseSpec {

    @Autowired
    ProcessApplicationService applicationService

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

    def 'can query cases'() {
        given:
        def user = logInUser()
        def processStartDto = new ProcessStartDto()
        processStartDto.processKey = 'encryption'
        processStartDto.variableName = 'collectionOfData'
        processStartDto.setBusinessKey('BF-20200120-555')
        def data = new Data()
        data.candidateGroup = "teamA"
        data.name = "test 0"
        data.description = "test 0"
        processStartDto.data = [data]
        processStartDto

        and:
        applicationService.createInstance(processStartDto, user)._1()

        when:
        def result = mvc.perform(get("/api/workflow/cases?businessKeyQuery=BF-20200120%"))

        then:
        result.andReturn().response.contentAsString != ''


    }

    def 'can get submission data for form'() {
        given:
        amazonS3Client.createBucket("events")

        def user = logInUser()
        def processStartDto = new ProcessStartDto()
        processStartDto.processKey = 'encryption'
        processStartDto.variableName = 'collectionOfData'
        processStartDto.setBusinessKey('BF-20200120-555')
        def data = new Data()
        data.candidateGroup = "teamA"
        data.name = "test 0"
        data.description = "test 0"
        processStartDto.data = [data]
        processStartDto

        and:
        def processInstance = applicationService.createInstance(processStartDto, user)._1()
        def definition = processInstance.getProcessDefinitionId()

        ObjectMetadata metadata = new ObjectMetadata()
        metadata.addUserMetadata('name', 'formNameA')
        metadata.addUserMetadata('title', 'formNameA')
        metadata.addUserMetadata('formversionid', 'formNameA')
        metadata.addUserMetadata('processdefinitionid', definition)

        amazonS3Client.putObject(new PutObjectRequest("events", "BF-20200120-555/eventAtBorder/20120101-xx@x.com.json",
                new ClassPathResource("data.json").getInputStream(), metadata))


        metadata = new ObjectMetadata()
        metadata.addUserMetadata('name', 'formNameB')
        metadata.addUserMetadata('title', 'formNameB')
        metadata.addUserMetadata('formversionid', 'formNameB')
        metadata.addUserMetadata('processdefinitionid', definition)

        amazonS3Client.putObject(new PutObjectRequest("events", "BF-20200120-555/peopleEaB/20120101-xx@x.com.json",
                new ClassPathResource("data.json").getInputStream(), metadata))


        metadata = new ObjectMetadata()
        metadata.addUserMetadata('name', 'formNameC')
        metadata.addUserMetadata('title', 'formNameC')
        metadata.addUserMetadata('formversionid', 'formNameC')
        metadata.addUserMetadata('processdefinitionid', definition)
        amazonS3Client.putObject(new PutObjectRequest("events", "BF-20200120-555/itemsEaB/20120101-xx@x.com.json",
                new ClassPathResource("data.json").getInputStream(), metadata))


        metadata = new ObjectMetadata()
        metadata.addUserMetadata('name', 'formNameD')
        metadata.addUserMetadata('title', 'formNameD')
        metadata.addUserMetadata('formversionid', 'formNameD')
        metadata.addUserMetadata('processdefinitionid', definition)
        amazonS3Client.putObject(new PutObjectRequest("events", "BF-20200120-555/journeyEaB/20120101-xx@x.com.json",
                new ClassPathResource("data.json").getInputStream(), metadata))


        when:
        def result = mvc.perform(get("/api/workflow/cases/BF-20200120-555"))

        then:
        result.andExpect(status().is2xxSuccessful())
        def caseDetail = Spin.JSON(result.andReturn().response.contentAsString);

        caseDetail.prop('businessKey').stringValue() == "BF-20200120-555"

        when:
        def submissionData = mvc.perform(get("/api/workflow/cases/BF-20200120-555/submission?key=BF-20200120-555/journeyEaB/20120101-xx@x.com.json"))

        then:
        def asSpin = Spin.JSON(IOUtils.toString(new ClassPathResource("data.json").getInputStream(), "UTF-8"))
        submissionData.andReturn().response.contentAsString == asSpin.toString()

    }
}
