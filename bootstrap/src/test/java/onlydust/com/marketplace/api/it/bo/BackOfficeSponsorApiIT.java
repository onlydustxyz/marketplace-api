package onlydust.com.marketplace.api.it.bo;

import onlydust.com.backoffice.api.contract.model.SponsorRequest;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagBO;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.kernel.port.output.ImageStoragePort;
import onlydust.com.marketplace.user.domain.model.BackofficeUser;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.testcontainers.shaded.org.apache.commons.lang3.mutable.MutableObject;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
    @Order(6)
    void should_get_sponsors() {
        // Given
        final String jwt = pierre.jwt();
        depositHelper.preview(SponsorId.of("58a0a05c-c81e-447c-910f-629817a987b8"), Network.ETHEREUM);
        depositHelper.preview(SponsorId.of("4202fd03-f316-458f-a642-421c7b3c7026"), Network.ETHEREUM);
        depositHelper.preview(SponsorId.of("4202fd03-f316-458f-a642-421c7b3c7026"), Network.APTOS);
        depositHelper.preview(SponsorId.of("0980c5ab-befc-4314-acab-777fbf970cbb"), Network.STELLAR);
        depositHelper.preview(SponsorId.of("0980c5ab-befc-4314-acab-777fbf970cbb"), Network.OPTIMISM);
        depositHelper.preview(SponsorId.of("0980c5ab-befc-4314-acab-777fbf970cbb"), Network.STARKNET);

        // When
        client.get()
                .uri(getApiURI(GET_SPONSORS, Map.of("pageIndex", "0", "pageSize", "10")))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .json("""
                        {
                          "totalPageNumber": 2,
                          "totalItemNumber": 14,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "sponsors": [
                            {
                              "name": "Captain America",
                              "url": "https://www.marvel.com/characters/captain-america-steve-rogers",
                              "logoUrl": "https://www.ed92.org/wp-content/uploads/2021/06/captain-america-2-scaled.jpg",
                              "programs": [
                                {
                                  "id": "e919b1ff-c666-4dd9-99bc-522ceb5fde7a",
                                  "name": "Captain America",
                                  "logoUrl": "https://www.ed92.org/wp-content/uploads/2021/06/captain-america-2-scaled.jpg"
                                }
                              ],
                              "leads": []
                            },
                            {
                              "name": "ChatGPT",
                              "url": "https://chat.openai.com/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/4216570625498269873.png",
                              "programs": [
                                {
                                  "id": "04315017-a917-4f60-93c5-3b9f0dbe3af4",
                                  "name": "ChatGPT",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/4216570625498269873.png"
                                }
                              ],
                              "leads": []
                            },
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
                              "programs": [
                                {
                                  "id": "a0d5edba-0d1d-4bd2-8bca-2656b86a03f6",
                                  "name": "Coca Colax",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/10299112926576087945.jpg"
                                }
                              ],
                              "leads": []
                            },
                            {
                              "name": "Creepy sponsor",
                              "url": "https://bretzel.club/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/10742744717192724286.jpg",
                              "programs": [
                                {
                                  "id": "e92a5a4e-8730-4a30-adfb-aa78fd04471e",
                                  "name": "Creepy sponsor",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/10742744717192724286.jpg"
                                }
                              ],
                              "leads": []
                            },
                            {
                              "name": "Foobaaaar",
                              "url": "https://www.foobaaaar.com",
                              "logoUrl": "https://www.foobaaaar.com/logo.png",
                              "programs": [
                                {
                                  "id": "4b1cbc61-045c-4229-8378-8ad50c2aca78",
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
                              "name": "No Sponsor",
                              "url": null,
                              "logoUrl": "https://app.onlydust.com/_next/static/media/onlydust-logo.68e14357.webp",
                              "programs": [
                                {
                                  "id": "6d13685b-2853-438e-bf78-b7d4a37adc70",
                                  "name": "No Sponsor",
                                  "logoUrl": "https://app.onlydust.com/_next/static/media/onlydust-logo.68e14357.webp"
                                }
                              ],
                              "leads": []
                            },
                            {
                              "name": "OGC Nissa Ineos",
                              "url": "https://www.ogcnice.com/fr/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2946389705306833508.png",
                              "programs": [
                                {
                                  "id": "a572bf50-3b2f-4008-8a98-f62de1acd4eb",
                                  "name": "OGC Nissa Ineos",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2946389705306833508.png"
                                }
                              ],
                              "leads": []
                            },
                            {
                              "name": "PSG",
                              "url": "https://www.psg.fr/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168095065030147290.png",
                              "programs": [
                                {
                                  "id": "a606447e-89a7-4e76-92e8-a5b020f83420",
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
                          "totalItemNumber": 14,
                          "hasMore": false,
                          "nextPageIndex": 1,
                          "sponsors": [
                            {
                              "name": "Red Bull",
                              "url": "https://www.redbull.com/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13218160580172982881.jpg",
                              "programs": [
                                {
                                  "id": "4ffb631f-e850-46ef-9171-894f5b0f5f2c",
                                  "name": "Red Bull",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13218160580172982881.jpg"
                                }
                              ],
                              "leads": []
                            },
                            {
                              "name": "Starknet Foundation",
                              "url": "https://starknet.io",
                              "logoUrl": "https://logos-marques.com/wp-content/uploads/2020/09/Logo-Instagram-1.png",
                              "programs": [
                                {
                                  "id": "54a7604d-e7c8-4d4b-a186-9ad7fd31e4a6",
                                  "name": "Starknet Foundation",
                                  "logoUrl": "https://logos-marques.com/wp-content/uploads/2020/09/Logo-Instagram-1.png"
                                }
                              ],
                              "leads": []
                            },
                            {
                              "name": "Theodo",
                              "url": null,
                              "logoUrl": "https://upload.wikimedia.org/wikipedia/fr/thumb/d/dd/Logo-theodo.png/1200px-Logo-theodo.png",
                              "programs": [
                                {
                                  "id": "05ad97ac-11c4-40fa-9a35-7564120e2706",
                                  "name": "Theodo",
                                  "logoUrl": "https://upload.wikimedia.org/wikipedia/fr/thumb/d/dd/Logo-theodo.png/1200px-Logo-theodo.png"
                                }
                              ],
                              "leads": []
                            },
                            {
                              "name": "We are super sponsors!",
                              "url": null,
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/4216570625498269873.png",
                              "programs": [
                                {
                                  "id": "9f9a8397-802a-4f60-8db9-34264b64d747",
                                  "name": "We are super sponsors!",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/4216570625498269873.png"
                                }
                              ],
                              "leads": []
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
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
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
                              "programs": [
                                {
                                  "id": "a0d5edba-0d1d-4bd2-8bca-2656b86a03f6",
                                  "name": "Coca Colax",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/10299112926576087945.jpg"
                                }
                              ],
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
