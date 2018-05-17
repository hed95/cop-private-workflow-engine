package uk.gov.homeoffice.borders.workflow.identity;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
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
        String response = IOUtils.toString(this.getClass().getResourceAsStream("/shift.json"), "UTF-8");
        wireMockRule.stubFor(get(urlEqualTo("/shift?email=eq.email"))
                .willReturn(aResponse().withHeader(
                        "Content-Type", "application/json"
                ).withBody(
                        response
                )));
        String staff = IOUtils.toString(this.getClass().getResourceAsStream("/staffview.json"), "UTF-8");

        wireMockRule.stubFor(get(urlEqualTo("/staffview?staffid=eq.staffid"))
                .willReturn(aResponse().withHeader(
                        "Content-Type", "application/json"
                ).withBody(
                        staff
                )));

        wireMockRule.stubFor(post(urlEqualTo("/rpc/teamchildren"))
                .willReturn(aResponse().withHeader(
                        "Content-Type", "application/json"
                ).withBody(
                        IOUtils.toString(this.getClass().getResourceAsStream("/team.json"), "UTF-8")
                )));

        //when
        User user = userService.findByUserId("email");

        //then
        assertThat(user, Matchers.is(notNullValue()));
        assertThat(user.getQualifications().size(), is(2));
    }

    @Test
    public void canQueryByTeamId() throws Exception {
        //given
        String response = IOUtils.toString(this.getClass().getResourceAsStream("/shift.json"), "UTF-8");
        wireMockRule.stubFor(get(urlEqualTo("/shift?teamid=eq.teamId"))
                .willReturn(aResponse().withHeader(
                        "Content-Type", "application/json"
                ).withBody(
                        response
                )));

        String staff = IOUtils.toString(this.getClass().getResourceAsStream("/staffviewlist.json"), "UTF-8");

        wireMockRule.stubFor(get(urlEqualTo("/staffview?staffid=in.(staffid)"))
                .willReturn(aResponse().withHeader(
                        "Content-Type", "application/json"
                ).withBody(
                        staff
                )));

        //when
        UserQuery query = new UserQuery();
        query.memberOfGroup("teamId");

        List<User> users = userService.findByQuery(query);

        //then
        assertThat(users, Matchers.is(notNullValue()));
        assertThat(users.size(), is(1));
    }

}
