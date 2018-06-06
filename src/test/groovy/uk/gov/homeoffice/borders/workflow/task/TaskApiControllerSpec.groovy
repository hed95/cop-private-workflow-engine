package uk.gov.homeoffice.borders.workflow.task

import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto
import org.camunda.bpm.engine.task.Task
import org.camunda.bpm.engine.variable.Variables
import org.springframework.http.MediaType
import uk.gov.homeoffice.borders.workflow.BaseSpec
import uk.gov.homeoffice.borders.workflow.identity.Team
import uk.gov.homeoffice.borders.workflow.identity.User

import static org.hamcrest.Matchers.greaterThanOrEqualTo
import static org.hamcrest.Matchers.is
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class TaskApiControllerSpec extends BaseSpec {

    def processInstance

    void createTasks(number) {
        def tasks = []
        number.times {
            def data = new Data()
            data.assignee = "test"
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
        createTasks(30)
        and:
        def user = new User()
        user.email = 'email'
        def team = new Team()
        user.teams = []
        team.teamCode = 'teamA'
        user.teams << team
        restApiUserExtractor.toUser() >> user

        when:
        def result = mvc.perform(get("/api/workflow/tasks")
                .contentType(MediaType.APPLICATION_JSON))


        then:
        result.andExpect(status().is2xxSuccessful())


    }

    def 'can query task by name'() {
        given:
        createTasks(1)

        and:
        def user = new User()
        user.email = 'email'
        def team = new Team()
        user.teams = []
        team.teamCode = 'teamA'
        user.teams << team
        restApiUserExtractor.toUser() >> user


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
        createTasks(30)
        and:
        def user = new User()
        user.email = 'test'
        def team = new Team()
        user.teams = []
        team.teamCode = 'teamA'
        user.teams << team
        restApiUserExtractor.toUser() >> user

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
        createTasks(1)
        and:
        def user = new User()
        user.email = 'test'
        def team = new Team()
        user.teams = []
        team.teamCode = 'teamA'
        user.teams << team
        restApiUserExtractor.toUser() >> user

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


    @lombok.Data
    class Data {
        String assignee
        String candidateGroup
        String name
        String description

    }

}