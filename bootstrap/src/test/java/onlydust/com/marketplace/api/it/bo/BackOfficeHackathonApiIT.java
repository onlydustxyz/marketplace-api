package onlydust.com.marketplace.api.it.bo;

import onlydust.com.backoffice.api.contract.model.HackathonsPageResponse;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagBO;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.port.output.HackathonStoragePort;
import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@TagBO
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackOfficeHackathonApiIT extends AbstractMarketplaceBackOfficeApiIT {

    final static MutableObject<String> hackathonId1 = new MutableObject<>();
    final static MutableObject<String> hackathonId2 = new MutableObject<>();

    UserAuthHelper.AuthenticatedBackofficeUser emilie;

    @BeforeEach
    void login() {
        emilie = userAuthHelper.authenticateEmilie();
    }

    @Test
    @Order(1)
    void should_raise_missing_authentication_given_no_access_token() {
        // When
        client.get()
                .uri(getApiURI(HACKATHONS))
                .exchange()
                .expectStatus()
                // Then
                .isUnauthorized();

        // When
        client.post()
                .uri(getApiURI(HACKATHONS))
                .exchange()
                .expectStatus()
                // Then
                .isUnauthorized();

        // When
        client.get()
                .uri(getApiURI(HACKATHONS_BY_ID.formatted(UUID.randomUUID().toString())))
                .exchange()
                .expectStatus()
                // Then
                .isUnauthorized();

        // When
        client.put()
                .uri(getApiURI(HACKATHONS_BY_ID.formatted(UUID.randomUUID().toString())))
                .exchange()
                .expectStatus()
                // Then
                .isUnauthorized();

        // When
        client.patch()
                .uri(getApiURI(HACKATHONS_BY_ID.formatted(UUID.randomUUID().toString())))
                .exchange()
                .expectStatus()
                // Then
                .isUnauthorized();

        // When
        client.delete()
                .uri(getApiURI(HACKATHONS_BY_ID.formatted(UUID.randomUUID().toString())))
                .exchange()
                .expectStatus()
                // Then
                .isUnauthorized();
    }

    @Test
    @Order(1)
    void should_raise_not_found_error_with_non_existing_hackathon() {
        // When
        client.get()
                .uri(getApiURI(HACKATHONS_BY_ID.formatted(UUID.randomUUID().toString())))
                .header("Authorization", "Bearer " + emilie.jwt())
                .exchange()
                .expectStatus()
                // Then
                .isNotFound();

        // When
        client.put()
                .uri(getApiURI(HACKATHONS_BY_ID.formatted(UUID.randomUUID().toString())))
                .header("Authorization", "Bearer " + emilie.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                          "status": "PUBLISHED",
                          "title": "Hackathon 2021 updated",
                          "description": "My hackathon description",
                          "location": "Paris",
                          "totalBudget": "$1.000.000",
                          "startDate": "2024-04-19T00:00:00Z",
                          "endDate": "2024-04-22T00:00:00Z",
                          "githubLabels": [],
                          "communityLinks": [],
                          "links": [],
                          "projectIds": [],
                          "events": []
                        }
                        """)
                .exchange()
                .expectStatus()
                // Then
                .isNotFound();

        // When
        client.patch()
                .uri(getApiURI(HACKATHONS_BY_ID.formatted(UUID.randomUUID().toString())))
                .header("Authorization", "Bearer " + emilie.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                          "status": "PUBLISHED"
                        }
                        """)
                .exchange()
                .expectStatus()
                // Then
                .isNotFound();
    }

    @Test
    @Order(1)
    void should_get_no_hackathons() {
        // When
        client.get()
                .uri(getApiURI(HACKATHONS, Map.of("pageIndex", "0", "pageSize", "10")))
                .header("Authorization", "Bearer " + emilie.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {"totalPageNumber":0,"totalItemNumber":0,"hasMore":false,"nextPageIndex":0,"hackathons":[]}
                        """);
    }

    @Test
    @Order(2)
    void should_create_new_hackathon() {
        // When
        client.post()
                .uri(getApiURI(HACKATHONS))
                .header("Authorization", "Bearer " + emilie.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "title": "Hackathon 2021",
                            "githubLabels": ["label1", "label2"],
                            "startDate": "2024-01-01T10:10:00Z",
                            "endDate": "2024-01-05T20:20:00Z"
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .isNoContent();

        final HackathonsPageResponse hackathonsPageResponse = client.get()
                .uri(getApiURI(HACKATHONS, Map.of("pageIndex", "0", "pageSize", "10")))
                .header("Authorization", "Bearer " + emilie.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(HackathonsPageResponse.class)
                .returnResult()
                .getResponseBody();
        hackathonId1.setValue(hackathonsPageResponse.getHackathons().get(0).getId().toString());
        assertThat(hackathonId1.getValue()).isNotEmpty();
        assertThat(UUID.fromString(hackathonId1.getValue())).isNotNull();

    }

    @Test
    @Order(3)
    void should_get_new_hackathon() {
        // When
        client.get()
                .uri(getApiURI(HACKATHONS_BY_ID.formatted(hackathonId1.getValue())))
                .header("Authorization", "Bearer " + emilie.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isEqualTo(hackathonId1.getValue())
                .json("""
                        {
                          "slug": "hackathon-2021",
                          "status": "DRAFT",
                          "title": "Hackathon 2021",
                          "githubLabels": [
                            "label1",
                            "label2"
                          ],
                          "subscriberCount": 0,
                          "startDate": "2024-01-01T10:10:00Z",
                          "endDate": "2024-01-05T20:20:00Z",
                          "description": null,
                          "location": null,
                          "totalBudget": null,
                          "communityLinks": [],
                          "links": [],
                          "projects": [],
                          "events": []
                        }
                        """);
    }

    @Test
    @Order(10)
    void should_update_hackathon() {
        // When
        client.put()
                .uri(getApiURI(HACKATHONS_BY_ID.formatted(hackathonId1.getValue())))
                .header("Authorization", "Bearer " + emilie.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                          "status": "PUBLISHED",
                          "title": "Hackathon 2021 updated",
                          "description": "My hackathon description",
                          "location": "Paris",
                          "totalBudget": "$1.000.000",
                          "startDate": "2024-04-19T11:00:00Z",
                          "endDate": "2024-04-22T13:00:00Z",
                          "githubLabels": ["label2", "good first issue"],
                          "communityLinks": [
                            {
                              "url": "https://a.bc",
                              "value": "ABC"
                            }
                          ],
                          "links": [
                            {
                              "url": "https://www.google.com",
                              "value": "Google"
                            },
                            {
                              "url": "https://www.facebook.com",
                              "value": "Facebook"
                            }
                          ],
                          "projectIds": [
                            "00490be6-2c03-4720-993b-aea3e07edd81",
                            "1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e"
                          ],
                          "events": [
                            {
                              "name": "Event 1",
                              "subtitle": "Event 1 subtitle",
                              "iconSlug": "event1",
                              "startDate": "2024-04-19T11:00:00Z",
                              "endDate": "2024-04-19T12:00:00Z",
                              "links": [
                                {
                                  "url": "https://www.event1.com",
                                  "value": "Event 1"
                                }
                              ]
                            },
                            {
                              "name": "Event 2",
                              "subtitle": "Event 2 subtitle",
                              "iconSlug": "event2",
                              "startDate": "2024-04-20T11:00:00Z",
                              "endDate": "2024-04-20T12:00:00Z",
                              "links": [
                                {
                                  "url": "https://www.event2.com",
                                  "value": "Event 2"
                                }
                              ]
                            }
                          ]
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .isEqualTo(204);
    }

    @Autowired
    HackathonStoragePort hackathonStoragePort;

    @Test
    @Order(11)
    void should_get_hackathon_issues() {
        // When
        client.get()
                .uri(getApiURI(HACKATHONS_BY_ID_ISSUES.formatted(hackathonId1.getValue()), Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "projectIds", "00490be6-2c03-4720-993b-aea3e07edd81,52cadf17-5a89-45b4-ad2a-884b4741e557",
                        "search", "Exercise"
                )))
                .header("Authorization", "Bearer " + emilie.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 3,
                          "totalItemNumber": 11,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "users": [
                            {
                              "id": 1270688337,
                              "number": 196,
                              "title": "Exercise on the ec_op builtin",
                              "status": "OPEN",
                              "repo": {
                                "id": 480776993,
                                "owner": "onlydustxyz",
                                "name": "starklings",
                                "htmlUrl": "https://github.com/onlydustxyz/starklings"
                              },
                              "projects": [
                                {
                                  "id": "00490be6-2c03-4720-993b-aea3e07edd81",
                                  "slug": "zama",
                                  "name": "Zama",
                                  "logoUrl": "https://dl.airtable.com/.attachments/f776b6ea66adbe46d86adaea58626118/610d50f6/15TqNyRwTMGoVeAX2u1M"
                                }
                              ],
                              "author": {
                                "githubUserId": 98529704,
                                "userId": null,
                                "login": "tekkac",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/98529704?v=4"
                              },
                              "labels": [
                                "Context: isolated",
                                "Difficulty: hard",
                                "Duration: few days",
                                "State: open",
                                "Techno: cairo",
                                "Type: feature",
                                "good first issue"
                              ],
                              "assignees": [],
                              "applicants": []
                            },
                            {
                              "id": 1270667749,
                              "number": 195,
                              "title": "Exercise on the range_check builtin",
                              "status": "OPEN",
                              "repo": {
                                "id": 480776993,
                                "owner": "onlydustxyz",
                                "name": "starklings",
                                "htmlUrl": "https://github.com/onlydustxyz/starklings"
                              },
                              "projects": [
                                {
                                  "id": "00490be6-2c03-4720-993b-aea3e07edd81",
                                  "slug": "zama",
                                  "name": "Zama",
                                  "logoUrl": "https://dl.airtable.com/.attachments/f776b6ea66adbe46d86adaea58626118/610d50f6/15TqNyRwTMGoVeAX2u1M"
                                }
                              ],
                              "author": {
                                "githubUserId": 98529704,
                                "userId": null,
                                "login": "tekkac",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/98529704?v=4"
                              },
                              "labels": [
                                "Context: isolated",
                                "Difficulty: intermediate",
                                "Duration: few days",
                                "State: open",
                                "Techno: cairo",
                                "Type: feature",
                                "good first issue"
                              ],
                              "assignees": [
                                {
                                  "githubUserId": 98529704,
                                  "userId": null,
                                  "login": "tekkac",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/98529704?v=4"
                                }
                              ],
                              "applicants": []
                            },
                            {
                              "id": 1270661065,
                              "number": 194,
                              "title": "Exercise on the output builtin",
                              "status": "OPEN",
                              "repo": {
                                "id": 480776993,
                                "owner": "onlydustxyz",
                                "name": "starklings",
                                "htmlUrl": "https://github.com/onlydustxyz/starklings"
                              },
                              "projects": [
                                {
                                  "id": "00490be6-2c03-4720-993b-aea3e07edd81",
                                  "slug": "zama",
                                  "name": "Zama",
                                  "logoUrl": "https://dl.airtable.com/.attachments/f776b6ea66adbe46d86adaea58626118/610d50f6/15TqNyRwTMGoVeAX2u1M"
                                }
                              ],
                              "author": {
                                "githubUserId": 98529704,
                                "userId": null,
                                "login": "tekkac",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/98529704?v=4"
                              },
                              "labels": [
                                "Context: isolated",
                                "Difficulty: intermediate",
                                "Duration: few days",
                                "State: open",
                                "Techno: cairo",
                                "Type: feature",
                                "good first issue"
                              ],
                              "assignees": [],
                              "applicants": []
                            },
                            {
                              "id": 1269383720,
                              "number": 191,
                              "title": "Fix end of exercises message",
                              "status": "COMPLETED",
                              "repo": {
                                "id": 480776993,
                                "owner": "onlydustxyz",
                                "name": "starklings",
                                "htmlUrl": "https://github.com/onlydustxyz/starklings"
                              },
                              "projects": [
                                {
                                  "id": "00490be6-2c03-4720-993b-aea3e07edd81",
                                  "slug": "zama",
                                  "name": "Zama",
                                  "logoUrl": "https://dl.airtable.com/.attachments/f776b6ea66adbe46d86adaea58626118/610d50f6/15TqNyRwTMGoVeAX2u1M"
                                }
                              ],
                              "author": {
                                "githubUserId": 4435377,
                                "userId": "6115f024-159a-4b1f-b713-1e2ad5c6063e",
                                "login": "Bernardstanislas",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4"
                              },
                              "labels": [
                                "Context: coupled",
                                "Difficulty: easy",
                                "Duration: under a day",
                                "State: open",
                                "Techno: python",
                                "Type: bug",
                                "good first issue"
                              ],
                              "assignees": [],
                              "applicants": []
                            },
                            {
                              "id": 1263404890,
                              "number": 183,
                              "title": "Put the Id struct in the exercise storage02",
                              "status": "COMPLETED",
                              "repo": {
                                "id": 480776993,
                                "owner": "onlydustxyz",
                                "name": "starklings",
                                "htmlUrl": "https://github.com/onlydustxyz/starklings"
                              },
                              "projects": [
                                {
                                  "id": "00490be6-2c03-4720-993b-aea3e07edd81",
                                  "slug": "zama",
                                  "name": "Zama",
                                  "logoUrl": "https://dl.airtable.com/.attachments/f776b6ea66adbe46d86adaea58626118/610d50f6/15TqNyRwTMGoVeAX2u1M"
                                }
                              ],
                              "author": {
                                "githubUserId": 4435377,
                                "userId": "6115f024-159a-4b1f-b713-1e2ad5c6063e",
                                "login": "Bernardstanislas",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4"
                              },
                              "labels": [
                                "Context: isolated",
                                "Difficulty: easy",
                                "Duration: under a day",
                                "State: open",
                                "Techno: cairo",
                                "Type: refactor",
                                "good first issue"
                              ],
                              "assignees": [
                                {
                                  "githubUserId": 34384633,
                                  "userId": null,
                                  "login": "tdelabro",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/34384633?v=4"
                                }
                              ],
                              "applicants": []
                            }
                          ]
                        }
                        """);
    }

    @Test
    @Order(11)
    void should_get_updated_hackathon() {
        hackathonStoragePort.registerUser(UserId.of("fc92397c-3431-4a84-8054-845376b630a0"), Hackathon.Id.of(hackathonId1.getValue()));

        // When
        client.get()
                .uri(getApiURI(HACKATHONS_BY_ID.formatted(hackathonId1.getValue())))
                .header("Authorization", "Bearer " + emilie.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isEqualTo(hackathonId1.getValue())
                .json("""
                        {
                          "slug": "hackathon-2021-updated",
                          "status": "PUBLISHED",
                          "title": "Hackathon 2021 updated",
                          "githubLabels": [
                            "good first issue",
                            "label2"
                          ],
                          "subscriberCount": 1,
                          "startDate": "2024-04-19T11:00:00Z",
                          "endDate": "2024-04-22T13:00:00Z",
                          "description": "My hackathon description",
                          "location": "Paris",
                          "totalBudget": "$1.000.000",
                          "communityLinks": [
                            {
                              "url": "https://a.bc",
                              "value": "ABC"
                            }
                          ],
                          "links": [
                            {
                              "url": "https://www.google.com",
                              "value": "Google"
                            },
                            {
                              "url": "https://www.facebook.com",
                              "value": "Facebook"
                            }
                          ],
                          "projects": [
                            {
                              "id": "00490be6-2c03-4720-993b-aea3e07edd81",
                              "slug": "zama",
                              "name": "Zama",
                              "logoUrl": "https://dl.airtable.com/.attachments/f776b6ea66adbe46d86adaea58626118/610d50f6/15TqNyRwTMGoVeAX2u1M"
                            },
                            {
                              "id": "1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e",
                              "slug": "calcom",
                              "name": "Cal.com",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5271998260751715005.png"
                            }
                          ],
                          "events": [
                            {
                              "name": "Event 1",
                              "subtitle": "Event 1 subtitle",
                              "iconSlug": "event1",
                              "startDate": "2024-04-19T11:00:00Z",
                              "endDate": "2024-04-19T12:00:00Z",
                              "links": [
                                {
                                  "url": "https://www.event1.com",
                                  "value": "Event 1"
                                }
                              ]
                            },
                            {
                              "name": "Event 2",
                              "subtitle": "Event 2 subtitle",
                              "iconSlug": "event2",
                              "startDate": "2024-04-20T11:00:00Z",
                              "endDate": "2024-04-20T12:00:00Z",
                              "links": [
                                {
                                  "url": "https://www.event2.com",
                                  "value": "Event 2"
                                }
                              ]
                            }
                          ]
                        }
                        """);
    }

    @Test
    @Order(20)
    void should_update_hackathon_again_and_update_lists_accordingly() {
        // When
        client.put()
                .uri(getApiURI(HACKATHONS_BY_ID.formatted(hackathonId1.getValue())))
                .header("Authorization", "Bearer " + emilie.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                          "status": "PUBLISHED",
                          "title": "Hackathon 2021 updated 2",
                          "description": "My hackathon description 2",
                          "location": "Paris 2",
                          "totalBudget": "$2.000.000",
                          "startDate": "2024-04-23T11:00:00Z",
                          "endDate": "2024-04-25T13:00:00Z",
                          "githubLabels": ["label99", "label100"],
                          "communityLinks": [
                            {
                              "url": "https://yoooo.yo",
                              "value": "Yo"
                            }
                          ],
                          "links": [
                            {
                              "url": "https://www.foo.com",
                              "value": "Foo"
                            },
                            {
                              "url": "https://www.facebook.com",
                              "value": "Facebook"
                            },
                            {
                              "url": "https://www.bar.com",
                              "value": "Bar"
                            }
                          ],
                          "projectIds": [
                            "2073b3b2-60f4-488c-8a0a-ab7121ed850c"
                          ],
                          "events": [
                            {
                              "name": "Event 1",
                              "subtitle": "Event 1 subtitle",
                              "iconSlug": "event1",
                              "startDate": "2024-04-19T11:00:00Z",
                              "endDate": "2024-04-19T12:00:00Z",
                              "links": [
                                {
                                  "url": "https://www.event1.com",
                                  "value": "Event 1"
                                }
                              ]
                            },
                            {
                              "name": "Event 3",
                              "subtitle": "Event 3 subtitle",
                              "iconSlug": "event3",
                              "startDate": "2024-04-21T11:00:00Z",
                              "endDate": "2024-04-21T12:00:00Z",
                              "links": [
                                {
                                  "url": "https://www.event3.com",
                                  "value": "Event 3"
                                },
                                {
                                  "url": "https://www.event333.com",
                                  "value": "Event 333"
                                }
                              ]
                            }
                          ]
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .isNoContent();

        client.get()
                .uri(getApiURI(HACKATHONS_BY_ID.formatted(hackathonId1.getValue())))
                .header("Authorization", "Bearer " + emilie.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isEqualTo(hackathonId1.getValue())
                .json("""
                        {
                          "slug": "hackathon-2021-updated-2",
                          "status": "PUBLISHED",
                          "title": "Hackathon 2021 updated 2",
                          "githubLabels": [
                            "label100",
                            "label99"
                          ],
                          "subscriberCount": 1,
                          "startDate": "2024-04-23T11:00:00Z",
                          "endDate": "2024-04-25T13:00:00Z",
                          "description": "My hackathon description 2",
                          "location": "Paris 2",
                          "totalBudget": "$2.000.000",
                          "communityLinks": [
                            {
                              "url": "https://yoooo.yo",
                              "value": "Yo"
                            }
                          ],
                          "links": [
                            {
                              "url": "https://www.facebook.com",
                              "value": "Facebook"
                            },
                            {
                              "url": "https://www.foo.com",
                              "value": "Foo"
                            },
                            {
                              "url": "https://www.bar.com",
                              "value": "Bar"
                            }
                          ],
                          "projects": [
                            {
                              "id": "2073b3b2-60f4-488c-8a0a-ab7121ed850c",
                              "slug": "apibara",
                              "name": "Apibara",
                              "logoUrl": null
                            }
                          ],
                          "events": [
                            {
                              "name": "Event 1",
                              "subtitle": "Event 1 subtitle",
                              "iconSlug": "event1",
                              "startDate": "2024-04-19T11:00:00Z",
                              "endDate": "2024-04-19T12:00:00Z",
                              "links": [
                                {
                                  "url": "https://www.event1.com",
                                  "value": "Event 1"
                                }
                              ]
                            },
                            {
                              "name": "Event 3",
                              "subtitle": "Event 3 subtitle",
                              "iconSlug": "event3",
                              "startDate": "2024-04-21T11:00:00Z",
                              "endDate": "2024-04-21T12:00:00Z",
                              "links": [
                                {
                                  "url": "https://www.event3.com",
                                  "value": "Event 3"
                                },
                                {
                                  "url": "https://www.event333.com",
                                  "value": "Event 333"
                                }
                              ]
                            }
                          ]
                        }
                        """);

    }

    @Test
    @Order(30)
    void should_create_another_hackathon() {
        // When
        client.post()
                .uri(getApiURI(HACKATHONS))
                .header("Authorization", "Bearer " + emilie.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "title": "OD Hack",
                            "githubLabels": ["ODHack5"],
                            "startDate": "2024-06-01T00:00:00Z",
                            "endDate": "2024-06-05T00:00:00Z"
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .isNoContent();

        final HackathonsPageResponse hackathonsPageResponse = client.get()
                .uri(getApiURI(HACKATHONS, Map.of("pageIndex", "0", "pageSize", "10")))
                .header("Authorization", "Bearer " + emilie.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(HackathonsPageResponse.class)
                .returnResult()
                .getResponseBody();
        hackathonId2.setValue(hackathonsPageResponse.getHackathons().get(0).getId().toString());
        assertThat(hackathonId2.getValue()).isNotEmpty();
        assertThat(UUID.fromString(hackathonId2.getValue())).isNotNull();
        assertThat(hackathonId2.getValue()).isNotEqualTo(hackathonId1.getValue());

        client.get()
                .uri(getApiURI(HACKATHONS_BY_ID.formatted(hackathonId2.getValue())))
                .header("Authorization", "Bearer " + emilie.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isEqualTo(hackathonId2.getValue())
                .json("""
                        {
                          "slug": "od-hack",
                          "status": "DRAFT",
                          "title": "OD Hack",
                          "githubLabels": [
                            "ODHack5"
                          ],
                          "subscriberCount": 0,
                          "startDate": "2024-06-01T00:00:00Z",
                          "endDate": "2024-06-05T00:00:00Z",
                          "description": null,
                          "location": null,
                          "totalBudget": null,
                          "communityLinks": [],
                          "links": [],
                          "projects": []
                        }
                        """);
    }

    @Test
    @Order(31)
    void should_get_hackathons() {
        // When
        client.get()
                .uri(getApiURI(HACKATHONS, Map.of("pageIndex", "0", "pageSize", "10")))
                .header("Authorization", "Bearer " + emilie.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "hackathons": [
                            {
                              "slug": "hackathon-2021-updated-2",
                              "status": "PUBLISHED",
                              "title": "Hackathon 2021 updated 2",
                              "githubLabels": [
                                "label100",
                                "label99"
                              ],
                              "subscriberCount": 1,
                              "startDate": "2024-04-23T11:00:00Z",
                              "endDate": "2024-04-25T13:00:00Z"
                            },
                            {
                              "slug": "od-hack",
                              "status": "DRAFT",
                              "title": "OD Hack",
                              "githubLabels": [
                                "ODHack5"
                              ],
                              "subscriberCount": 0,
                              "startDate": "2024-06-01T00:00:00Z",
                              "endDate": "2024-06-05T00:00:00Z"
                            }
                          ]
                        }
                        """);
    }

    @Test
    @Order(32)
    void should_get_hackathons_paginated() {
        // When
        client.get()
                .uri(getApiURI(HACKATHONS, Map.of("pageIndex", "0", "pageSize", "1")))
                .header("Authorization", "Bearer " + emilie.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 2,
                          "totalItemNumber": 2,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "hackathons": [
                            {
                              "slug": "od-hack",
                              "status": "DRAFT",
                              "title": "OD Hack",
                              "githubLabels": [
                                "ODHack5"
                              ],
                              "subscriberCount": 0,
                              "startDate": "2024-06-01T00:00:00Z",
                              "endDate": "2024-06-05T00:00:00Z"
                            }
                          ]
                        }
                        """);
    }

    @Test
    @Order(40)
    void should_patch_hackathon_status() {
        // When
        client.patch()
                .uri(getApiURI(HACKATHONS_BY_ID.formatted(hackathonId2.getValue())))
                .header("Authorization", "Bearer " + emilie.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                          "status": "PUBLISHED"
                        }
                        """)
                .exchange()
                .expectStatus()
                // Then
                .isNoContent();

        // And when
        client.get()
                .uri(getApiURI(HACKATHONS, Map.of("pageIndex", "0", "pageSize", "1")))
                .header("Authorization", "Bearer " + emilie.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 2,
                          "totalItemNumber": 2,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "hackathons": [
                            {
                              "slug": "od-hack",
                              "status": "PUBLISHED",
                              "title": "OD Hack",
                              "githubLabels": [
                                "ODHack5"
                              ],
                              "subscriberCount": 0,
                              "startDate": "2024-06-01T00:00:00Z",
                              "endDate": "2024-06-05T00:00:00Z"
                            }
                          ]
                        }
                        """);
    }

    @Test
    @Order(50)
    void should_get_registered_users() {
        // Given
        hackathonStoragePort.registerUser(UserId.of("dd0ab03c-5875-424b-96db-a35522eab365"), Hackathon.Id.of(hackathonId1.getValue()));
        hackathonStoragePort.registerUser(UserId.of("fc92397c-3431-4a84-8054-845376b630a0"), Hackathon.Id.of(hackathonId1.getValue()));
        hackathonStoragePort.registerUser(UserId.of("747e663f-4e68-4b42-965b-b5aebedcd4c4"), Hackathon.Id.of(hackathonId1.getValue()));

        // When
        client.get()
                .uri(getApiURI(HACKATHONS_BY_ID_USERS.formatted(hackathonId1.getValue()), Map.of("pageIndex", "0", "pageSize", "2")))
                .header("Authorization", "Bearer " + emilie.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 2,
                          "totalItemNumber": 3,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "users": [
                            {
                              "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4",
                              "githubUserId": 43467246,
                              "login": "AnthonyBuisset",
                              "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                              "email": "abuisset@gmail.com",
                              "lastSeenAt": "2023-10-05T19:06:50.034Z",
                              "signedUpAt": "2022-12-12T09:51:58.48559Z"
                            },
                            {
                              "id": "dd0ab03c-5875-424b-96db-a35522eab365",
                              "githubUserId": 21149076,
                              "login": "oscarwroche",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                              "email": "oscar.w.roche@gmail.com",
                              "lastSeenAt": "2023-06-27T09:11:30.869Z",
                              "signedUpAt": "2022-12-15T08:18:40.237388Z"
                            }
                          ]
                        }
                        """);
    }

    @Test
    @Order(100)
    void should_not_delete_hackathon_with_registered_users() {
        // When
        client.delete()
                .uri(getApiURI(HACKATHONS_BY_ID.formatted(hackathonId1.getValue())))
                .header("Authorization", "Bearer " + emilie.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isBadRequest();

        // When
        client.get()
                .uri(getApiURI(HACKATHONS, Map.of("pageIndex", "0", "pageSize", "10")))
                .header("Authorization", "Bearer " + emilie.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "hackathons": [
                            {
                              "slug": "od-hack",
                              "status": "PUBLISHED",
                              "title": "OD Hack",
                              "githubLabels": [
                                "ODHack5"
                              ],
                              "subscriberCount": 0,
                              "startDate": "2024-06-01T00:00:00Z",
                              "endDate": "2024-06-05T00:00:00Z"
                            },
                            {
                              "slug": "hackathon-2021-updated-2",
                              "status": "PUBLISHED",
                              "title": "Hackathon 2021 updated 2",
                              "githubLabels": [
                                "label100",
                                "label99"
                              ],
                              "subscriberCount": 3,
                              "startDate": "2024-04-23T11:00:00Z",
                              "endDate": "2024-04-25T13:00:00Z"
                            }
                          ]
                        }
                        """);
    }

    @Test
    @Order(101)
    void should_delete_hackathon() {
        // When
        client.delete()
                .uri(getApiURI(HACKATHONS_BY_ID.formatted(hackathonId2.getValue())))
                .header("Authorization", "Bearer " + emilie.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        // When
        client.get()
                .uri(getApiURI(HACKATHONS, Map.of("pageIndex", "0", "pageSize", "10")))
                .header("Authorization", "Bearer " + emilie.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 1,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "hackathons": [
                            {
                              "slug": "hackathon-2021-updated-2",
                              "status": "PUBLISHED",
                              "title": "Hackathon 2021 updated 2",
                              "githubLabels": [
                                "label100",
                                "label99"
                              ],
                              "subscriberCount": 3,
                              "startDate": "2024-04-23T11:00:00Z",
                              "endDate": "2024-04-25T13:00:00Z"
                            }
                          ]
                        }
                        """);
    }
}
