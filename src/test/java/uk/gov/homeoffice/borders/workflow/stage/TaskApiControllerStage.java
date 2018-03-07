package uk.gov.homeoffice.borders.workflow.stage;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.integration.spring.JGivenStage;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.task.Task;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.homeoffice.borders.workflow.identity.User;
import uk.gov.homeoffice.borders.workflow.security.WorkflowAuthentication;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@JGivenStage
public class TaskApiControllerStage extends Stage<TaskApiControllerStage> {

    @Autowired
    private TaskService taskService;

    @MockBean
    private IdentityService identityService;

    @Autowired
    private MockMvc mockMvc;

    private ResultActions mvcResult;

    public TaskApiControllerStage aNumberOfTasksCreated(int numberOfTasks) {

        for (int i = 0; i < numberOfTasks; i++) {
            Task task = new TaskEntity(UUID.randomUUID().toString());
            task.setName("test" + i);
            task.setDescription("test" + i);
            task.setAssignee("test");
            taskService.saveTask(task);
        }
        return this;
    }

    public TaskApiControllerStage aCallToGetTasksIsForUser(String username) throws Exception {
        User user = new User();
        user.setUsername(username);
        WorkflowAuthentication workflowAuthentication = new WorkflowAuthentication(user);

        Mockito.when(identityService.getCurrentAuthentication()).thenReturn(workflowAuthentication);


        mvcResult = mockMvc.perform(get("/api/engine/tasks").contentType(MediaType.APPLICATION_JSON));

        return this;
    }

    public TaskApiControllerStage statusIsOK() throws Exception {
        mvcResult.andExpect(status().isOk());
        return this;
    }

    public TaskApiControllerStage hasNextLink() throws Exception {
        mvcResult.andExpect(jsonPath("$._links['next'].href", is(not(""))));
        return this;
    }

    public TaskApiControllerStage hasFirstLink() throws Exception {
        mvcResult.andExpect(jsonPath("$._links['first'].href", is(not(""))));
        return this;
    }

    public TaskApiControllerStage hasLastLink() throws Exception {
        mvcResult.andExpect(jsonPath("$._links['last'].href", is(not(""))));
        return this;
    }


    public TaskApiControllerStage totalResults(int expected) throws Exception {
        mvcResult.andExpect(jsonPath("$.page['totalElements']", is(expected)));
        return this;
    }
}
