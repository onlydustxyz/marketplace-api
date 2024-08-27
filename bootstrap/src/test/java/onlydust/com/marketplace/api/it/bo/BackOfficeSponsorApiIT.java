package onlydust.com.marketplace.api.it.bo;

import onlydust.com.backoffice.api.contract.model.AllocationRequest;
import onlydust.com.backoffice.api.contract.model.CreateAccountRequest;
import onlydust.com.backoffice.api.contract.model.SponsorRequest;
import onlydust.com.backoffice.api.contract.model.SponsorResponse;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagBO;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.port.output.ImageStoragePort;
import onlydust.com.marketplace.user.domain.model.BackofficeUser;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.testcontainers.shaded.org.apache.commons.lang3.mutable.MutableObject;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static onlydust.com.marketplace.api.helper.CurrencyHelper.STRK;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromResource;

@TagBO
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackOfficeSponsorApiIT extends AbstractMarketplaceBackOfficeApiIT {

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
                          "programs": [
                            {
                              "name": "AS Nancy Lorraine",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/951523516066154017.png",
                              "remainingBudgets": [
                                {
                                  "amount": 0,
                                  "currency": {
                                    "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                    "code": "USDC",
                                    "name": "USD Coin",
                                    "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                    "decimals": 6
                                  }
                                },
                                {
                                  "amount": 0,
                                  "currency": {
                                    "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                                    "code": "USD",
                                    "name": "US Dollar",
                                    "logoUrl": null,
                                    "decimals": 2
                                  }
                                }
                              ]
                            }
                          ],
                          "leads": []
                        }
                        """);
    }

    @Test
    @Order(3)
    void should_create_sponsor() {
        final var jwt = pierre.jwt();
        final var sponsorId = new MutableObject<String>();

        // When
        client.post()
                .uri(getApiURI(POST_SPONSORS))
                .header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new SponsorRequest()
                        .name("Foobar")
                        .url(URI.create("https://www.foobar.com"))
                        .logoUrl(URI.create("https://www.foobar.com/logo.png"))
                        .leads(List.of(userAuthHelper.authenticatePierre().user().getId()))
                )
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").value(sponsorId::setValue);

        client.get()
                .uri(getApiURI(GET_SPONSOR.formatted(sponsorId.getValue())))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isEqualTo(sponsorId.getValue())
                .json("""
                        {
                          "name": "Foobar",
                          "url": "https://www.foobar.com",
                          "logoUrl": "https://www.foobar.com/logo.png",
                          "availableBudgets": [],
                          "programs": [],
                          "leads": [
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
    @Order(4)
    void should_update_sponsor() {
        final String jwt = pierre.jwt();

        // When
        client.put()
                .uri(getApiURI(PUT_SPONSORS.formatted("85435c9b-da7f-4670-bf65-02b84c5da7f0")))
                .header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new SponsorRequest()
                        .name("Foobaaaar")
                        .url(URI.create("https://www.foobaaaar.com"))
                        .logoUrl(URI.create("https://www.foobaaaar.com/logo.png"))
                        .leads(List.of(userAuthHelper.authenticateAntho().user().getId()))
                )
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

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
                          "name": "Foobaaaar",
                          "url": "https://www.foobaaaar.com",
                          "logoUrl": "https://www.foobaaaar.com/logo.png",
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
                          "programs": [
                            {
                              "id": "4b1cbc61-045c-4229-8378-8ad50c2aca78",
                              "name": "AS Nancy Lorraine",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/951523516066154017.png",
                              "remainingBudgets": [
                                {
                                  "amount": 0,
                                  "currency": {
                                    "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                                    "code": "USD",
                                    "name": "US Dollar",
                                    "logoUrl": null,
                                    "decimals": 2
                                  }
                                },
                                {
                                  "amount": 0,
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
                          ],
                          "leads": [
                            {
                              "githubUserId": 43467246,
                              "userId": "747e663f-4e68-4b42-965b-b5aebedcd4c4",
                              "login": "AnthonyBuisset",
                              "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp"
                            }
                          ]
                        }
                        """);
    }

    @Test
    @Order(5)
    void should_link_sponsor_to_program_upon_allocation() {
        final var jwt = pierre.jwt();
        final var program = programHelper.create();
        final var BRETZEL = ProjectId.of("7d04163c-4187-4313-8066-61504d34fc56");

        // Given
        final var sponsor = client.post()
                .uri(getApiURI(POST_SPONSORS))
                .header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Virgin sponsor",
                          "url": "https://www.foobar.com",
                          "logoUrl": "https://www.foobar.com/logo.png",
                          "leads": []
                        }
                        """)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(SponsorResponse.class)
                .returnResult().getResponseBody();

        client.post()
                .uri(getApiURI(POST_SPONSORS_ACCOUNTS.formatted(sponsor.getId())))
                .header("Authorization", "Bearer " + jwt)
                .contentType(APPLICATION_JSON)
                .bodyValue(new CreateAccountRequest()
                        .currencyId(STRK.value())
                        .allowance(BigDecimal.valueOf(1000)))
                .exchange()
                .expectStatus()
                .isOk();

        // And when
        client.post()
                .uri(getApiURI(SPONSORS_BY_ID_ALLOCATE.formatted(sponsor.getId())))
                .header("Authorization", "Bearer " + jwt)
                .contentType(APPLICATION_JSON)
                .bodyValue(new AllocationRequest()
                        .amount(BigDecimal.valueOf(400))
                        .currencyId(STRK.value())
                        .programId(program.id().value()))
                .exchange()
                .expectStatus()
                .isNoContent();

        client.get()
                .uri(getApiURI(GET_SPONSOR.formatted(sponsor.getId())))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.availableBudgets.length()").isEqualTo(1)
                .jsonPath("$.availableBudgets[0].currency.id").isEqualTo(STRK.value().toString())
                .jsonPath("$.availableBudgets[0].initialBalance").isEqualTo(0)
                .jsonPath("$.availableBudgets[0].currentBalance").isEqualTo(0)
                .jsonPath("$.availableBudgets[0].initialAllowance").isEqualTo(1000)
                .jsonPath("$.availableBudgets[0].currentAllowance").isEqualTo(600)
                .jsonPath("$.availableBudgets[0].debt").isEqualTo(1000)
                .jsonPath("$.availableBudgets[0].awaitingPaymentAmount").isEqualTo(0)
                .jsonPath("$.programs.length()").isEqualTo(1)
                .jsonPath("$.programs[0].remainingBudgets.length()").isEqualTo(1)
                .jsonPath("$.programs[0].remainingBudgets[0].currency.id").isEqualTo(STRK.value().toString())
                .jsonPath("$.programs[0].remainingBudgets[0].amount").isEqualTo(400);

        // And when
        accountingHelper.grant(program.id(), BRETZEL, 400L, STRK);

        client.get()
                .uri(getApiURI(GET_SPONSOR.formatted(sponsor.getId())))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.availableBudgets.length()").isEqualTo(1)
                .jsonPath("$.availableBudgets[0].currency.id").isEqualTo(STRK.value().toString())
                .jsonPath("$.availableBudgets[0].initialBalance").isEqualTo(0)
                .jsonPath("$.availableBudgets[0].currentBalance").isEqualTo(0)
                .jsonPath("$.availableBudgets[0].initialAllowance").isEqualTo(1000)
                .jsonPath("$.availableBudgets[0].currentAllowance").isEqualTo(600)
                .jsonPath("$.availableBudgets[0].debt").isEqualTo(1000)
                .jsonPath("$.availableBudgets[0].awaitingPaymentAmount").isEqualTo(0)
                .jsonPath("$.programs.length()").isEqualTo(1)
                .jsonPath("$.programs[0].remainingBudgets.length()").isEqualTo(1)
                .jsonPath("$.programs[0].remainingBudgets[0].currency.id").isEqualTo(STRK.value().toString())
                .jsonPath("$.programs[0].remainingBudgets[0].amount").isEqualTo(0);
    }

    @Test
    @Order(6)
    void should_get_sponsors() {
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
                              "programs": [
                                {
                                  "name": "Captain America",
                                  "logoUrl": "https://www.ed92.org/wp-content/uploads/2021/06/captain-america-2-scaled.jpg"
                                }
                              ],
                              "leads": []
                            },
                            {
                              "id": "4202fd03-f316-458f-a642-421c7b3c7026",
                              "name": "ChatGPT",
                              "url": "https://chat.openai.com/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/4216570625498269873.png",
                              "programs": [],
                              "leads": []
                            },
                            {
                              "id": "0980c5ab-befc-4314-acab-777fbf970cbb",
                              "name": "Coca Cola",
                              "url": null,
                              "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj",
                              "programs": [
                                {
                                  "name": "Coca Cola",
                                  "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj"
                                }
                              ],
                              "leads": []
                            },
                            {
                              "id": "44c6807c-48d1-4987-a0a6-ac63f958bdae",
                              "name": "Coca Colax",
                              "url": "https://www.coca-cola-france.fr/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/10299112926576087945.jpg",
                              "programs": [],
                              "leads": []
                            },
                            {
                              "id": "6511500c-e6f2-41a4-9f8f-f15289969d09",
                              "name": "Creepy sponsor",
                              "url": "https://bretzel.club/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/10742744717192724286.jpg",
                              "programs": [],
                              "leads": []
                            },
                            {
                              "id": "85435c9b-da7f-4670-bf65-02b84c5da7f0",
                              "name": "Foobaaaar",
                              "url": "https://www.foobaaaar.com",
                              "logoUrl": "https://www.foobaaaar.com/logo.png",
                              "programs": [
                                {
                                  "name": "AS Nancy Lorraine",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/951523516066154017.png"
                                }
                              ],
                              "leads": [
                                {
                                  "githubUserId": 43467246,
                                  "userId": "747e663f-4e68-4b42-965b-b5aebedcd4c4",
                                  "login": "AnthonyBuisset",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp"
                                }
                              ]
                            },
                            {
                              "name": "Foobar",
                              "url": "https://www.foobar.com",
                              "logoUrl": "https://www.foobar.com/logo.png",
                              "programs": [],
                              "leads": [
                                {
                                  "githubUserId": 16590657,
                                  "userId": "fc92397c-3431-4a84-8054-845376b630a0",
                                  "login": "PierreOucif",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                                }
                              ]
                            },
                            {
                              "id": "01bc5c57-9b7c-4521-b7be-8a12861ae5f4",
                              "name": "No Sponsor",
                              "url": null,
                              "logoUrl": "https://app.onlydust.com/_next/static/media/onlydust-logo.68e14357.webp",
                              "programs": [
                                {
                                  "name": "No Sponsor",
                                  "logoUrl": "https://app.onlydust.com/_next/static/media/onlydust-logo.68e14357.webp"
                                }
                              ],
                              "leads": []
                            },
                            {
                              "id": "c8dfb479-ee9d-4c16-b4b3-0ba39c2fdd6f",
                              "name": "OGC Nissa Ineos",
                              "url": "https://www.ogcnice.com/fr/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2946389705306833508.png",
                              "programs": [
                                {
                                  "name": "OGC Nissa Ineos",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2946389705306833508.png"
                                }
                              ],
                              "leads": []
                            },
                            {
                              "id": "1774fd34-a8b6-43b0-b376-f2c2b256d478",
                              "name": "PSG",
                              "url": "https://www.psg.fr/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168095065030147290.png",
                              "programs": [
                                {
                                  "name": "PSG",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168095065030147290.png"
                                }
                              ],
                              "leads": []
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
                              "name": "Red Bull",
                              "url": "https://www.redbull.com/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13218160580172982881.jpg",
                              "programs": [
                                {
                                  "name": "Red Bull",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13218160580172982881.jpg"
                                }
                              ]
                            },
                            {
                              "name": "Starknet Foundation",
                              "url": "https://starknet.io",
                              "logoUrl": "https://logos-marques.com/wp-content/uploads/2020/09/Logo-Instagram-1.png",
                              "programs": []
                            },
                            {
                              "name": "Theodo",
                              "url": null,
                              "logoUrl": "https://upload.wikimedia.org/wikipedia/fr/thumb/d/dd/Logo-theodo.png/1200px-Logo-theodo.png",
                              "programs": [
                                {
                                  "name": "Theodo",
                                  "logoUrl": "https://upload.wikimedia.org/wikipedia/fr/thumb/d/dd/Logo-theodo.png/1200px-Logo-theodo.png"
                                }
                              ]
                            },
                            {
                              "name": "Virgin sponsor",
                              "url": "https://www.foobar.com",
                              "logoUrl": "https://www.foobar.com/logo.png",
                              "programs": [
                                {
                                }
                              ]
                            },
                            {
                              "name": "We are super sponsors!",
                              "url": null,
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/4216570625498269873.png",
                              "programs": []
                            }
                          ]
                        }
                        """);
    }

    @Test
    @Order(6)
    void should_get_sponsors_by_name() {
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
                              "name": "Coca Cola",
                              "url": null,
                              "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj",
                              "programs": [
                                {
                                  "id": "451e5b61-c340-4f85-905c-da4332c968ed",
                                  "name": "Coca Cola",
                                  "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj"
                                }
                              ],
                              "leads": []
                            },
                            {
                              "name": "Coca Colax",
                              "url": "https://www.coca-cola-france.fr/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/10299112926576087945.jpg",
                              "programs": [],
                              "leads": []
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
