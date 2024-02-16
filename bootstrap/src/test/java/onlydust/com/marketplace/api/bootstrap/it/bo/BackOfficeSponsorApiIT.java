package onlydust.com.marketplace.api.bootstrap.it.bo;

import onlydust.com.backoffice.api.contract.model.AccountResponse;
import onlydust.com.backoffice.api.contract.model.SponsorResponse;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectSponsorEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectSponsorRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

import static onlydust.com.marketplace.api.bootstrap.it.bo.BackOfficeAccountingApiIT.BRETZEL;
import static onlydust.com.marketplace.api.bootstrap.it.bo.BackOfficeAccountingApiIT.STRK;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackOfficeSponsorApiIT extends AbstractMarketplaceBackOfficeApiIT {

    @Autowired
    ProjectSponsorRepository projectSponsorRepository;

    @Test
    @Order(1)
    void should_get_sponsor() {
        // When
        client.get()
                .uri(getApiURI(GET_SPONSOR.formatted("85435c9b-da7f-4670-bf65-02b84c5da7f0")))
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "id": "85435c9b-da7f-4670-bf65-02b84c5da7f0",
                          "name": "AS Nancy Lorraine",
                          "url": null,
                          "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/951523516066154017.png",
                          "projectIds": [
                            "98873240-31df-431a-81dc-7d6fe01143a0",
                            "a0c91aee-9770-4000-a893-953ddcbd62a7"
                          ]
                        }
                        """);
    }


    @Test
    @Order(2)
    void should_get_sponsors() {
        // When
        client.get()
                .uri(getApiURI(GET_SPONSORS, Map.of("pageIndex", "0", "pageSize", "5")))
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 3,
                          "totalItemNumber": 12,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "sponsors": [
                            {
                              "id": "85435c9b-da7f-4670-bf65-02b84c5da7f0",
                              "name": "AS Nancy Lorraine",
                              "url": null,
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/951523516066154017.png",
                              "projectIds": [
                                "98873240-31df-431a-81dc-7d6fe01143a0",
                                "a0c91aee-9770-4000-a893-953ddcbd62a7"
                              ]
                            },
                            {
                              "id": "58a0a05c-c81e-447c-910f-629817a987b8",
                              "name": "Captain America",
                              "url": "https://www.marvel.com/characters/captain-america-steve-rogers",
                              "logoUrl": "https://www.ed92.org/wp-content/uploads/2021/06/captain-america-2-scaled.jpg",
                              "projectIds": [
                                "45ca43d6-130e-4bf7-9776-2b1eb1dcb782"
                              ]
                            },
                            {
                              "id": "4202fd03-f316-458f-a642-421c7b3c7026",
                              "name": "ChatGPT",
                              "url": "https://chat.openai.com/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/4216570625498269873.png",
                              "projectIds": []
                            },
                            {
                              "id": "0980c5ab-befc-4314-acab-777fbf970cbb",
                              "name": "Coca Cola",
                              "url": null,
                              "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj",
                              "projectIds": [
                                "7d04163c-4187-4313-8066-61504d34fc56",
                                "98873240-31df-431a-81dc-7d6fe01143a0"
                              ]
                            },
                            {
                              "id": "44c6807c-48d1-4987-a0a6-ac63f958bdae",
                              "name": "Coca Colax",
                              "url": "https://www.coca-cola-france.fr/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/10299112926576087945.jpg",
                              "projectIds": [
                                "98873240-31df-431a-81dc-7d6fe01143a0"
                              ]
                            }
                          ]
                        }
                                                
                        """);

        client.get()
                .uri(getApiURI(GET_SPONSORS, Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "projectIds", "467cb27c-9726-4f94-818e-6aa49bbf5e75,b0f54343-3732-4118-8054-dba40f1ffb85")
                ))
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "sponsors": [
                            {
                              "id": "eb04a5de-4802-4071-be7b-9007b563d48d",
                              "name": "Starknet Foundation",
                              "url": "https://starknet.io",
                              "logoUrl": "https://logos-marques.com/wp-content/uploads/2020/09/Logo-Instagram-1.png",
                              "projectIds": [
                                "594ca5ca-48f7-49a8-9c26-84b949d4fdd9",
                                "467cb27c-9726-4f94-818e-6aa49bbf5e75"
                              ]
                            },
                            {
                              "id": "2639563e-4437-4bde-a4f4-654977c0cb39",
                              "name": "Theodo",
                              "url": null,
                              "logoUrl": "https://upload.wikimedia.org/wikipedia/fr/thumb/d/dd/Logo-theodo.png/1200px-Logo-theodo.png",
                              "projectIds": [
                                "594ca5ca-48f7-49a8-9c26-84b949d4fdd9",
                                "467cb27c-9726-4f94-818e-6aa49bbf5e75"
                              ]
                            }
                          ]
                        }
                                                
                        """);


        client.get()
                .uri(getApiURI(GET_SPONSORS, Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "sponsorIds", "eb04a5de-4802-4071-be7b-9007b563d48d,2639563e-4437-4bde-a4f4-654977c0cb39")
                ))
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                               "totalPageNumber": 1,
                               "totalItemNumber": 2,
                               "hasMore": false,
                               "nextPageIndex": 0,
                               "sponsors": [
                                 {
                                   "id": "eb04a5de-4802-4071-be7b-9007b563d48d",
                                   "name": "Starknet Foundation",
                                   "url": "https://starknet.io",
                                   "logoUrl": "https://logos-marques.com/wp-content/uploads/2020/09/Logo-Instagram-1.png",
                                   "projectIds": [
                                     "467cb27c-9726-4f94-818e-6aa49bbf5e75",
                                     "594ca5ca-48f7-49a8-9c26-84b949d4fdd9"
                                   ]
                                 },
                                 {
                                   "id": "2639563e-4437-4bde-a4f4-654977c0cb39",
                                   "name": "Theodo",
                                   "url": null,
                                   "logoUrl": "https://upload.wikimedia.org/wikipedia/fr/thumb/d/dd/Logo-theodo.png/1200px-Logo-theodo.png",
                                   "projectIds": [
                                     "467cb27c-9726-4f94-818e-6aa49bbf5e75",
                                     "594ca5ca-48f7-49a8-9c26-84b949d4fdd9"
                                   ]
                                 }
                               ]
                             }
                        """);
    }

    @Test
    @Order(3)
    void should_create_sponsor() {
        // When
        client.post()
                .uri(getApiURI(POST_SPONSORS))
                .header("Api-Key", apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Foobar",
                          "url": "https://www.foobar.com",
                          "logoUrl": "https://www.foobar.com/logo.png"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "name": "Foobar",
                          "url": "https://www.foobar.com",
                          "logoUrl": "https://www.foobar.com/logo.png",
                          "projectIds": []
                        }
                        """)
                .jsonPath("$.id").isNotEmpty();
    }

    @Test
    @Order(4)
    void should_update_sponsor() {
        // When
        client.put()
                .uri(getApiURI(PUT_SPONSORS.formatted("85435c9b-da7f-4670-bf65-02b84c5da7f0")))
                .header("Api-Key", apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Foobaaaar",
                          "url": "https://www.foobaaaar.com",
                          "logoUrl": "https://www.foobaaaar.com/logo.png"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "id": "85435c9b-da7f-4670-bf65-02b84c5da7f0",
                          "name": "Foobaaaar",
                          "url": "https://www.foobaaaar.com",
                          "logoUrl": "https://www.foobaaaar.com/logo.png",
                          "projectIds": [
                            "98873240-31df-431a-81dc-7d6fe01143a0",
                            "a0c91aee-9770-4000-a893-953ddcbd62a7"
                          ]
                        }
                        """);
    }

    @Test
    @Order(5)
    void should_link_sponsor_to_project_on_allocation() {
        // Given
        final var sponsor = client.post()
                .uri(getApiURI(POST_SPONSORS))
                .header("Api-Key", apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Virgin sponsor",
                          "url": "https://www.foobar.com",
                          "logoUrl": "https://www.foobar.com/logo.png"
                        }
                        """)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(SponsorResponse.class)
                .returnResult().getResponseBody();

        final var sponsorAccount = client.post()
                .uri(getApiURI(POST_SPONSORS_ACCOUNTS.formatted(sponsor.getId())))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "currencyId": "%s",
                            "allowance": 1000
                        }
                        """.formatted(STRK))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(AccountResponse.class)
                .returnResult()
                .getResponseBody();

        // Then
        client.get()
                .uri(getApiURI(GET_SPONSOR.formatted(sponsorAccount.getSponsorId())))
                .header("Api-Key", apiKey())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "name": "Virgin sponsor",
                          "projectIds": []
                        }
                        """);

        // And when
        client.post()
                .uri(getApiURI(POST_PROJECTS_BUDGETS_ALLOCATE.formatted(BRETZEL)))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "sponsorAccountId": "%s",
                            "amount": 400
                        }
                        """.formatted(sponsorAccount.getId()))
                .exchange()
                .expectStatus()
                .isNoContent();

        client.get()
                .uri(getApiURI(GET_SPONSOR.formatted(sponsorAccount.getSponsorId())))
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "name": "Virgin sponsor",
                          "projectIds": [
                            "%s"
                          ]
                        }
                        """.formatted(BRETZEL));

        client.get()
                .uri(getApiURI(GET_SPONSORS, Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "sponsorIds", sponsor.getId().toString())))
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 1,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "sponsors": [
                            {
                              "id": "%s",
                              "name": "Virgin sponsor",
                              "url": "https://www.foobar.com",
                              "logoUrl": "https://www.foobar.com/logo.png",
                              "projectIds": [
                                "7d04163c-4187-4313-8066-61504d34fc56"
                              ]
                            }
                          ]
                        }
                        """.formatted(sponsor.getId()));

        // And when
        projectSponsorRepository.save(new ProjectSponsorEntity(BRETZEL.value(), sponsorAccount.getSponsorId(),
                Date.from(ZonedDateTime.now().minusMonths(6).minusDays(1).toInstant())));


        client.get()
                .uri(getApiURI(GET_SPONSOR.formatted(sponsorAccount.getSponsorId())))
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "name": "Virgin sponsor",
                          "projectIds": []
                        }
                        """);

        client.get()
                .uri(getApiURI(GET_SPONSORS, Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "sponsorIds", sponsor.getId().toString())))
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 1,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "sponsors": [
                            {
                              "id": "%s",
                              "name": "Virgin sponsor",
                              "url": "https://www.foobar.com",
                              "logoUrl": "https://www.foobar.com/logo.png",
                              "projectIds": []
                            }
                          ]
                        }
                        """.formatted(sponsor.getId()));
    }
}
