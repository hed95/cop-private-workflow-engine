package uk.gov.homeoffice.borders.workflow.identity

import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.github.tomjankes.wiremock.WireMockGroovy
import org.junit.Rule
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import uk.gov.homeoffice.borders.workflow.PlatformDataUrlBuilder

class UserServiceSpec extends Specification {

    def wmPort = 8089

    @Rule
    WireMockRule wireMockRule = new WireMockRule(wmPort)

    def wireMockStub = new WireMockGroovy(wmPort)


    def platformDataUrlBuilder = new PlatformDataUrlBuilder('http://localhost:8089')
    def userService = new UserService(new RestTemplate(), platformDataUrlBuilder)

    def 'can find user by id'() {
        given:
        wireMockStub.stub {
            request {
                method 'GET'
                url '/shift?email=eq.email'
            }

            response {
                status 200
                body """ [
                            {
                                "shiftid" : "id",
                                "staffid" : "staffid",
                                "teamid" : "teamid",
                                "phone" : "phone",
                                "email" : "email"
                              }
                         ]
                     """
                headers {
                    "Content-Type" "application/json"
                }
            }

        }

        wireMockStub.stub {
            request {
                method 'GET'
                url '/staffview?staffid=eq.staffid'
            }
            response {
                status: 200
                body """
                       {
                          "phone": "phone",
                          "email": "email",
                          "gradetypeid": "gradetypeid",
                          "firstname": "firstname",
                          "surname": "surname",
                          "qualificationtypes": [
                            {
                              "qualificationname": "dummy",
                              "qualificationtype": "1"
                            },
                            {
                              "qualificationname": "staff",
                              "qualificationtype": "2"
                            }
                          ],
                          "staffid": "staffid",
                          "gradename": "grade"
                        }
                     """
                headers {
                    "Content-Type" "application/json"
                }
            }
        }

        wireMockStub.stub {
            request {
                method 'POST'
                url '/rpc/teamchildren'

            }
            response {
                status: 200
                body """
                       [
                          {
                            "teamid": "teamid",
                            "parentteamid": null,
                            "teamname": "teamname",
                            "teamcode": "teamcode"
                          }
                        ]
                     """
                headers {
                    "Content-Type" "application/json"
                }
            }
        }



        when:
        def result = userService.findByUserId("email")

        then:
        result
        result.email == 'email'

    }

    def 'can find by team id'() {
        given:
        wireMockStub.stub {
            request {
                method 'GET'
                url '/shift?teamid=eq.teamId'
            }

            response {
                status 200
                body """ [
                            {
                                "shiftid" : "id",
                                "staffid" : "staffid",
                                "teamid" : "teamId",
                                "phone" : "phone",
                                "email" : "email"
                              }
                         ]
                     """
                headers {
                    "Content-Type" "application/json"
                }
            }

        }

        wireMockStub.stub {
            request {
                method 'GET'
                url '/staffview?staffid=in.(staffid)'
            }
            response {
                status: 200
                body """
                       [{
                          "phone": "phone",
                          "email": "email",
                          "gradetypeid": "gradetypeid",
                          "firstname": "firstname",
                          "surname": "surname",
                          "qualificationtypes": [
                            {
                              "qualificationname": "dummy",
                              "qualificationtype": "1"
                            },
                            {
                              "qualificationname": "staff",
                              "qualificationtype": "2"
                            }
                          ],
                          "staffid": "staffid",
                          "gradename": "grade"
                        }]
                     """
                headers {
                    "Content-Type" "application/json"
                }
            }
        }

        when:
        def query = new UserQuery()
        query.memberOfGroup("teamId")
        def result = userService.findByQuery(query)


        then:
        result
        !result.isEmpty()
    }

}
