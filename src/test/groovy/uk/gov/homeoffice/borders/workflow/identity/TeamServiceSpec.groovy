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
        refDataBean.url="http://localhost:8182"
        def refDataUrlBuilder = new RefDataUrlBuilder(refDataBean)
        teamService = new TeamService(new RestTemplate(), refDataUrlBuilder)
    }

    def 'can find by id'() {
        given:
        wireMockStub.stub {
            request {
                method 'GET'
                url '/team?teamcode=eq.teamcode'
            }

            response {
                status 200
                body """ [
                            {
                                "teamid" : "id",
                                "teamcode" : "teamcode",
                                "teamname" : "teamname"
                            }
                         ]
                     """
                headers {
                    "Content-Type" "application/vnd.pgrst.object+json"
                }
            }

        }
        when:
        def result = teamService.findById("teamcode")

        then:
        result
        result.name == 'teamname'
        result.teamCode == 'teamcode'
    }

    def 'can find by query'() {
        given:
        wireMockStub.stub {
            request {
                method 'GET'
                url '/team?teamname=eq.teamname'
            }

            response {
                status 200
                body """ [
                            {
                                "teamid" : "id",
                                "teamcode" : "teamcode",
                                "teamname" : "teamname"
                            }
                         ]
                     """
                headers {
                    "Content-Type" "application/vnd.pgrst.object+json"
                }
            }

        }
        when:
        def teamQuery = new TeamQuery().groupName("teamname")
        def result = teamService.findByQuery(teamQuery)

        then:
        result
        result.size() == 1
        result[0].name == 'teamname'

    }

    def 'can find team children'() {
        given:
            wireMockStub.stub {
                request {
                    method 'GET'
                    url '/team?parentteamid=in.(%5B1234%5D)'
                }

                response {
                    status 200
                    body """[{
                                "teamid" : "5678"
                            },{
                                "teamid" : "4321"
                            }]"""
                    headers {
                        "Content-Type" "application/vnd.pgrst.object+json"
                    }
                }
            }
            wireMockStub.stub {
                request {
                    method 'GET'
                    url '/team?parentteamid=in.(%5B4321,%205678%5D)'
                }

                response {
                    status 200
                    body """[]"""
                    headers {
                        "Content-Type" "application/vnd.pgrst.object+json"
                    }
                }
            }

        when:
            def teams = teamService.teamChildren('1234')

        then:
            teams.collect({t -> t.id}) == ['5678', '4321']
    }

    def 'can find no team children'() {
        given:
            wireMockStub.stub {
                request {
                    method 'GET'
                    url '/team?parentteamid=in.(%5B1234%5D)'
                }

                response {
                    status 200
                    body """[]"""
                    headers {
                        "Content-Type" "application/vnd.pgrst.object+json"
                    }
                }
            }

        when:
            def teams = teamService.teamChildren('1234')

        then:
            teams == []
    }
}
