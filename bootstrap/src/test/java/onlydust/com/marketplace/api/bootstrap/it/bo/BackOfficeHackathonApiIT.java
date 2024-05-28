package onlydust.com.marketplace.api.bootstrap.it.bo;

import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.port.output.HackathonStoragePort;
import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

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
                          "subtitle": "subtitle updated",
                          "description": "My hackathon description",
                          "location": "Paris",
                          "totalBudget": "$1.000.000",
                          "startDate": "2024-04-19T00:00:00Z",
                          "endDate": "2024-04-22T00:00:00Z",
                          "links": [],
                          "sponsorIds": [],
                          "tracks": []
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
                            "subtitle": "subtitle",
                            "startDate": "2024-01-01T10:10:00Z",
                            "endDate": "2024-01-05T20:20:00Z"
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").value(hackathonId1::setValue)
                .json("""
                        {
                            "slug": "hackathon-2021",
                            "status": "DRAFT",
                            "title": "Hackathon 2021",
                            "subtitle": "subtitle",
                            "description": null,
                            "location": null,
                            "totalBudget": null,
                            "startDate": "2024-01-01T10:10:00Z",
                            "endDate": "2024-01-05T20:20:00Z",
                            "links": [],
                            "sponsors": [],
                            "tracks": []
                        }
                        """);
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
                            "subtitle": "subtitle",
                            "description": null,
                            "location": null,
                            "totalBudget": null,
                            "startDate": "2024-01-01T10:10:00Z",
                            "endDate": "2024-01-05T20:20:00Z",
                            "links": [],
                            "sponsors": [],
                            "tracks": []
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
                          "subtitle": "subtitle updated",
                          "description": "My hackathon description",
                          "location": "Paris",
                          "totalBudget": "$1.000.000",
                          "startDate": "2024-04-19T11:00:00Z",
                          "endDate": "2024-04-22T13:00:00Z",
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
                          "tracks": [
                            {
                              "name": "First track",
                              "subtitle": "First track subtitle",
                              "description": "First track description",
                              "iconSlug": "icon-1",
                              "projectIds": [
                                "8156fc5f-cec5-4f70-a0de-c368772edcd4",
                                "7ce1a761-2b7b-43ba-9eb5-17e95ef4aa54"
                              ]
                            },
                            {
                              "name": "Second track",
                              "subtitle": "Second track subtitle",
                              "description": "Second track description",
                              "iconSlug": "icon-2",
                              "projectIds": [
                                "c6940f66-d64e-4b29-9a7f-07abf5c3e0ed",
                                "7ce1a761-2b7b-43ba-9eb5-17e95ef4aa54"
                              ]
                            },
                            {
                              "name": "Third track",
                              "subtitle": "Third track subtitle",
                              "description": "Third track description",
                              "iconSlug": "icon-3",
                              "projectIds": []
                            }
                          ]
                        }
                        """)
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
                          "subtitle": "subtitle updated",
                          "description": "My hackathon description",
                          "location": "Paris",
                          "totalBudget": "$1.000.000",
                          "startDate": "2024-04-19T11:00:00Z",
                          "endDate": "2024-04-22T13:00:00Z",
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
                              "id": "0d66ba03-cecb-45a4-ab7d-98f0cc18a3aa",
                              "name": "Red Bull",
                              "url": "https://www.redbull.com/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13218160580172982881.jpg"
                            },
                            {
                              "id": "85435c9b-da7f-4670-bf65-02b84c5da7f0",
                              "name": "AS Nancy Lorraine",
                              "url": null,
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/951523516066154017.png"
                            }
                          ],
                          "tracks": [
                            {
                              "name": "First track",
                              "subtitle": "First track subtitle",
                              "description": "First track description",
                              "iconSlug": "icon-1",
                              "projects": [
                                {
                                  "id": "8156fc5f-cec5-4f70-a0de-c368772edcd4",
                                  "slug": "cairo-foundry",
                                  "name": "Cairo foundry",
                                  "logoUrl": null
                                },
                                {
                                  "id": "7ce1a761-2b7b-43ba-9eb5-17e95ef4aa54",
                                  "slug": "cairo-streams",
                                  "name": "Cairo streams",
                                  "logoUrl": null
                                }
                              ]
                            },
                            {
                              "name": "Second track",
                              "subtitle": "Second track subtitle",
                              "description": "Second track description",
                              "iconSlug": "icon-2",
                              "projects": [
                                {
                                  "id": "c6940f66-d64e-4b29-9a7f-07abf5c3e0ed",
                                  "slug": "red-bull",
                                  "name": "Red bull",
                                  "logoUrl": "https://cdn.filestackcontent.com/cZCHED10RzuEloOXuk7A"
                                },
                                {
                                  "id": "7ce1a761-2b7b-43ba-9eb5-17e95ef4aa54",
                                  "slug": "cairo-streams",
                                  "name": "Cairo streams",
                                  "logoUrl": null
                                }
                              ]
                            },
                            {
                              "name": "Third track",
                              "subtitle": "Third track subtitle",
                              "description": "Third track description",
                              "iconSlug": "icon-3",
                              "projects": []
                            }
                          ]
                        }
                        """);
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
                          "subtitle": "subtitle updated",
                          "description": "My hackathon description",
                          "location": "Paris",
                          "totalBudget": "$1.000.000",
                          "startDate": "2024-04-19T11:00:00Z",
                          "endDate": "2024-04-22T13:00:00Z",
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
                              "id": "0d66ba03-cecb-45a4-ab7d-98f0cc18a3aa",
                              "name": "Red Bull",
                              "url": "https://www.redbull.com/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13218160580172982881.jpg"
                            },
                            {
                              "id": "85435c9b-da7f-4670-bf65-02b84c5da7f0",
                              "name": "AS Nancy Lorraine",
                              "url": null,
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/951523516066154017.png"
                            }
                          ],
                          "tracks": [
                            {
                              "name": "First track",
                              "subtitle": "First track subtitle",
                              "description": "First track description",
                              "iconSlug": "icon-1",
                              "projects": [
                                {
                                  "id": "8156fc5f-cec5-4f70-a0de-c368772edcd4",
                                  "slug": "cairo-foundry",
                                  "name": "Cairo foundry",
                                  "logoUrl": null
                                },
                                {
                                  "id": "7ce1a761-2b7b-43ba-9eb5-17e95ef4aa54",
                                  "slug": "cairo-streams",
                                  "name": "Cairo streams",
                                  "logoUrl": null
                                }
                              ]
                            },
                            {
                              "name": "Second track",
                              "subtitle": "Second track subtitle",
                              "description": "Second track description",
                              "iconSlug": "icon-2",
                              "projects": [
                                {
                                  "id": "c6940f66-d64e-4b29-9a7f-07abf5c3e0ed",
                                  "slug": "red-bull",
                                  "name": "Red bull",
                                  "logoUrl": "https://cdn.filestackcontent.com/cZCHED10RzuEloOXuk7A"
                                },
                                {
                                  "id": "7ce1a761-2b7b-43ba-9eb5-17e95ef4aa54",
                                  "slug": "cairo-streams",
                                  "name": "Cairo streams",
                                  "logoUrl": null
                                }
                              ]
                            },
                            {
                              "name": "Third track",
                              "subtitle": "Third track subtitle",
                              "description": "Third track description",
                              "iconSlug": "icon-3",
                              "projects": []
                            }
                          ],
                          "registeredUsers": [
                              {
                                "githubUserId": 16590657,
                                "userId": "fc92397c-3431-4a84-8054-845376b630a0",
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
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
                          "title": "Hackathon 2021 updated",
                          "subtitle": "subtitle updated 2",
                          "description": "My hackathon description 2",
                          "location": "Paris 2",
                          "totalBudget": "$2.000.000",
                          "startDate": "2024-04-23T11:00:00Z",
                          "endDate": "2024-04-25T13:00:00Z",
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
                          "tracks": [
                            {
                              "name": "First track",
                              "subtitle": "First track subtitle",
                              "description": "First track description",
                              "iconSlug": "icon-1",
                              "projectIds": [
                                "2073b3b2-60f4-488c-8a0a-ab7121ed850c"
                              ]
                            }
                          ]
                        }
                        """)
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
                          "subtitle": "subtitle updated 2",
                          "description": "My hackathon description 2",
                          "location": "Paris 2",
                          "totalBudget": "$2.000.000",
                          "startDate": "2024-04-23T11:00:00Z",
                          "endDate": "2024-04-25T13:00:00Z",
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
                          "sponsors": [
                            {
                              "id": "44c6807c-48d1-4987-a0a6-ac63f958bdae",
                              "name": "Coca Colax",
                              "url": "https://www.coca-cola-france.fr/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/10299112926576087945.jpg"
                            }
                          ],
                          "tracks": [
                            {
                              "name": "First track",
                              "subtitle": "First track subtitle",
                              "description": "First track description",
                              "iconSlug": "icon-1",
                              "projects": [
                                {
                                  "id": "2073b3b2-60f4-488c-8a0a-ab7121ed850c",
                                  "slug": "apibara",
                                  "name": "Apibara",
                                  "logoUrl": null
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
                            "subtitle": "The best hackathon",
                            "startDate": "2024-06-01T00:00:00Z",
                            "endDate": "2024-06-05T00:00:00Z"
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").value(hackathonId2::setValue)
                .json("""
                        {
                            "slug": "od-hack",
                            "status": "DRAFT",
                            "title": "OD Hack",
                            "subtitle": "The best hackathon",
                            "description": null,
                            "location": null,
                            "totalBudget": null,
                            "startDate": "2024-06-01T00:00:00Z",
                            "endDate": "2024-06-05T00:00:00Z",
                            "links": [],
                            "sponsors": [],
                            "tracks": []
                        }
                        """);
        assertThat(hackathonId2.getValue()).isNotEmpty();
        assertThat(UUID.fromString(hackathonId2.getValue())).isNotNull();
        assertThat(hackathonId2.getValue()).isNotEqualTo(hackathonId1.getValue());
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
                              "slug": "hackathon-2021-updated",
                              "status": "PUBLISHED",
                              "title": "Hackathon 2021 updated",
                              "location": "Paris 2",
                              "startDate": "2024-04-23T11:00:00Z",
                              "endDate": "2024-04-25T13:00:00Z"
                            },
                            {
                              "slug": "od-hack",
                              "status": "DRAFT",
                              "title": "OD Hack",
                              "location": null,
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
                              "location": null,
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
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "slug": "od-hack",
                          "status": "PUBLISHED",
                          "title": "OD Hack",
                          "subtitle": "The best hackathon",
                          "description": null,
                          "location": null,
                          "totalBudget": null,
                          "startDate": "2024-06-01T00:00:00Z",
                          "endDate": "2024-06-05T00:00:00Z",
                          "links": [],
                          "sponsors": [],
                          "tracks": []
                        }
                        """);

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
                              "location": null,
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
                              "slug": "hackathon-2021-updated",
                              "status": "PUBLISHED",
                              "title": "Hackathon 2021 updated",
                              "location": "Paris 2",
                              "startDate": "2024-04-23T11:00:00Z",
                              "endDate": "2024-04-25T13:00:00Z"
                            }
                          ]
                        }
                        """);
    }
}
