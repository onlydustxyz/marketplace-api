package onlydust.com.marketplace.api.it.bo;

import onlydust.com.backoffice.api.contract.model.HackathonsPageResponse;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagBO;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.port.output.HackathonStoragePort;
import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

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
                          "sponsorIds": [],
                          "projectIds": []
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
                          "sponsors": [],
                          "projects": []
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
                          "githubLabels": ["label2", "label3"],
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
                          "sponsorIds": [
                            "0d66ba03-cecb-45a4-ab7d-98f0cc18a3aa",
                            "85435c9b-da7f-4670-bf65-02b84c5da7f0"
                          ],
                          "projectIds": [
                            "8156fc5f-cec5-4f70-a0de-c368772edcd4",
                            "7ce1a761-2b7b-43ba-9eb5-17e95ef4aa54"
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
    void should_get_updated_hackathon() {
        hackathonStoragePort.registerUser(UUID.fromString("fc92397c-3431-4a84-8054-845376b630a0"), Hackathon.Id.of(hackathonId1.getValue()));

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
                            "label2",
                            "label3"
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
                          "sponsors": [
                            {
                              "id": "85435c9b-da7f-4670-bf65-02b84c5da7f0",
                              "name": "AS Nancy Lorraine",
                              "url": null,
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/951523516066154017.png"
                            },
                            {
                              "id": "0d66ba03-cecb-45a4-ab7d-98f0cc18a3aa",
                              "name": "Red Bull",
                              "url": "https://www.redbull.com/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13218160580172982881.jpg"
                            }
                          ],
                          "projects": [
                            {
                              "id": "7ce1a761-2b7b-43ba-9eb5-17e95ef4aa54",
                              "slug": "cairo-streams",
                              "name": "Cairo streams",
                              "logoUrl": null
                            },
                            {
                              "id": "8156fc5f-cec5-4f70-a0de-c368772edcd4",
                              "slug": "cairo-foundry",
                              "name": "Cairo foundry",
                              "logoUrl": null
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
                          "sponsorIds": [
                            "44c6807c-48d1-4987-a0a6-ac63f958bdae"
                          ],
                          "projectIds": [
                            "2073b3b2-60f4-488c-8a0a-ab7121ed850c"
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
                          "sponsors": [
                            {
                              "id": "44c6807c-48d1-4987-a0a6-ac63f958bdae",
                              "name": "Coca Colax",
                              "url": "https://www.coca-cola-france.fr/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/10299112926576087945.jpg"
                            }
                          ],
                          "projects": [
                            {
                              "id": "2073b3b2-60f4-488c-8a0a-ab7121ed850c",
                              "slug": "apibara",
                              "name": "Apibara",
                              "logoUrl": null
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
        hackathonId2.setValue(hackathonsPageResponse.getHackathons().get(1).getId().toString());
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
                          "sponsors": [],
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
                .uri(getApiURI(HACKATHONS, Map.of("pageIndex", "1", "pageSize", "1")))
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
                          "hasMore": false,
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
                .uri(getApiURI(HACKATHONS, Map.of("pageIndex", "1", "pageSize", "1")))
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
                          "hasMore": false,
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
        hackathonStoragePort.registerUser(UUID.fromString("dd0ab03c-5875-424b-96db-a35522eab365"), Hackathon.Id.of(hackathonId1.getValue()));
        hackathonStoragePort.registerUser(UUID.fromString("fc92397c-3431-4a84-8054-845376b630a0"), Hackathon.Id.of(hackathonId1.getValue()));
        hackathonStoragePort.registerUser(UUID.fromString("747e663f-4e68-4b42-965b-b5aebedcd4c4"), Hackathon.Id.of(hackathonId1.getValue()));

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
