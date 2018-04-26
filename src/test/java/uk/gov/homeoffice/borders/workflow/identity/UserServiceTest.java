package uk.gov.homeoffice.borders.workflow.identity;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.homeoffice.borders.workflow.BaseIntClass;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class UserServiceTest extends BaseIntClass {

    @Autowired
    private UserService userService;

    @Test
    public void canFindByUserId() throws Exception {
        //given
        String response = IOUtils.toString(this.getClass().getResourceAsStream("/get-single-active-user.json"), "UTF-8");
        wireMockRule.stubFor(get(urlEqualTo("/_QUERIES/read/get-active-user?email=email"))
                .willReturn(aResponse().withHeader(
                        "Content-Type", "application/json"
                ).withBody(
                        response
                )));

        //when
        User user = userService.findByUserId("email");

        //then
        assertThat(user, Matchers.is(notNullValue()));
    }

    @Test
    public void canGetAllUsers() throws Exception {
        //given
        String response = IOUtils.toString(this.getClass().getResourceAsStream("/get-all-active-users.json"), "UTF-8");
        wireMockRule.stubFor(get(urlEqualTo("/_QUERIES/read/get-active-users"))
                .willReturn(aResponse().withHeader(
                        "Content-Type", "application/json"
                ).withBody(
                        response
                )));

        //when
        List<User> users = userService.allUsers();

        //then
        assertThat(users, Matchers.is(notNullValue()));
        assertThat(users.size(), is(1));
    }

}
