package uk.gov.homeoffice.borders.workflow.identity

import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.github.tomjankes.wiremock.WireMockGroovy
import org.junit.Rule
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import uk.gov.homeoffice.borders.workflow.RefDataUrlBuilder
import uk.gov.homeoffice.borders.workflow.config.RefDataBean

class TeamServiceSpec extends Specification {

    def wmPort = 8182

    @Rule
    WireMockRule wireMockRule = new WireMockRule(wmPort)

    def wireMockStub = new WireMockGroovy(wmPort)
    def teamService

    def setup() {
        def refDataBean = new RefDataBean()
        refDataBean.url=new URI("http://localhost:" + wmPort)
        def refDataUrlBuilder = new RefDataUrlBuilder(refDataBean)
        teamService = new TeamService(new RestTemplate(), refDataUrlBuilder)
    }

    def 'can find by id'() {
        given:
        wireMockStub.stub {
            request {
                method 'GET'
                url '/v2/entities/team?filter=code%3Deq.code&mode=dataOnly'
            }

            response {
                status 200
                body """ {"data":[
                            {
                                "id" : "id",
                                "code" : "code",
                                "name" : "teamname"
                            }
                         ]}
                     """
                headers {
                    "Content-Type" "application/json"
                }
            }

        }
        when:
        def result = teamService.findById("code")

        then:
        result
        result.name == 'teamname'
        result.code == 'code'
    }

    def 'can find by query'() {
        given:
        wireMockStub.stub {
            request {
                method 'GET'
                url '/v2/entities/team?filter=name%3Deq.name&mode=dataOnly'
            }

            response {
                status 200
                body """ {"data":[
                            {
                                "id" : "id",
                                "code" : "code",
                                "name" : "teamname"
                            }
                         ]}
                     """
                headers {
                    "Content-Type" "application/json"
                }
            }

        }
        when:
        def teamQuery = new TeamQuery().groupName("name")
        def result = teamService.findByQuery(teamQuery)

        then:
        result
        result.size() == 1
        result[0].name == 'teamname'

    }
}
