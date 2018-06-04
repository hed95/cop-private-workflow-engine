package uk.gov.homeoffice.borders.workflow.stage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.integration.spring.JGivenStage;
import org.assertj.core.api.Java6BDDAssertions;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.homeoffice.borders.workflow.identity.Team;
import uk.gov.homeoffice.borders.workflow.identity.User;
import uk.gov.homeoffice.borders.workflow.security.WorkflowAuthentication;
import uk.gov.homeoffice.borders.workflow.task.TasksCountDto;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@JGivenStage
public class TaskApiControllerStage extends Stage<TaskApiControllerStage> {


    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private ResultActions mvcGetResults;

    private ResultActions mvcPostResults;

    private ProcessInstance processInstance;

    private ResultActions mvcTaskCountResults;

    public TaskApiControllerStage aNumberOfTasksCreated(int numberOfTasks) {
        List<Data> collectionOfData = new ArrayList<>();

        for (int i = 0; i < numberOfTasks; i++) {
            Data d = new Data();
            d.setAssignee("test");
            d.setCandidateGroup("test");
            d.setName("test " + i);
            d.setDescription("test " + i);
            collectionOfData.add(d);

        }

        ObjectValue dataObjectValue =
                Variables.objectValue(collectionOfData)
                        .serializationDataFormat("application/json")
                        .create();

        Map<String, Object> variables = new HashMap<>();
        variables.put("collectionOfData", dataObjectValue);
        variables.put("type", "non-notification");

        processInstance = runtimeService.startProcessInstanceByKey("test",
                variables);

        return this;
    }

    public TaskApiControllerStage aCallToGetTasksIsForUser(String username) throws Exception {
        User user = new User();
        user.setEmail(username);
        Team team = new Team();
        team.setName("test");
        user.setTeams(Collections.singletonList(team));
        WorkflowAuthentication workflowAuthentication = new WorkflowAuthentication(user);

        Mockito.when(identityService.getCurrentAuthentication()).thenReturn(workflowAuthentication);


        mvcGetResults = mockMvc.perform(get("/api/workflow/tasks").contentType(MediaType.APPLICATION_JSON));

        return this;
    }

    public TaskApiControllerStage statusIsOK() throws Exception {
        mvcGetResults.andExpect(status().isOk());
        return this;
    }

    public TaskApiControllerStage hasNextLink() throws Exception {
        mvcGetResults.andExpect(jsonPath("$._links['next'].href", is(not(""))));
        return this;
    }

    public TaskApiControllerStage hasFirstLink() throws Exception {
        mvcGetResults.andExpect(jsonPath("$._links['first'].href", is(not(""))));
        return this;
    }

    public TaskApiControllerStage hasLastLink() throws Exception {
        mvcGetResults.andExpect(jsonPath("$._links['last'].href", is(not(""))));
        return this;
    }


    public TaskApiControllerStage aQueryWithTaskName(String taskName) throws Exception {
        User user = new User();
        user.setEmail("test");
        Team team = new Team();
        team.setName("test");
        team.setId(UUID.randomUUID().toString());
        user.setTeams(Collections.singletonList(team));
        WorkflowAuthentication workflowAuthentication = new WorkflowAuthentication(user);

        Mockito.when(identityService.getCurrentAuthentication()).thenReturn(workflowAuthentication);

        TaskQueryDto taskQueryDto = new TaskQueryDto();
        taskQueryDto.setName(taskName);

        mvcPostResults = mockMvc.perform(post("/api/workflow/tasks")
                .content(objectMapper.writeValueAsString(taskQueryDto))
                .contentType(MediaType.APPLICATION_JSON));
        return this;
    }

    public TaskApiControllerStage responseIsOK() throws Exception {
        mvcPostResults.andExpect(status().isOk());
        return this;
    }

    public TaskApiControllerStage numberOfResultsShouldGreaterOrEqualTo(int expected) throws Exception {
        mvcPostResults.andExpect(jsonPath("$.page['totalElements']", greaterThanOrEqualTo(expected)));
        return this;
    }

    public TaskApiControllerStage resultSizeIsNotZero() throws Exception {
        mvcGetResults.andExpect(jsonPath("$.page['totalElements']", is(not(0))));
        return this;
    }

    public TaskApiControllerStage taskCountForUser(String username) throws Exception {
        User user = new User();
        user.setEmail(username);
        Team team = new Team();
        team.setName("test");
        team.setTeamCode("test");
        user.setTeams(Collections.singletonList(team));
        WorkflowAuthentication workflowAuthentication = new WorkflowAuthentication(user);

        Mockito.when(identityService.getCurrentAuthentication()).thenReturn(workflowAuthentication);

        mvcTaskCountResults = mockMvc.perform(get("/api/workflow/tasks/_task-counts").contentType(MediaType.APPLICATION_JSON));
        return this;
    }

    public TaskApiControllerStage numberOfUnassignedTasks(long number) throws Exception {

        TasksCountDto tasksCountDto = (TasksCountDto) mvcTaskCountResults.andReturn().getAsyncResult();
        assertThat(tasksCountDto.getTasksUnassigned(), is (number));
        return this;
    }

    public TaskApiControllerStage numberOfAssignedTasksToUser(long number) throws Exception {
        TasksCountDto tasksCountDto = (TasksCountDto) mvcTaskCountResults.andReturn().getAsyncResult();
        assertThat(tasksCountDto.getTasksAssignedToUser(), is (number));
        return this;
    }

    public TaskApiControllerStage numberOfTasksAssignedToTeam(long number) throws Exception {
        TasksCountDto tasksCountDto = (TasksCountDto) mvcTaskCountResults.andReturn().getAsyncResult();
        assertThat(tasksCountDto.getTotalTasksAllocatedToTeam(), is (number));
        return this;
    }


    public static class Data {
        private String assignee;
        private String candidateGroup;
        private String name;
        private String description;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getAssignee() {
            return assignee;
        }

        public void setAssignee(String assignee) {
            this.assignee = assignee;
        }

        public String getCandidateGroup() {
            return candidateGroup;
        }

        public void setCandidateGroup(String candidateGroup) {
            this.candidateGroup = candidateGroup;
        }
    }
}
