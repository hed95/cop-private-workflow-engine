package uk.gov.homeoffice.borders.workflow.task

import io.vavr.Tuple2
import org.camunda.bpm.engine.HistoryService
import org.camunda.bpm.engine.IdentityService
import org.camunda.bpm.engine.rest.dto.VariableValueDto
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.task.Task
import org.camunda.spin.Spin
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import uk.gov.homeoffice.borders.workflow.BaseSpec
import uk.gov.homeoffice.borders.workflow.identity.PlatformUser
import uk.gov.homeoffice.borders.workflow.identity.Team
import uk.gov.homeoffice.borders.workflow.process.ProcessApplicationService
import uk.gov.homeoffice.borders.workflow.process.ProcessStartDto
import uk.gov.homeoffice.borders.workflow.security.WorkflowAuthentication

import javax.crypto.SealedObject

class TaskApplicationServiceSpec extends BaseSpec {
    def processInstance

    @Autowired
    IdentityService identityService
    @Autowired
    HistoryService historyService
    @Autowired
    TaskApplicationService applicationService
    @Autowired
    ProcessApplicationService processApplicationService



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
                Spin.S(tasks, "application/json")

        def variables = new HashMap<String, Object>()
        variables['collectionOfData'] = objectValue
        variables['type'] = 'non-notification'
        processInstance = runtimeService.startProcessInstanceByKey("test",
                variables)
    }

    def setup() {
        runtimeService.createProcessInstanceQuery()
                .list().each { it -> runtimeService.deleteProcessInstance(it.id, 'deleted') }

        createTasks(1, 'assigneeOneTwoThree')
    }


    def 'can get tasks for assignee'() {
        given:
        def user = new PlatformUser()
        user.id = 'assigneeOneTwoThree'
        user.email = 'assigneeOneTwoThree'

        def shift = new PlatformUser.ShiftDetails()
        shift.roles = ['custom_role']
        user.shiftDetails = shift

        def team = new Team()
        user.teams = []
        team.code = 'teamA'
        user.teams << team
        user.roles = ['custom_role']
        identityService.getCurrentAuthentication() >> new WorkflowAuthentication(user)
        user


        when:
        def result = applicationService.tasks(user, null, PageRequest.of(0, 20))

        then:
        result.totalElements == 1
        result.first().assignee == 'assigneeOneTwoThree'
    }

    def 'can get tasks for assignee criteria'() {
        given:
        def user = new PlatformUser()
        user.id = 'assigneeOneTwoThree'
        user.email = 'assigneeOneTwoThree'

        def shift = new PlatformUser.ShiftDetails()
        shift.roles = ['custom_role']
        user.shiftDetails = shift

        def team = new Team()
        user.teams = []
        team.code = 'teamA'
        user.teams << team
        user.roles = ['custom_role']
        identityService.getCurrentAuthentication() >> new WorkflowAuthentication(user)
        user

        and:
        def taskCriteria = new TaskCriteria()
        taskCriteria.assignedToMeOnly = true

        when:
        def result = applicationService.tasks(user, taskCriteria, PageRequest.of(0, 20))

        then:
        result.totalElements == 1
        result.first().assignee == 'assigneeOneTwoThree'
    }

    def 'can get tasks for candidate user'() {
        given:
        def tasks = []
        1.times {
            def data = new Data()
            data.candidateUser = 'candidateUserABC'
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
        processInstance = runtimeService.startProcessInstanceByKey("testCandidateUser",
                variables)
        and:
        def taskCriteria = new TaskCriteria()
        taskCriteria.assignedToMeOnly = true

        and:
        def user = new PlatformUser()
        user.id = 'candidateUserABC'
        user.email = 'candidateUserABC'

        def shift = new PlatformUser.ShiftDetails()
        shift.roles = ['custom_role']
        user.shiftDetails = shift

        def team = new Team()
        user.teams = []
        team.code = 'teamA'
        user.teams << team
        user.roles = ['custom_role']
        identityService.getCurrentAuthentication() >> new WorkflowAuthentication(user)
        user

        when:
        def result = applicationService.tasks(user, taskCriteria, PageRequest.of(0, 20))

        then:
        result.totalElements == 1

    }


    def 'complete task variables encrypted if process has encryption flag'() {
        given:
        def processStartDto = new ProcessStartDto()
        processStartDto.processKey = 'encryption'
        processStartDto.variableName = 'collectionOfData'
        def data = new Data()
        data.candidateGroup = "teamA"
        data.name = "test 0"
        data.description = "test 0"
        processStartDto.data = [data]
        processStartDto

        and:
        def user = new PlatformUser()
        user.id = 'assigneeOneTwoThree'
        user.email = 'assigneeOneTwoThree'

        def shift = new PlatformUser.ShiftDetails()
        shift.roles = ['custom_role']
        user.shiftDetails = shift

        def team = new Team()
        user.teams = []
        team.code = 'teamA'
        user.teams << team
        user.roles = ['custom_role']
        identityService.getCurrentAuthentication() >> new WorkflowAuthentication(user)
        user

        and:
        Tuple2<ProcessInstance, List<Task>> response = processApplicationService.createInstance(processStartDto, user)


        def processInstance = response._1().id
        when:
        def task = taskService.createTaskQuery()
                .processInstanceId(processInstance).singleResult()
        def completeTaskDto = new TaskCompleteDto()
        completeTaskDto.variableName = 'myTaskVariable'
        def taskData = new Data()
        taskData.candidateGroup = "taskDataA"
        taskData.name = "taskDataName"
        taskData.description = "taskDataDesc"
        completeTaskDto.data = taskData
        applicationService.completeTask(task.id,completeTaskDto)


        and:
        def variables = processApplicationService
                .variables(processInstance, user)

        then:
        !(variables.get('myTaskVariable') instanceof SealedObject)

    }

    def 'can complete with form for encrypted process'() {
        given:
        def processStartDto = new ProcessStartDto()
        processStartDto.processKey = 'encryption'
        processStartDto.variableName = 'collectionOfData'
        def data = new Data()
        data.candidateGroup = "teamA"
        data.name = "test 0"
        data.description = "test 0"
        processStartDto.data = [data]
        processStartDto

        and:
        def user = new PlatformUser()
        user.id = 'assigneeOneTwoThree'
        user.email = 'assigneeOneTwoThree'

        def shift = new PlatformUser.ShiftDetails()
        shift.roles = ['custom_role']
        user.shiftDetails = shift

        def team = new Team()
        user.teams = []
        team.code = 'teamA'
        user.teams << team
        user.roles = ['custom_role']
        identityService.getCurrentAuthentication() >> new WorkflowAuthentication(user)
        user

        and:
        Tuple2<ProcessInstance, List<Task>> response = processApplicationService.createInstance(processStartDto, user)


        def processInstanceId = response._1().id
        when:
        def task = taskService.createTaskQuery()
                .processInstanceId(processInstanceId).singleResult()
        taskService.setAssignee(task.id, user.email)

        def completeTaskDto = new CompleteTaskDto()
        completeTaskDto.variables = new HashMap<String,VariableValueDto>()

        def taskData = new Data()
        taskData.candidateGroup = "taskDataA"
        taskData.name = "taskDataName"
        taskData.description = "taskDataDesc"
        def dto = new VariableValueDto()
        dto.value = data
        completeTaskDto.variables.put('myTaskVariable', dto)
        applicationService.completeTaskWithForm(user, task.id,completeTaskDto)


        and:
        def variables = processApplicationService
                .variables(processInstanceId, user)

        then:
        !(variables.get('myTaskVariable') instanceof SealedObject)

    }
}
