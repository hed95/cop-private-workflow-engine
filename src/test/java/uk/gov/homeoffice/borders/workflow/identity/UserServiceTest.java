package uk.gov.homeoffice.borders.workflow.identity;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.homeoffice.borders.workflow.BaseIntClass;

@Ignore
public class UserServiceTest extends BaseIntClass {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    @Autowired
    private UserService userService;


    @Test
    public void canFindByUserId() {

    }

}
