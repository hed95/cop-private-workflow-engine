package uk.gov.homeoffice.borders.workflow.task

import org.camunda.bpm.engine.HistoryService
import org.camunda.bpm.engine.rest.dto.VariableValueDto
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto
import org.camunda.bpm.engine.task.Task
import org.camunda.bpm.engine.variable.Variables
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import uk.gov.homeoffice.borders.workflow.BaseSpec

import static org.hamcrest.Matchers.greaterThanOrEqualTo
import static org.hamcrest.Matchers.is
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class TaskApiControllerSpec extends BaseSpec {

    def processInstance

    @Autowired
    HistoryService historyService

    void createTasks(number, assignee) {
        def tasks = []
        number.times {
            def data = new Data()
            data.assignee = assignee
            data.candidateGroup = "teamA"
            data.name = "test ${it}"
            data.description = "test ${it}"
            tasks << data
        }

        def objectValue =
                Variables.objectValue(tasks)
                        .serializationDataFormat("application/json")
                        .create()

        def variables = [:]
        variables['collectionOfData'] = objectValue
        variables['type'] = 'non-notification'
        processInstance = runtimeService.startProcessInstanceByKey("test",
                variables)
    }


    def 'can get paged results'() {
        given:
        createTasks(30, "test")
        and:
        logInUser()

        when:
        def result = mvc.perform(get("/api/workflow/tasks")
                .contentType(MediaType.APPLICATION_JSON))


        then:
        result.andExpect(status().is2xxSuccessful())


    }




    def 'can query task by name'() {
        given:
        createTasks(1, "test")

        and:
        logInUser()

        and:
        def taskQueryDto = new TaskQueryDto()
        taskQueryDto.setName("Perform duty for test 0")

        when:
        def result = mvc.perform(post("/api/workflow/tasks")
                .content(objectMapper.writeValueAsString(taskQueryDto))
                .contentType(MediaType.APPLICATION_JSON))


        then:
        result.andExpect(status().is2xxSuccessful())
        result.andExpect(jsonPath('$.page["totalElements"]', greaterThanOrEqualTo(1)))
        result.andExpect(jsonPath('$._embedded.tasks.length()', is(1)))
    }

    def 'can get task count'() {
        given:
        createTasks(30, "test")
        and:
        logInUser()

        when:
        def result = mvc.perform(get("/api/workflow/tasks/_task-counts")
                .contentType(MediaType.APPLICATION_JSON))

        then:
        result.andExpect(status().is2xxSuccessful())
        def taskCountDto = result.andReturn().asyncResult
        taskCountDto.tasksAssignedToUser == 30
        taskCountDto.tasksUnassigned == 0
        taskCountDto.totalTasksAllocatedToTeam == 30
    }


    def 'can get task'() {
        given:
        createTasks(1, "test")
        and:
        logInUser()

        when:
        List<Task> list = taskService.createTaskQuery()
                .processInstanceId(processInstance.getProcessInstanceId()).list()
        Task task = list.first()
        def result = mvc.perform(get("/api/workflow/tasks/" + task.getId()).contentType(MediaType.APPLICATION_JSON))

        then:
        result.andExpect(status().is2xxSuccessful())
        def taskLoaded = result.andReturn().asyncResult
        taskLoaded
        !taskLoaded.candidateGroups.isEmpty()
    }


    def 'can claim a task'() {
        given:
        createTasks(1, null)
        and:
        def user = logInUser()
        and:
        List<Task> list = taskService.createTaskQuery()
                .processInstanceId(processInstance.getProcessInstanceId()).list()
        def task = list.first()

        when:
        def result = mvc.perform(post("/api/workflow/tasks/${task.id}/_claim")
                .contentType(MediaType.APPLICATION_JSON))

        then:
        result.andExpect(status().is2xxSuccessful())
        and:
        def reloded = taskService.createTaskQuery().taskId(task.id).singleResult()
        reloded.assignee == user.email

    }

    def 'can claim and complete task'() {
        given:
        createTasks(1, null)
        and:
        def user = logInUser()
        and:
        List<Task> list = taskService.createTaskQuery()
                .processInstanceId(processInstance.getProcessInstanceId()).list()
        def task = list.first()

        and:
        mvc.perform(post("/api/workflow/tasks/${task.id}/_claim")
                .contentType(MediaType.APPLICATION_JSON))

        when:
        def result = mvc.perform(post("/api/workflow/tasks/${task.id}/_complete")
                .contentType(MediaType.APPLICATION_JSON))

        then:
        result.andExpect(status().is2xxSuccessful())
        and:
        def reloded = historyService.createHistoricTaskInstanceQuery()
                .taskId(task.id).singleResult()

        reloded.assignee == user.email
        reloded.endTime
    }

    def 'can claim and unclaim task'() {
        given:
        createTasks(1, null)
        and:
        logInUser()
        and:
        List<Task> list = taskService.createTaskQuery()
                .processInstanceId(processInstance.getProcessInstanceId()).list()
        def task = list.first()


        and:
        mvc.perform(post("/api/workflow/tasks/${task.id}/_claim")
                .contentType(MediaType.APPLICATION_JSON))
        list = taskService.createTaskQuery()
                .processInstanceId(processInstance.getProcessInstanceId()).list()
        task = list.first()
        task.assignee == 'test'

        when:
        def result = mvc.perform(post("/api/workflow/tasks/${task.id}/_unclaim")
                .contentType(MediaType.APPLICATION_JSON))

        then:
        result.andExpect(status().is2xxSuccessful())
        and:
        def reloded = taskService.createTaskQuery().taskId(task.id).singleResult()
        reloded.assignee == null
    }

    def 'can complete task with form'() {
        given:
        createTasks(1, null)
        and:
        def user = logInUser()
        and:
        List<Task> list = taskService.createTaskQuery()
                .processInstanceId(processInstance.getProcessInstanceId()).list()
        def task = list.first()

        and:
        mvc.perform(post("/api/workflow/tasks/${task.id}/_claim")
                .contentType(MediaType.APPLICATION_JSON))

        def formData = new CompleteTaskDto()
        formData.variables = [:]
        def variable = new VariableValueDto()
        variable.type = 'string'
        variable.value = 'asStringValue'
        formData.variables['formData'] = variable
        def  json = objectMapper.writeValueAsString(formData)
        json.inspect()

        when:
        def result = mvc.perform(post("/api/workflow/tasks/${task.id}/form/_complete")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
        and:
        def reloded = historyService.createHistoricTaskInstanceQuery()
                .taskId(task.id).singleResult()
        and:
        def formDataVariable = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(task.processInstanceId)
                .variableName('formData').singleResult()

        then:
        result.andExpect(status().is2xxSuccessful())

        and:
        reloded.assignee == user.email
        reloded.endTime
        formDataVariable
        formDataVariable.name == 'formData'
        formDataVariable.value == variable.value
    }


}