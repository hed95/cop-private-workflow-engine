package uk.gov.homeoffice.borders.workflow.event

import org.camunda.bpm.engine.runtime.ProcessInstance
import spock.lang.Specification

class FormToS3PutRequestGeneratorSpec extends Specification {

    def formToS3DocGenerator = new FormToS3PutRequestGenerator()

    def 'can generate request'() {
        given: 'a form'
        def form = '''{
                       "testForm": {
                            "submit": true,
                            "test": "apples",
                            "shiftDetailsContext" : {
                               "email": "email"
                            },
                            "form": {
                               "name": "testForm",
                               "formVersionId": "versionId",
                               "submittedBy": "test",
                               "submissionDate": "20200120T12:12:00",
                               "title": "test",
                               "process": {
                                  
                               }
                            }
                        }
                      }'''
        and: 'process instance'
        ProcessInstance processInstance = Mock()
        processInstance.getId() >> "processInstance"
        processInstance.getProcessDefinitionId() >> "processdefinitionid"
        processInstance.getBusinessKey() >> "businessKey"

        when: 'request is made'
        def request = formToS3DocGenerator.request(form, processInstance, "test")

        then: 'request is not null'
        request
        request.getMetadata()
        request.getMetadata().getUserMetaDataOf("processinstanceid") == 'processInstance'
        request.getMetadata().getUserMetaDataOf("processdefinitionid") == 'processdefinitionid'
        request.getMetadata().getUserMetaDataOf("formversionid") == 'versionId'
        request.getMetadata().getUserMetaDataOf("name") == 'testForm'
        request.getMetadata().getUserMetaDataOf("submittedby") == 'email'
        request.getMetadata().getUserMetaDataOf("submissiondate") == '20200120T12:12:00'
        request.getMetadata().getUserMetaDataOf("title") == 'test'
        request.getKey().contains('businessKey/testForm/email')
    }
}
