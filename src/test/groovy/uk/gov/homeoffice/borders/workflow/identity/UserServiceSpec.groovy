package uk.gov.homeoffice.borders.workflow.identity

import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.github.tomjankes.wiremock.WireMockGroovy
import org.junit.Rule
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import uk.gov.homeoffice.borders.workflow.PlatformDataUrlBuilder

class UserServiceSpec extends Specification {

    def wmPort = 8181

    @Rule
    WireMockRule wireMockRule = new WireMockRule(wmPort)

    def wireMockStub = new WireMockGroovy(wmPort)


    def platformDataUrlBuilder = new PlatformDataUrlBuilder('http://localhost:8181')
    def userService = new UserService(new RestTemplate(), platformDataUrlBuilder)

    def setup() {
        userService.self = userService
    }


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

    def 'can find by command'() {
        given:
        wireMockStub.stub {
            request {
                method 'GET'
                url '/shift?or=(subcommandid.eq.commandId,commandid.eq.commandId)'
            }

            response {
                status 200
                body """ [
                            {
                                "shiftid" : "id",
                                "staffid" : "staffid",
                                "teamid" : "teamId",
                                "phone" : "phone",
                                "email" : "email",
                                "commandid": "commandId"
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
        query.command("commandId")
        def result = userService.findByQuery(query)


        then:
        result
        !result.isEmpty()

    }

    def 'can find by subcommand'() {
        given:
        wireMockStub.stub {
            request {
                method 'GET'
                url '/shift?subcommandid=eq.subcommand'
            }

            response {
                status 200
                body """ [
                            {
                                "shiftid" : "id",
                                "staffid" : "staffid",
                                "teamid" : "teamId",
                                "phone" : "phone",
                                "email" : "email",
                                "subcommandid": "subcommand"
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
        query.subCommand("subcommand")
        def result = userService.findByQuery(query)


        then:
        result
        !result.isEmpty()

    }

    def 'can find by location'() {
        given:
        wireMockStub.stub {
            request {
                method 'GET'
                url '/shift?locationid=eq.locationId'
            }

            response {
                status 200
                body """ [
                            {
                                "shiftid" : "id",
                                "staffid" : "staffid",
                                "teamid" : "teamId",
                                "phone" : "phone",
                                "email" : "email",
                                "locationid": "locationId"
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
        query.location("locationId")
        def result = userService.findByQuery(query)


        then:
        result
        !result.isEmpty()
    }

    def 'illegal argument thrown if url is empty or null'() {
        when:
        def query = new UserQuery()
        userService.findByQuery(query)


        then:
        thrown(IllegalArgumentException)
    }

    def 'can find by user id in query'() {
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
        def query = new UserQuery()
        query.userId("email")
        def result = userService.findByQuery(query)


        then:
        result
        !result.isEmpty()
    }

    def 'no shift info returned if remote service throws Exception'() {
        given:
        wireMockStub.stub {
            request {
                method 'GET'
                url '/shift?email=eq.email'
            }

            response {
                status 504
                headers {
                    "Content-Type" "application/json"
                }
            }

        }
        when:
        def result = userService.findByUserId("email")

        then:
        !result

    }

    def 'no shift info returned if remote service returns empty result'() {
        given:
        wireMockStub.stub {
            request {
                method 'GET'
                url '/shift?email=eq.email'
            }

            response {
                status 200
                body '''[]'''
                headers {
                    "Content-Type" "application/json"
                }
            }

        }
        when:
        def result = userService.findByUserId("email")

        then:
        !result

    }

}
