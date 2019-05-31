package uk.gov.homeoffice.borders.workflow.task

import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.impl.persistence.entity.IdentityLinkEntity
import org.camunda.bpm.engine.task.Task
import spock.lang.Specification
import uk.gov.homeoffice.borders.workflow.exception.ForbiddenException
import uk.gov.homeoffice.borders.workflow.identity.PlatformUser
import uk.gov.homeoffice.borders.workflow.identity.Team

class TaskCheckerSpec extends Specification {

    TaskService taskService = Mock(TaskService)

    TaskChecker underTest = new TaskChecker(taskService)

    def 'no exception thrown if user allowed to see task based on group'() {
        given:
        def user = new PlatformUser()
        def team = new Team()
        team.id = 'teamId'
        team.code = 'teamCode'
        user.teams = [team]

        and:
        def task = Mock(Task)
        task.getId() >> 'taskId'

        and:
        def identityLink = IdentityLinkEntity.newIdentityLink()
        identityLink.taskId = task.id
        identityLink.groupId = 'teamCode'
        taskService.getIdentityLinksForTask(task.id) >> [identityLink]

        when:
        underTest.checkUserAuthorized(user, task)

        then:
        noExceptionThrown()

    }

    def 'no exception thrown if user allowed to see task based on assignee'() {
        given:
        def user = new PlatformUser()
        user.email = 'email'
        def team = new Team()
        team.id = 'teamId'
        team.code = 'teamCode'
        user.teams = [team]

        and:
        def task = Mock(Task)
        task.getId() >> 'taskId'
        task.getAssignee() >> user.email

        and:
        def identityLink = IdentityLinkEntity.newIdentityLink()
        identityLink.taskId = task.id
        identityLink.groupId = 'teamCodeA'
        taskService.getIdentityLinksForTask(task.id) >> [identityLink]

        when:
        underTest.checkUserAuthorized(user, task)

        then:
        noExceptionThrown()

    }

    def 'exception thrown if user is not assigned to task and not in team'() {
        given:
        def user = new PlatformUser()
        user.email = 'email'
        def team = new Team()
        team.id = 'teamId'
        team.code = 'teamCodeA'
        user.teams = [team]

        and:
        def task = Mock(Task)
        task.getId() >> 'taskId'
        task.getAssignee() >> null

        and:
        def identityLink = IdentityLinkEntity.newIdentityLink()
        identityLink.taskId = task.id
        identityLink.groupId = 'teamCode'
        taskService.getIdentityLinksForTask(task.id) >> [identityLink]

        when:
        underTest.checkUserAuthorized(user, task)

        then:
        thrown(ForbiddenException)

    }

}
