package uk.gov.homeoffice.borders.workflow.task

import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.TaskService
import org.camunda.spin.Spin
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.homeoffice.borders.workflow.BaseSpec

class TaskDtoResourceAssemblerSpec extends BaseSpec {

    def processInstance

    @Autowired
    TaskDtoResourceAssembler taskDtoResourceAssembler

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TaskService taskService

    def 'can get extension properties from user task'() {
        given:
        def tasks = []
        1.times {
            def data = new Data()
            data.assignee = 'assignee'
            data.candidateGroup = "teamA"
            data.name = "test ${it}"
            data.description = "test ${it}"
            tasks << data
        }

        def objectValue =
                Spin.S(tasks, "application/json")

        def variables = new HashMap<String, Object>()
        variables['collectionOfData'] = objectValue
        variables['type'] = 'non-notification'
        processInstance = runtimeService.startProcessInstanceByKey("test",
                variables)


        when:
        def task = taskService.createTaskQuery().processInstanceId(processInstance.id).list().first()
        def result = taskDtoResourceAssembler.toResource(task)

        then:
        System.out.println(objectMapper.writeValueAsString(result))
        result.extensionData.size() != 0

    }

    def 'no extension if user task does not have any'() {
        given:
        def tasks = []
        1.times {
            def data = new Data()
            data.assignee = 'assignee'
            data.candidateGroup = "teamA"
            data.name = "test ${it}"
            data.description = "test ${it}"
            tasks << data
        }

        def objectValue =
                Spin.S(tasks, "application/json")

        def variables = new HashMap<String, Object>()
        variables['collectionOfData'] = objectValue
        variables['type'] = 'non-notification'
        processInstance = runtimeService.startProcessInstanceByKey("test-noextension",
                variables)


        when:
        def task = taskService.createTaskQuery().processInstanceId(processInstance.id).list().first()
        def result = taskDtoResourceAssembler.toResource(task)

        then:
        result.extensionData.size() == 0
    }
}
