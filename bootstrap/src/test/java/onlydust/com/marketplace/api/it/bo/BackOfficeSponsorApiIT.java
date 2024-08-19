package onlydust.com.marketplace.api.it.bo;

import onlydust.com.backoffice.api.contract.model.AccountResponse;
import onlydust.com.backoffice.api.contract.model.SponsorResponse;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectSponsorEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectSponsorRepository;
import onlydust.com.marketplace.api.suites.tags.TagBO;
import onlydust.com.marketplace.kernel.port.output.ImageStoragePort;
import onlydust.com.marketplace.user.domain.model.BackofficeUser;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static onlydust.com.marketplace.api.helper.CurrencyHelper.STRK;
import static onlydust.com.marketplace.api.it.bo.BackOfficeAccountingApiIT.BRETZEL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromResource;

@TagBO
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackOfficeSponsorApiIT extends AbstractMarketplaceBackOfficeApiIT {

    @Autowired
    ProjectSponsorRepository projectSponsorRepository;

    @Autowired
    private ImageStoragePort imageStoragePort;

    UserAuthHelper.AuthenticatedBackofficeUser pierre;

    @BeforeEach
    void setUp() {
        pierre = userAuthHelper.authenticateBackofficeUser("pierre.oucif@gadz.org", List.of(BackofficeUser.Role.BO_READER,
                BackofficeUser.Role.BO_FINANCIAL_ADMIN));
    }

    @Test
    @Order(1)
    void should_get_sponsor() {
        final String jwt = pierre.jwt();

        // When
        client.get()
                .uri(getApiURI(GET_SPONSOR.formatted("85435c9b-da7f-4670-bf65-02b84c5da7f0")))
                .header("Authorization", "Bearer " + jwt)
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
                          "availableBudgets": [
                            {
                              "currency": {
                                "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                "code": "USDC",
                                "name": "USD Coin",
                                "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                "decimals": 6
                              },
                              "initialBalance": 19823190,
                              "currentBalance": 19822690,
                              "initialAllowance": 19823190,
                              "currentAllowance": 0,
                              "debt": 0,
                              "awaitingPaymentAmount": 0,
                              "lockedAmounts": []
                            },
                            {
                              "currency": {
                                "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                                "code": "USD",
                                "name": "US Dollar",
                                "logoUrl": null,
                                "decimals": 2
                              },
                              "initialBalance": 4000,
                              "currentBalance": 0,
                              "initialAllowance": 4000,
                              "currentAllowance": 0,
                              "debt": 0,
                              "awaitingPaymentAmount": 0,
                              "lockedAmounts": []
                            }
                          ],
                          "projects": [
                            {
                              "id": "98873240-31df-431a-81dc-7d6fe01143a0",
                              "slug": "aiolia-du-lion",
                              "name": "Aiolia du Lion",
                              "logoUrl": "https://www.puregamemedia.fr/media/images/uploads/2019/11/ban_saint_seiya_awakening_kotz_aiolia_lion.jpg/?w=790&h=inherit&fm=webp&fit=contain&s=11e0e551affa5a88cc8c6de7f352449c",
                              "remainingBudgets": []
                            },
                            {
                              "id": "a0c91aee-9770-4000-a893-953ddcbd62a7",
                              "slug": "aldbaran-du-taureau",
                              "name": "Aldébaran du Taureau",
                              "logoUrl": "https://www.puregamemedia.fr/media/images/uploads/2019/11/ban_saint_seiya_awakening_kotz_aldebaran_taureau.jpg/?w=790&h=inherit&fm=webp&fit=contain&s=ab78704b124d2de9525a8af91ef7c4ed",
                              "remainingBudgets": [
                                {
                                  "amount": 19803190,
                                  "currency": {
                                    "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                    "code": "USDC",
                                    "name": "USD Coin",
                                    "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                    "decimals": 6
                                  }
                                }
                              ]
                            }
                          ]
                        }
                        """);
    }

    @Test
    @Order(2)
    void should_get_sponsors() {
        final String jwt = pierre.jwt();

        // When
        client.get()
                .uri(getApiURI(OLD_GET_SPONSORS, Map.of("pageIndex", "0", "pageSize", "5")))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 3,
                          "totalItemNumber": 13,
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
                                "98873240-31df-431a-81dc-7d6fe01143a0",
                                "7d04163c-4187-4313-8066-61504d34fc56"
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
                .uri(getApiURI(OLD_GET_SPONSORS, Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "projectIds", "467cb27c-9726-4f94-818e-6aa49bbf5e75,b0f54343-3732-4118-8054-dba40f1ffb85")
                ))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 3,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "sponsors": [
                            {
                              "id": "01bc5c57-9b7c-4521-b7be-8a12861ae5f4",
                              "name": "No Sponsor",
                              "url": null,
                              "logoUrl": "https://app.onlydust.com/_next/static/media/onlydust-logo.68e14357.webp",
                              "projectIds": [
                                "9fc2aba1-3e39-4e50-aca1-8e080135ed16",
                                "f39b827f-df73-498c-8853-99bc3f562723",
                                "02a533f5-6cbb-4cb6-90fe-f6bee220443c",
                                "57f76bd5-c6fb-4ef0-8a0a-74450f4ceca8",
                                "5aabf0f1-7495-4bff-8de2-4396837ce6b4",
                                "8daa34b4-563a-4ef5-8c1c-4bcffdfbc4f6",
                                "2724526e-3889-475c-a915-fc6c788a46e3",
                                "4f33a304-b4e9-42a2-a041-d0e359ede3bc",
                                "90fb751a-1137-4815-b3c4-54927a5db059",
                                "27ca7e18-9e71-468f-8825-c64fe6b79d66",
                                "29cdf359-f60c-41a0-8b11-18d6841311f6",
                                "247ac542-762d-44cb-b8d4-4d6199c916be",
                                "62565a49-67d8-45fa-9877-7ba8004b2db7",
                                "2073b3b2-60f4-488c-8a0a-ab7121ed850c",
                                "166b6ed6-2d71-44f9-bbf1-89ce82ec3e43",
                                "d1bfb3b0-b5e3-4c8c-9ffb-e74231c678b8",
                                "5bffd576-4139-4dee-a201-af671abb78a9",
                                "f25e3389-d681-4811-b45c-3d1106d8e478",
                                "d64341b6-2e95-43ba-946d-f6e2b094e7ed",
                                "25c125a2-5b99-4268-8a7c-2d8ed3f4a824",
                                "e4e9d711-5866-48b3-b2e0-14c48a2f9e12",
                                "8db522e0-9a4b-4d0a-9f6a-f009aaa546eb",
                                "7ce1a761-2b7b-43ba-9eb5-17e95ef4aa54",
                                "59696b39-6580-4542-86bd-ac8290572247",
                                "a852e8fd-de3c-4a14-813e-4b592af40d54",
                                "1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e",
                                "61ef7d3a-81a2-4baf-bdb0-e7ae5e165d17",
                                "dcb3548a-977a-480e-8fb4-423d3f890c04",
                                "3a1e0a11-634e-4bf1-a3ed-022ae68b6436",
                                "6eef8438-c538-4c10-9678-7dc82a8e550b",
                                "f992349c-e30c-4156-8b55-0a9dbc20b873",
                                "b7c97583-e2db-47b5-a0b4-88d2b3a59336",
                                "84f5ed22-93ec-4ee7-ba01-62d293837754",
                                "ccf90dcf-a91b-42c6-b5ca-49d687b4401a",
                                "dc60d963-4b5f-4a96-928c-8440b4657138",
                                "5e55d48f-93c2-4131-a455-fb3d30098c28",
                                "2a95f786-beb2-461d-b573-7150e4a1b65b",
                                "c137a92f-4bec-4159-ba35-331cfb3eb452",
                                "00490be6-2c03-4720-993b-aea3e07edd81",
                                "ade75c25-b39f-4fdf-a03a-e2391c1bc371",
                                "56504731-0398-441f-80ac-90edbd14675f",
                                "c6940f66-d64e-4b29-9a7f-07abf5c3e0ed",
                                "8156fc5f-cec5-4f70-a0de-c368772edcd4",
                                "105ef782-322b-414f-97ce-050880a9c526",
                                "c1d9a0d5-2c78-4ea5-89a7-9ae6a09b527a",
                                "d4e8ab3b-a4a8-493d-83bd-a4c8283b94f9",
                                "4f7bcc3e-3d3d-4a8f-8280-bb6df33382da",
                                "c44930eb-d292-4de0-99b3-85957e1a7a1a",
                                "b0f54343-3732-4118-8054-dba40f1ffb85",
                                "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                                "d658085e-7035-4e02-b0c7-67995920b437",
                                "b49ac452-bb04-4365-bb83-c3eb6fec2ed7",
                                "e41f44a2-464c-4c96-817f-81acb06b2523",
                                "6239cb20-eece-466a-80a0-742c1071dd3c",
                                "6d955622-c1ce-4227-85ea-51cb1b3207b1",
                                "c66b929a-664d-40b9-96c4-90d3efd32a3c"
                              ]
                            },
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


        client.get()
                .uri(getApiURI(OLD_GET_SPONSORS, Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "sponsorIds", "eb04a5de-4802-4071-be7b-9007b563d48d,2639563e-4437-4bde-a4f4-654977c0cb39")
                ))
                .header("Authorization", "Bearer " + jwt)
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
        final String jwt = pierre.jwt();

        // When
        client.post()
                .uri(getApiURI(POST_SPONSORS))
                .header("Authorization", "Bearer " + jwt)
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
                          "logoUrl": "https://www.foobar.com/logo.png"
                        }
                        """)
                .jsonPath("$.id").isNotEmpty();
    }

    @Test
    @Order(4)
    void should_update_sponsor() {
        final String jwt = pierre.jwt();

        // When
        client.put()
                .uri(getApiURI(PUT_SPONSORS.formatted("85435c9b-da7f-4670-bf65-02b84c5da7f0")))
                .header("Authorization", "Bearer " + jwt)
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
                          "logoUrl": "https://www.foobaaaar.com/logo.png"
                        }
                        """);
    }

    @Test
    @Order(5)
    void should_link_sponsor_to_project_on_allocation() {
        final String jwt = pierre.jwt();

        // Given
        final var sponsor = client.post()
                .uri(getApiURI(POST_SPONSORS))
                .header("Authorization", "Bearer " + jwt)
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
                .header("Authorization", "Bearer " + jwt)
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
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "name": "Virgin sponsor",
                          "projects": []
                        }
                        """);

        // And when
        client.post()
                .uri(getApiURI(POST_PROJECTS_BUDGETS_ALLOCATE.formatted(BRETZEL)))
                .header("Authorization", "Bearer " + jwt)
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
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "name": "Virgin sponsor",
                          "url": "https://www.foobar.com",
                          "logoUrl": "https://www.foobar.com/logo.png",
                          "availableBudgets": [
                            {
                              "currency": {
                                "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                                "code": "STRK",
                                "name": "StarkNet Token",
                                "logoUrl": null,
                                "decimals": 18
                              },
                              "initialBalance": 0,
                              "currentBalance": 0,
                              "initialAllowance": 1000,
                              "currentAllowance": 600,
                              "debt": 1000,
                              "awaitingPaymentAmount": 0,
                              "lockedAmounts": []
                            }
                          ],
                          "projects": [
                            {
                              "name": "Bretzel",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png",
                              "remainingBudgets": [
                                {
                                  "amount": 400,
                                  "currency": {
                                    "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                                    "code": "STRK",
                                    "name": "StarkNet Token",
                                    "logoUrl": null,
                                    "decimals": 18
                                  }
                                }
                              ]
                            }
                          ]
                        }
                        """);

        client.get()
                .uri(getApiURI(OLD_GET_SPONSORS, Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "sponsorIds", sponsor.getId().toString())))
                .header("Authorization", "Bearer " + jwt)
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
                              "logoUrl": "https://www.foobar.com/logo.png"
                            }
                          ]
                        }
                        """.formatted(sponsor.getId()));

        // And when
        projectSponsorRepository.save(new ProjectSponsorEntity(BRETZEL.value(), sponsorAccount.getSponsorId(),
                Date.from(ZonedDateTime.now().minusMonths(6).minusDays(1).toInstant())));


        client.get()
                .uri(getApiURI(GET_SPONSOR.formatted(sponsorAccount.getSponsorId())))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "name": "Virgin sponsor",
                          "projects": []
                        }
                        """);

        client.get()
                .uri(getApiURI(OLD_GET_SPONSORS, Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "sponsorIds", sponsor.getId().toString())))
                .header("Authorization", "Bearer " + jwt)
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
                              "logoUrl": "https://www.foobar.com/logo.png"
                            }
                          ]
                        }
                        """.formatted(sponsor.getId()));
    }

    @Test
    @Order(6)
    void should_get_sponsors_for_new_bo() {
        final String jwt = pierre.jwt();

        // When
        client.get()
                .uri(getApiURI(GET_SPONSORS, Map.of("pageIndex", "0", "pageSize", "10")))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 2,
                          "totalItemNumber": 15,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "sponsors": [
                            {
                              "id": "58a0a05c-c81e-447c-910f-629817a987b8",
                              "name": "Captain America",
                              "url": "https://www.marvel.com/characters/captain-america-steve-rogers",
                              "logoUrl": "https://www.ed92.org/wp-content/uploads/2021/06/captain-america-2-scaled.jpg",
                              "projects": [
                                {
                                  "name": "Marketplace",
                                  "logoUrl": null
                                }
                              ]
                            },
                            {
                              "id": "4202fd03-f316-458f-a642-421c7b3c7026",
                              "name": "ChatGPT",
                              "url": "https://chat.openai.com/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/4216570625498269873.png",
                              "projects": []
                            },
                            {
                              "id": "0980c5ab-befc-4314-acab-777fbf970cbb",
                              "name": "Coca Cola",
                              "url": null,
                              "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj",
                              "projects": [
                                {
                                  "name": "Aiolia du Lion",
                                  "logoUrl": "https://www.puregamemedia.fr/media/images/uploads/2019/11/ban_saint_seiya_awakening_kotz_aiolia_lion.jpg/?w=790&h=inherit&fm=webp&fit=contain&s=11e0e551affa5a88cc8c6de7f352449c"
                                },
                                {
                                  "name": "Bretzel",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                                }
                              ]
                            },
                            {
                              "id": "44c6807c-48d1-4987-a0a6-ac63f958bdae",
                              "name": "Coca Colax",
                              "url": "https://www.coca-cola-france.fr/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/10299112926576087945.jpg",
                              "projects": [
                                {
                                  "name": "Aiolia du Lion",
                                  "logoUrl": "https://www.puregamemedia.fr/media/images/uploads/2019/11/ban_saint_seiya_awakening_kotz_aiolia_lion.jpg/?w=790&h=inherit&fm=webp&fit=contain&s=11e0e551affa5a88cc8c6de7f352449c"
                                }
                              ]
                            },
                            {
                              "id": "6511500c-e6f2-41a4-9f8f-f15289969d09",
                              "name": "Creepy sponsor",
                              "url": "https://bretzel.club/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/10742744717192724286.jpg",
                              "projects": []
                            },
                            {
                              "id": "85435c9b-da7f-4670-bf65-02b84c5da7f0",
                              "name": "Foobaaaar",
                              "url": "https://www.foobaaaar.com",
                              "logoUrl": "https://www.foobaaaar.com/logo.png",
                              "projects": [
                                {
                                  "name": "Aiolia du Lion",
                                  "logoUrl": "https://www.puregamemedia.fr/media/images/uploads/2019/11/ban_saint_seiya_awakening_kotz_aiolia_lion.jpg/?w=790&h=inherit&fm=webp&fit=contain&s=11e0e551affa5a88cc8c6de7f352449c"
                                },
                                {
                                  "name": "Aldébaran du Taureau",
                                  "logoUrl": "https://www.puregamemedia.fr/media/images/uploads/2019/11/ban_saint_seiya_awakening_kotz_aldebaran_taureau.jpg/?w=790&h=inherit&fm=webp&fit=contain&s=ab78704b124d2de9525a8af91ef7c4ed"
                                }
                              ]
                            },
                            {
                              "name": "Foobar",
                              "url": "https://www.foobar.com",
                              "logoUrl": "https://www.foobar.com/logo.png",
                              "projects": []
                            },
                            {
                              "id": "01bc5c57-9b7c-4521-b7be-8a12861ae5f4",
                              "name": "No Sponsor",
                              "url": null,
                              "logoUrl": "https://app.onlydust.com/_next/static/media/onlydust-logo.68e14357.webp",
                              "projects": [
                                {
                                  "name": "kaaper",
                                  "logoUrl": null
                                },
                                {
                                  "name": "Zero title 2",
                                  "logoUrl": null
                                },
                                {
                                  "name": "Cairo foundry",
                                  "logoUrl": null
                                },
                                {
                                  "name": "The Ultimate project",
                                  "logoUrl": null
                                },
                                {
                                  "name": "Deluge",
                                  "logoUrl": null
                                },
                                {
                                  "name": "Zero title 15",
                                  "logoUrl": null
                                },
                                {
                                  "name": "Zero title 7",
                                  "logoUrl": null
                                },
                                {
                                  "name": "Zero title 6",
                                  "logoUrl": null
                                },
                                {
                                  "name": "Mû du Bélier",
                                  "logoUrl": "https://cdn.filestackcontent.com/llgXpbEzSMaSC1lpzTdj"
                                },
                                {
                                  "name": "My awesome project",
                                  "logoUrl": null
                                },
                                {
                                  "name": "Starklings",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13746458086965388437.jpg"
                                },
                                {
                                  "name": "Zero title 19",
                                  "logoUrl": null
                                },
                                {
                                  "name": "QA new contributions",
                                  "logoUrl": null
                                },
                                {
                                  "name": "Cairo streams",
                                  "logoUrl": null
                                },
                                {
                                  "name": "Zero title 4",
                                  "logoUrl": null
                                },
                                {
                                  "name": "Zero title 9",
                                  "logoUrl": null
                                },
                                {
                                  "name": "Zero title 1",
                                  "logoUrl": null
                                },
                                {
                                  "name": "starkonquest",
                                  "logoUrl": null
                                },
                                {
                                  "name": "Zero title 5",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/1458710211645943860.png"
                                },
                                {
                                  "name": "kaaper 3",
                                  "logoUrl": null
                                },
                                {
                                  "name": "Marketplace 2",
                                  "logoUrl": null
                                },
                                {
                                  "name": "Paris est Magique",
                                  "logoUrl": "https://cdn.filestackcontent.com/Qa94g2pVRDGgURSWz5zQ"
                                },
                                {
                                  "name": "Red bull",
                                  "logoUrl": "https://cdn.filestackcontent.com/cZCHED10RzuEloOXuk7A"
                                },
                                {
                                  "name": "Zama",
                                  "logoUrl": "https://dl.airtable.com/.attachments/f776b6ea66adbe46d86adaea58626118/610d50f6/15TqNyRwTMGoVeAX2u1M"
                                },
                                {
                                  "name": "Cal.com",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5271998260751715005.png"
                                },
                                {
                                  "name": "Greg's project",
                                  "logoUrl": "https://dl.airtable.com/.attachments/75bca1dce6735d434b19631814ec84b0/2a9cad0b/aeZxLjpJQre2uXBQDoQf"
                                },
                                {
                                  "name": "My super project",
                                  "logoUrl": "https://cdn.filestackcontent.com/n3AJNeS5OX5biqh1DnAx"
                                },
                                {
                                  "name": "OnlyDust Marketplace",
                                  "logoUrl": null
                                },
                                {
                                  "name": "Fresh",
                                  "logoUrl": null
                                },
                                {
                                  "name": "Milo du Scorpion",
                                  "logoUrl": "https://www.puregamemedia.fr/media/images/uploads/2019/10/ban_saint_seiya_awakening_kotz_milo_scorpion.jpg/?w=790&h=inherit&fm=webp&fit=contain&s=ab68737019442bf0718949f3ff9c779f"
                                },
                                {
                                  "name": "Poor Project",
                                  "logoUrl": null
                                },
                                {
                                  "name": "Apibara",
                                  "logoUrl": null
                                },
                                {
                                  "name": "DogGPT",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15366926246018901574.jpg"
                                },
                                {
                                  "name": "Zero title 10",
                                  "logoUrl": null
                                },
                                {
                                  "name": "Shura du Capricorne",
                                  "logoUrl": "https://www.puregamemedia.fr/media/images/uploads/2019/10/ban_saint_seiya_awakening_kotz_shaka_vierge.jpg/?w=790&h=inherit&fm=webp&fit=contain&s=c7c3ac0e384ecc21c9f2abc329ec9dfc"
                                },
                                {
                                  "name": "Zero title 13",
                                  "logoUrl": null
                                },
                                {
                                  "name": "Bretzel 196",
                                  "logoUrl": null
                                },
                                {
                                  "name": "kaaper2",
                                  "logoUrl": null
                                },
                                {
                                  "name": "Camus du Verseau",
                                  "logoUrl": "https://www.puregamemedia.fr/media/images/uploads/2019/10/ban_saint_seiya_awakening_kotz_saga_gemeaux.jpg/?w=790&h=inherit&fm=webp&fit=contain&s=dc04302de827b79180762741085d71ec"
                                },
                                {
                                  "name": "Zero title 16",
                                  "logoUrl": null
                                },
                                {
                                  "name": "Zero title 18",
                                  "logoUrl": null
                                },
                                {
                                  "name": "Zero title 8",
                                  "logoUrl": null
                                },
                                {
                                  "name": "oscar's awesome project",
                                  "logoUrl": null
                                },
                                {
                                  "name": "Zero title 20",
                                  "logoUrl": null
                                },
                                {
                                  "name": "Long project",
                                  "logoUrl": null
                                },
                                {
                                  "name": "Zero title 14",
                                  "logoUrl": null
                                },
                                {
                                  "name": "Pizzeria Yoshi !",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/14305950553200301786.png"
                                },
                                {
                                  "name": "Monday",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/17809999902614173931.png"
                                },
                                {
                                  "name": "Paco's project",
                                  "logoUrl": "https://dl.airtable.com/.attachments/01f2dd7497313a1fa13b4c5546429318/764531e3/8bUn9t8ORk6LLyMRcu78"
                                },
                                {
                                  "name": "Yolo croute",
                                  "logoUrl": "https://i.natgeofe.com/n/8271db90-5c35-46bc-9429-588a9529e44a/raccoon_thumb_3x4.JPG"
                                },
                                {
                                  "name": "Zero title 12",
                                  "logoUrl": null
                                },
                                {
                                  "name": "Deoxys",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/4391015415333593216.jpg"
                                },
                                {
                                  "name": "Zero title 17",
                                  "logoUrl": null
                                },
                                {
                                  "name": "No sponsors",
                                  "logoUrl": null
                                },
                                {
                                  "name": "Zero title 3",
                                  "logoUrl": null
                                },
                                {
                                  "name": "B Conseil",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11012050846615405488.png"
                                }
                              ]
                            },
                            {
                              "id": "c8dfb479-ee9d-4c16-b4b3-0ba39c2fdd6f",
                              "name": "OGC Nissa Ineos",
                              "url": "https://www.ogcnice.com/fr/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2946389705306833508.png",
                              "projects": [
                                {
                                  "name": "Aiolia du Lion",
                                  "logoUrl": "https://www.puregamemedia.fr/media/images/uploads/2019/11/ban_saint_seiya_awakening_kotz_aiolia_lion.jpg/?w=790&h=inherit&fm=webp&fit=contain&s=11e0e551affa5a88cc8c6de7f352449c"
                                },
                                {
                                  "name": "Aldébaran du Taureau",
                                  "logoUrl": "https://www.puregamemedia.fr/media/images/uploads/2019/11/ban_saint_seiya_awakening_kotz_aldebaran_taureau.jpg/?w=790&h=inherit&fm=webp&fit=contain&s=ab78704b124d2de9525a8af91ef7c4ed"
                                },
                                {
                                  "name": "Anthology project",
                                  "logoUrl": "https://cdn.filestackcontent.com/pgjvFWS8Teq2Yns89IKg"
                                },
                                {
                                  "name": "Bretzel",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                                }
                              ]
                            },
                            {
                              "id": "1774fd34-a8b6-43b0-b376-f2c2b256d478",
                              "name": "PSG",
                              "url": "https://www.psg.fr/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168095065030147290.png",
                              "projects": [
                                {
                                  "name": "toto",
                                  "logoUrl": null
                                }
                              ]
                            }
                          ]
                        }
                        """);

        // When
        client.get()
                .uri(getApiURI(GET_SPONSORS, Map.of("pageIndex", "1", "pageSize", "10")))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 2,
                          "totalItemNumber": 15,
                          "hasMore": false,
                          "nextPageIndex": 1,
                          "sponsors": [
                            {
                              "id": "0d66ba03-cecb-45a4-ab7d-98f0cc18a3aa",
                              "name": "Red Bull",
                              "url": "https://www.redbull.com/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13218160580172982881.jpg",
                              "projects": [
                                {
                                  "name": "Taco Tuesday",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/6987338668519888809.jpg"
                                }
                              ]
                            },
                            {
                              "id": "eb04a5de-4802-4071-be7b-9007b563d48d",
                              "name": "Starknet Foundation",
                              "url": "https://starknet.io",
                              "logoUrl": "https://logos-marques.com/wp-content/uploads/2020/09/Logo-Instagram-1.png",
                              "projects": [
                                {
                                  "name": "Zero title 11",
                                  "logoUrl": null
                                },
                                {
                                  "name": "Mooooooonlight",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/1913921207486176664.jpg"
                                }
                              ]
                            },
                            {
                              "id": "2639563e-4437-4bde-a4f4-654977c0cb39",
                              "name": "Theodo",
                              "url": null,
                              "logoUrl": "https://upload.wikimedia.org/wikipedia/fr/thumb/d/dd/Logo-theodo.png/1200px-Logo-theodo.png",
                              "projects": [
                                {
                                  "name": "Zero title 11",
                                  "logoUrl": null
                                },
                                {
                                  "name": "Mooooooonlight",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/1913921207486176664.jpg"
                                }
                              ]
                            },
                            {
                              "name": "Virgin sponsor",
                              "url": "https://www.foobar.com",
                              "logoUrl": "https://www.foobar.com/logo.png",
                              "projects": [
                                {
                                  "name": "Bretzel",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                                }
                              ]
                            },
                            {
                              "id": "9181a1b6-d4a4-4a23-9c5c-1d1386828bc1",
                              "name": "We are super sponsors!",
                              "url": null,
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/4216570625498269873.png",
                              "projects": []
                            }
                          ]
                        }
                        """);


    }

    @Test
    @Order(6)
    void should_get_sponsors_by_name_for_new_bo() {
        final String jwt = pierre.jwt();

        // When
        client.get()
                .uri(getApiURI(GET_SPONSORS, Map.of("pageIndex", "0", "pageSize", "10", "search", "coca")))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "sponsors": [
                            {
                              "name": "Coca Cola"
                            },
                            {
                              "name": "Coca Colax"
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_upload_sponsor_logo() throws MalformedURLException {
        final String jwt = pierre.jwt();

        when(imageStoragePort.storeImage(any(InputStream.class)))
                .thenReturn(new URL("https://s3.amazon.com/logo.jpeg"));

        client.post()
                .uri(getApiURI(SPONSORS_LOGO))
                .header("Authorization", "Bearer " + jwt)
                .body(fromResource(new FileSystemResource(getClass().getResource("/images/logo-sample.jpeg").getFile())))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.url").isEqualTo("https://s3.amazon.com/logo.jpeg");
    }

    @Test
    void should_add_and_get_sponsor_leads() {
        // Given
        final UUID readBull = UUID.fromString("0d66ba03-cecb-45a4-ab7d-98f0cc18a3aa");
        final UUID olivierId = UUID.fromString("e461c019-ba23-4671-9b6c-3a5a18748af9");
        final UUID anthoId = UUID.fromString("747e663f-4e68-4b42-965b-b5aebedcd4c4");

        // When
        client.put()
                .uri(getApiURI(SPONSORS_LEADS.formatted(readBull, olivierId)))
                .header("Authorization", "Bearer " + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        // When
        client.put()
                .uri(getApiURI(SPONSORS_LEADS.formatted(readBull, anthoId)))
                .header("Authorization", "Bearer " + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        // When
        client.get()
                .uri(getApiURI(GET_SPONSOR.formatted(readBull)))
                .header("Authorization", "Bearer " + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "leads": [
                            {
                              "githubUserId": 595505,
                              "userId": "e461c019-ba23-4671-9b6c-3a5a18748af9",
                              "login": "ofux",
                              "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp"
                            },
                            {
                              "githubUserId": 43467246,
                              "userId": "747e663f-4e68-4b42-965b-b5aebedcd4c4",
                              "login": "AnthonyBuisset",
                              "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp"
                            }
                          ]
                        }""");
    }
}
