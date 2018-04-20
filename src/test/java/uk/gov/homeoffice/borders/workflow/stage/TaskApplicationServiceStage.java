package uk.gov.homeoffice.borders.workflow.stage;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.integration.spring.JGivenStage;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.Assert;
import uk.gov.homeoffice.borders.workflow.identity.Team;
import uk.gov.homeoffice.borders.workflow.identity.User;
import uk.gov.homeoffice.borders.workflow.task.TaskApplicationService;

import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


@JGivenStage
public class TaskApplicationServiceStage extends Stage<TaskApplicationServiceStage> {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskApplicationService applicationService;

    @Autowired
    private RuntimeService runtimeService;

    private Page<Task> tasks;
    private ProcessInstance processInstance;
    private TaskApiControllerStage.Data data;
    private Task task;

    public TaskApplicationServiceStage getTasksForUserIsRequested(String username) {
        Assert.notNull(task, "Task not initialised...call asTask()");
        User user = new User();
        user.setEmail(username);
        user.setEmail(username);
        Team team = new Team();
        team.setName("abc");
        team.setTeamCode("test-candidate");
        user.setTeam(team);
        tasks = applicationService.tasks(user, new PageRequest(0, 10));
        return this;
    }

    public TaskApplicationServiceStage getTaskForCandidateGroups(String candidateGroup) {
        Assert.notNull(task, "Task not initialised...call asTask()");
        User user = new User();
        user.setEmail(UUID.randomUUID().toString());
        Team group = new Team();
        group.setId(candidateGroup);
        group.setTeamCode(candidateGroup);
        group.setName(candidateGroup);
        user.setTeam(group);
        tasks = applicationService.tasks(user, new PageRequest(0, 10));
        return this;
    }


    public TaskApplicationServiceStage numberOfTasksShouldBe(long numberOfTasks) {
        assertThat(tasks.getTotalElements(), is(numberOfTasks));
        return this;
    }

    public TaskApplicationServiceStage assignedTasksUsernameIs(String username) {
        List<Task> tasks = this.
                tasks.getContent()
                .stream()
                .filter(t -> t.getAssignee().
                        equalsIgnoreCase(username))
                .collect(Collectors.toList());

        assertTrue(tasks.size() >= 1);
        return this;
    }

    public TaskApplicationServiceStage aTask() {
        data = new TaskApiControllerStage.Data();
        data.setName("test-task");
        data.setDescription("test-description");
        data.setCandidateGroup("test-candidate");
        return this;
    }

    public TaskApplicationServiceStage withUsername(String username) {
        Assert.notNull(data, "Data is null...please initialise with aTask()");
        data.setAssignee(username);
        return this;
    }


    public TaskApplicationServiceStage isCreated() {
        Assert.notNull(data, "Task is null...please initialise with aTask()");

        List<TaskApiControllerStage.Data> collectionOfData = Collections.singletonList(this.data);

        ObjectValue dataObjectValue =
                Variables.objectValue(collectionOfData)
                        .serializationDataFormat("application/json")
                        .create();

        Map<String,Object> variables = new HashMap<>();
        variables.put("collectionOfData", dataObjectValue);
        variables.put("type" , "non-notification");

        processInstance = runtimeService.startProcessInstanceByKey("test",
                variables);

        task = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).singleResult();
        return this;
    }

    public TaskApplicationServiceStage withCandidateGroup(String candidateGroup) {
        Assert.notNull(data, "Data is null...please initialise with aTask()");
        data.setCandidateGroup(candidateGroup);
        return this;
    }


    public TaskApplicationServiceStage numberOfPages(int numberOfPages) {
        assertThat(tasks.getTotalPages(), is(numberOfPages));
        return this;
    }

    public TaskApplicationServiceStage totalResultsIs(long totalResults) {
        assertThat(tasks.getTotalElements(), is(totalResults));
        return this;
    }
}
