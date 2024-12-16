package onlydust.com.marketplace.api.it.bo;

import lombok.SneakyThrows;
import onlydust.com.backoffice.api.contract.model.SponsorRequest;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagBO;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.kernel.port.output.ImageStoragePort;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import onlydust.com.marketplace.project.domain.model.notification.DepositApproved;
import onlydust.com.marketplace.project.domain.model.notification.DepositRejected;
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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.math.BigDecimal.TEN;
import static onlydust.com.marketplace.accounting.domain.model.Deposit.Status.*;
import static onlydust.com.marketplace.accounting.domain.model.Network.*;
import static onlydust.com.marketplace.api.helper.CurrencyHelper.USDC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.*;
import static org.springframework.web.reactive.function.BodyInserters.fromResource;

@TagBO
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackOfficeSponsorApiIT extends AbstractMarketplaceBackOfficeApiIT {

    @Autowired
    private ImageStoragePort imageStoragePort;

    @Autowired
    private NotificationPort notificationPort;

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
    @Order(1)
    void should_get_program() {
        final String jwt = pierre.jwt();

        // When
        client.get()
                .uri(getApiURI(PROGRAMS_BY_ID.formatted("4b1cbc61-045c-4229-8378-8ad50c2aca78")))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "id": "4b1cbc61-045c-4229-8378-8ad50c2aca78",
                          "name": "AS Nancy Lorraine",
                          "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/951523516066154017.png",
                          "projects": [
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
                          ]
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
        final var sponsor = sponsorHelper.create(userAuthHelper.authenticatePierre(), userAuthHelper.authenticateOlivier());

        // When
        client.put()
                .uri(getApiURI(PUT_SPONSORS.formatted(sponsor.id())))
                .header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new SponsorRequest()
                        .name("Foobaaaar")
                        .url(URI.create("https://www.foobaaaar.com"))
                        .logoUrl(URI.create("https://www.foobaaaar.com/logo.png"))
                        .leads(List.of(
                                userAuthHelper.authenticateOlivier().user().getId(),
                                userAuthHelper.authenticateAntho().user().getId()
                        ))
                )
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        client.get()
                .uri(getApiURI(GET_SPONSOR.formatted(sponsor.id())))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Foobaaaar")
                .jsonPath("$.url").isEqualTo("https://www.foobaaaar.com")
                .jsonPath("$.logoUrl").isEqualTo("https://www.foobaaaar.com/logo.png")
                .jsonPath("$.leads[?(@.login=='AnthonyBuisset')]").exists()
                .jsonPath("$.leads[?(@.login=='PierreOucif')]").doesNotExist()
                .jsonPath("$.leads[?(@.login=='ofux')]").exists();
    }

    @Test
    @Order(6)
    void should_get_sponsors() {
        // Given
        final String jwt = pierre.jwt();
        depositHelper.create(SponsorId.of("58a0a05c-c81e-447c-910f-629817a987b8"), ETHEREUM, USDC, TEN, DRAFT);
        depositHelper.create(SponsorId.of("4202fd03-f316-458f-a642-421c7b3c7026"), ETHEREUM, USDC, TEN, PENDING);
        depositHelper.create(SponsorId.of("4202fd03-f316-458f-a642-421c7b3c7026"), APTOS, USDC, TEN, COMPLETED);
        depositHelper.create(SponsorId.of("0980c5ab-befc-4314-acab-777fbf970cbb"), STELLAR, USDC, TEN, REJECTED);
        depositHelper.create(SponsorId.of("0980c5ab-befc-4314-acab-777fbf970cbb"), STELLAR, USDC, TEN, COMPLETED);
        depositHelper.create(SponsorId.of("0980c5ab-befc-4314-acab-777fbf970cbb"), OPTIMISM, USDC, TEN, PENDING);
        depositHelper.create(SponsorId.of("0980c5ab-befc-4314-acab-777fbf970cbb"), STARKNET, USDC, TEN, PENDING);

        // When
        client.get()
                .uri(getApiURI(GET_SPONSORS, Map.of("pageIndex", "0", "pageSize", "10")))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.sponsors[?(@.id=='58a0a05c-c81e-447c-910f-629817a987b8')].pendingDepositCount").isEqualTo(0)
                .jsonPath("$.sponsors[?(@.id=='4202fd03-f316-458f-a642-421c7b3c7026')].pendingDepositCount").isEqualTo(1)
                .jsonPath("$.sponsors[?(@.id=='0980c5ab-befc-4314-acab-777fbf970cbb')].pendingDepositCount").isEqualTo(2)
                .json("""
                        {
                          "totalPageNumber": 2,
                          "totalItemNumber": 16,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "sponsors": [
                            {
                              "name": "AS Nancy Lorraine",
                              "url": null,
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/951523516066154017.png",
                              "leads": [],
                              "pendingDepositCount": 0,
                              "programs": [
                                {
                                  "id": "4b1cbc61-045c-4229-8378-8ad50c2aca78",
                                  "name": "AS Nancy Lorraine",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/951523516066154017.png"
                                }
                              ]
                            },
                            {
                              "name": "Captain America",
                              "url": "https://www.marvel.com/characters/captain-america-steve-rogers",
                              "logoUrl": "https://www.ed92.org/wp-content/uploads/2021/06/captain-america-2-scaled.jpg",
                              "leads": [],
                              "pendingDepositCount": 0,
                              "programs": [
                                {
                                  "id": "e919b1ff-c666-4dd9-99bc-522ceb5fde7a",
                                  "name": "Captain America",
                                  "logoUrl": "https://www.ed92.org/wp-content/uploads/2021/06/captain-america-2-scaled.jpg"
                                }
                              ]
                            },
                            {
                              "name": "ChatGPT",
                              "url": "https://chat.openai.com/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/4216570625498269873.png",
                              "leads": [],
                              "pendingDepositCount": 1,
                              "programs": [
                                {
                                  "id": "04315017-a917-4f60-93c5-3b9f0dbe3af4",
                                  "name": "ChatGPT",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/4216570625498269873.png"
                                }
                              ]
                            },
                            {
                              "name": "Coca Cola",
                              "url": null,
                              "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj",
                              "leads": [],
                              "pendingDepositCount": 2,
                              "programs": [
                                {
                                  "id": "451e5b61-c340-4f85-905c-da4332c968ed",
                                  "name": "Coca Cola",
                                  "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj"
                                }
                              ]
                            },
                            {
                              "name": "Coca Colax",
                              "url": "https://www.coca-cola-france.fr/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/10299112926576087945.jpg",
                              "leads": [],
                              "pendingDepositCount": 0,
                              "programs": [
                                {
                                  "id": "a0d5edba-0d1d-4bd2-8bca-2656b86a03f6",
                                  "name": "Coca Colax",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/10299112926576087945.jpg"
                                }
                              ]
                            },
                            {
                              "name": "Creepy sponsor",
                              "url": "https://bretzel.club/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/10742744717192724286.jpg",
                              "leads": [],
                              "pendingDepositCount": 0,
                              "programs": [
                                {
                                  "id": "e92a5a4e-8730-4a30-adfb-aa78fd04471e",
                                  "name": "Creepy sponsor",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/10742744717192724286.jpg"
                                }
                              ]
                            },
                            {
                              "name": "Foobaaaar",
                              "url": "https://www.foobaaaar.com",
                              "logoUrl": "https://www.foobaaaar.com/logo.png",
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
                              ],
                              "pendingDepositCount": 0,
                              "programs": []
                            },
                            {
                              "name": "Foobar",
                              "url": "https://www.foobar.com",
                              "logoUrl": "https://www.foobar.com/logo.png",
                              "leads": [
                                {
                                  "githubUserId": 16590657,
                                  "userId": "fc92397c-3431-4a84-8054-845376b630a0",
                                  "login": "PierreOucif",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                                }
                              ],
                              "pendingDepositCount": 0,
                              "programs": []
                            },
                            {
                              "name": "No Sponsor",
                              "url": null,
                              "logoUrl": "https://app.onlydust.com/_next/static/media/onlydust-logo.68e14357.webp",
                              "leads": [],
                              "pendingDepositCount": 0,
                              "programs": [
                                {
                                  "id": "6d13685b-2853-438e-bf78-b7d4a37adc70",
                                  "name": "No Sponsor",
                                  "logoUrl": "https://app.onlydust.com/_next/static/media/onlydust-logo.68e14357.webp"
                                }
                              ]
                            },
                            {
                              "name": "OGC Nissa Ineos",
                              "url": "https://www.ogcnice.com/fr/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2946389705306833508.png",
                              "leads": [],
                              "pendingDepositCount": 0,
                              "programs": [
                                {
                                  "id": "a572bf50-3b2f-4008-8a98-f62de1acd4eb",
                                  "name": "OGC Nissa Ineos",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2946389705306833508.png"
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
                              "name": "PSG",
                              "url": "https://www.psg.fr/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168095065030147290.png",
                              "leads": [],
                              "pendingDepositCount": 0,
                              "programs": [
                                {
                                  "id": "a606447e-89a7-4e76-92e8-a5b020f83420",
                                  "name": "PSG",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168095065030147290.png"
                                }
                              ]
                            },
                            {
                              "name": "Red Bull",
                              "url": "https://www.redbull.com/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13218160580172982881.jpg",
                              "leads": [],
                              "pendingDepositCount": 0,
                              "programs": [
                                {
                                  "id": "4ffb631f-e850-46ef-9171-894f5b0f5f2c",
                                  "name": "Red Bull",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13218160580172982881.jpg"
                                }
                              ]
                            },
                            {
                              "name": "Starknet Foundation",
                              "url": "https://starknet.io",
                              "logoUrl": "https://logos-marques.com/wp-content/uploads/2020/09/Logo-Instagram-1.png",
                              "leads": [],
                              "pendingDepositCount": 0,
                              "programs": [
                                {
                                  "id": "54a7604d-e7c8-4d4b-a186-9ad7fd31e4a6",
                                  "name": "Starknet Foundation",
                                  "logoUrl": "https://logos-marques.com/wp-content/uploads/2020/09/Logo-Instagram-1.png"
                                }
                              ]
                            },
                            {
                              "name": "Theodo",
                              "url": null,
                              "logoUrl": "https://upload.wikimedia.org/wikipedia/fr/thumb/d/dd/Logo-theodo.png/1200px-Logo-theodo.png",
                              "leads": [],
                              "pendingDepositCount": 0,
                              "programs": [
                                {
                                  "id": "05ad97ac-11c4-40fa-9a35-7564120e2706",
                                  "name": "Theodo",
                                  "logoUrl": "https://upload.wikimedia.org/wikipedia/fr/thumb/d/dd/Logo-theodo.png/1200px-Logo-theodo.png"
                                }
                              ]
                            },
                            {
                              "name": "We are super sponsors!",
                              "url": null,
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/4216570625498269873.png",
                              "leads": [],
                              "pendingDepositCount": 0,
                              "programs": [
                                {
                                  "id": "9f9a8397-802a-4f60-8db9-34264b64d747",
                                  "name": "We are super sponsors!",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/4216570625498269873.png"
                                }
                              ]
                            }
                          ]
                        }
                        """);

        client.get()
                .uri(getApiURI(GET_SPONSOR.formatted("0980c5ab-befc-4314-acab-777fbf970cbb")))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.pendingDepositCount").isEqualTo(2);
    }

    @Test
    @Order(7)
    void should_get_sponsor_deposits() {
        // Given
        final String jwt = pierre.jwt();

        // When
        client.get()
                .uri(getApiURI(SPONSOR_DEPOSITS.formatted("0980c5ab-befc-4314-acab-777fbf970cbb"), Map.of("pageIndex", "0", "pageSize", "10")))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                           "totalPageNumber": 1,
                           "totalItemNumber": 9,
                           "hasMore": false,
                           "nextPageIndex": 0,
                           "deposits": [
                             {
                               "sponsor": {
                                 "id": "0980c5ab-befc-4314-acab-777fbf970cbb",
                                 "name": "Coca Cola",
                                 "url": null,
                                 "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj",
                                 "leads": []
                               },
                               "transaction": {
                                 "network": "STARKNET",
                                 "amount": 10
                               },
                               "currency": {
                                 "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                 "code": "USDC",
                                 "name": "USD Coin",
                                 "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                 "decimals": 6
                               },
                               "status": "PENDING"
                             },
                             {
                               "sponsor": {
                                 "id": "0980c5ab-befc-4314-acab-777fbf970cbb",
                                 "name": "Coca Cola",
                                 "url": null,
                                 "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj",
                                 "leads": []
                               },
                               "transaction": {
                                 "network": "OPTIMISM",
                                 "amount": 10
                               },
                               "currency": {
                                 "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                 "code": "USDC",
                                 "name": "USD Coin",
                                 "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                 "decimals": 6
                               },
                               "status": "PENDING"
                             },
                             {
                               "sponsor": {
                                 "id": "0980c5ab-befc-4314-acab-777fbf970cbb",
                                 "name": "Coca Cola",
                                 "url": null,
                                 "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj",
                                 "leads": []
                               },
                               "transaction": {
                                 "network": "STELLAR",
                                 "amount": 10
                               },
                               "currency": {
                                 "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                 "code": "USDC",
                                 "name": "USD Coin",
                                 "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                 "decimals": 6
                               },
                               "status": "COMPLETED"
                             },
                             {
                               "sponsor": {
                                 "id": "0980c5ab-befc-4314-acab-777fbf970cbb",
                                 "name": "Coca Cola",
                                 "url": null,
                                 "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj",
                                 "leads": []
                               },
                               "transaction": {
                                 "network": "STELLAR",
                                 "amount": 10
                               },
                               "currency": {
                                 "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                 "code": "USDC",
                                 "name": "USD Coin",
                                 "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                 "decimals": 6
                               },
                               "status": "REJECTED"
                             },
                             {
                               "sponsor": {
                                 "id": "0980c5ab-befc-4314-acab-777fbf970cbb",
                                 "name": "Coca Cola",
                                 "url": null,
                                 "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj",
                                 "leads": []
                               },
                               "transaction": {
                                 "network": "ETHEREUM",
                                 "amount": 3000,
                                 "blockExplorerUrl": null
                               },
                               "currency": {
                                 "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                 "code": "ETH",
                                 "name": "Ether",
                                 "logoUrl": null,
                                 "decimals": 18
                               },
                               "status": "COMPLETED"
                             },
                             {
                               "sponsor": {
                                 "id": "0980c5ab-befc-4314-acab-777fbf970cbb",
                                 "name": "Coca Cola",
                                 "url": null,
                                 "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj",
                                 "leads": []
                               },
                               "transaction": {
                                 "network": "ETHEREUM",
                                 "amount": 19933440,
                                 "blockExplorerUrl": null
                               },
                               "currency": {
                                 "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                 "code": "USDC",
                                 "name": "USD Coin",
                                 "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                 "decimals": 6
                               },
                               "status": "COMPLETED"
                             },
                             {
                               "sponsor": {
                                 "id": "0980c5ab-befc-4314-acab-777fbf970cbb",
                                 "name": "Coca Cola",
                                 "url": null,
                                 "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj",
                                 "leads": []
                               },
                               "transaction": {
                                 "network": "APTOS",
                                 "reference": "UNKNOWN",
                                 "timestamp": "2024-03-13T14:13:21.178611Z",
                                 "amount": 400000,
                                 "blockExplorerUrl": null
                               },
                               "currency": {
                                 "id": "48388edb-fda2-4a32-b228-28152a147500",
                                 "code": "APT",
                                 "name": "Aptos Coin",
                                 "logoUrl": null,
                                 "decimals": 8
                               },
                               "status": "COMPLETED"
                             },
                             {
                               "sponsor": {
                                 "id": "0980c5ab-befc-4314-acab-777fbf970cbb",
                                 "name": "Coca Cola",
                                 "url": null,
                                 "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj",
                                 "leads": []
                               },
                               "transaction": {
                                 "network": "SEPA",
                                 "reference": "UNKNOWN",
                                 "timestamp": "2024-03-13T14:13:21.150006Z",
                                 "amount": 3000,
                                 "blockExplorerUrl": null
                               },
                               "currency": {
                                 "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                                 "code": "USD",
                                 "name": "US Dollar",
                                 "logoUrl": null,
                                 "decimals": 2
                               },
                               "status": "COMPLETED"
                             },
                             {
                               "sponsor": {
                                 "id": "0980c5ab-befc-4314-acab-777fbf970cbb",
                                 "name": "Coca Cola",
                                 "url": null,
                                 "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj",
                                 "leads": []
                               },
                               "transaction": {
                                 "network": "OPTIMISM",
                                 "reference": "UNKNOWN",
                                 "timestamp": "2024-03-13T14:13:21.124359Z",
                                 "amount": 17000,
                                 "blockExplorerUrl": null
                               },
                               "currency": {
                                 "id": "00ca98a5-0197-4b76-a208-4bfc55ea8256",
                                 "code": "OP",
                                 "name": "Optimism",
                                 "logoUrl": null,
                                 "decimals": 18
                               },
                               "status": "COMPLETED"
                             }
                           ]
                         }
                        """);

    }

    @SneakyThrows
    @Test
    @Order(8)
    void should_reject_deposit() {
        // Given
        final String jwt = pierre.jwt();
        final var antho = userAuthHelper.authenticateAntho();
        final var sponsorId = sponsorHelper.create(antho).id();
        final var depositId = depositHelper.create(sponsorId, ETHEREUM, USDC, TEN, PENDING);

        // When
        reset(notificationPort);
        client.put()
                .uri(getApiURI(DEPOSIT.formatted(depositId)))
                .header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "status": "REJECTED"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();
        verify(notificationPort).push(any(), any(DepositRejected.class));

        Thread.sleep(200);

        customerIOWireMockServer.verify(1,
                postRequestedFor(urlEqualTo("/send/email"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer %s".formatted(customerIOProperties.getApiKey())))
                        .withRequestBody(matchingJsonPath("$.transactional_message_id", equalTo(customerIOProperties.getDepositRejectedEmailId().toString())))
                        .withRequestBody(matchingJsonPath("$.identifiers.id", equalTo(antho.user().getId().toString())))
                        .withRequestBody(matchingJsonPath("$.message_data.username", equalTo(antho.user().getGithubLogin())))
                        .withRequestBody(matchingJsonPath("$.message_data.title", equalTo("Deposit refused")))
                        .withRequestBody(matchingJsonPath("$.message_data.description", equalTo(("We regret to inform you that your deposit has been rejected" +
                                                                                                 ". Please review the information provided and try again. If " +
                                                                                                 "you need assistance, feel free to contact us."))))
                        .withRequestBody(matchingJsonPath("$.message_data.button.text", equalTo("Review transaction details")))
                        .withRequestBody(matchingJsonPath("$.message_data.button.link",
                                equalTo("https://develop-admin.onlydust.com/financials/%s".formatted(sponsorId))))
                        .withRequestBody(matchingJsonPath("$.to", equalTo(antho.user().getEmail())))
                        .withRequestBody(matchingJsonPath("$.subject", equalTo("Deposit refused"))));

        // When
        client.get()
                .uri(getApiURI(SPONSOR_DEPOSITS.formatted(sponsorId), Map.of("pageIndex", "0", "pageSize", "100")))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.deposits[?(@.id=='%s')].status".formatted(depositId)).isEqualTo("REJECTED")
        ;

    }

    @SneakyThrows
    @Test
    @Order(9)
    void should_approve_deposit() {
        // Given
        final String jwt = pierre.jwt();
        final var antho = userAuthHelper.authenticateAntho();
        final var sponsorId = sponsorHelper.create(antho).id();
        final var depositId = depositHelper.create(sponsorId, ETHEREUM, USDC, TEN, PENDING);

        // When
        reset(notificationPort);
        client.put()
                .uri(getApiURI(DEPOSIT.formatted(depositId)))
                .header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "status": "COMPLETED"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();
        verify(notificationPort).push(any(), any(DepositApproved.class));

        Thread.sleep(200);

        customerIOWireMockServer.verify(1,
                postRequestedFor(urlEqualTo("/send/email"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer %s".formatted(customerIOProperties.getApiKey())))
                        .withRequestBody(matchingJsonPath("$.transactional_message_id", equalTo(customerIOProperties.getDepositApprovedEmailId().toString())))
                        .withRequestBody(matchingJsonPath("$.identifiers.id", equalTo(antho.user().getId().toString())))
                        .withRequestBody(matchingJsonPath("$.message_data.username", equalTo(antho.user().getGithubLogin())))
                        .withRequestBody(matchingJsonPath("$.message_data.title", equalTo("Deposit approved")))
                        .withRequestBody(matchingJsonPath("$.message_data.description", equalTo(("Your deposit has been successfully approved. The funds are " +
                                                                                                 "now available in your account. You can view the details in " +
                                                                                                 "your dashboard."))))
                        .withRequestBody(matchingJsonPath("$.message_data.button.text", equalTo("Review transaction details")))
                        .withRequestBody(matchingJsonPath("$.message_data.button.link",
                                equalTo("https://develop-admin.onlydust.com/financials/%s".formatted(sponsorId))))
                        .withRequestBody(matchingJsonPath("$.to", equalTo(antho.user().getEmail())))
                        .withRequestBody(matchingJsonPath("$.subject", equalTo("Deposit approved"))));

        // When
        client.get()
                .uri(getApiURI(SPONSOR_DEPOSITS.formatted(sponsorId), Map.of("pageIndex", "0", "pageSize", "100")))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.deposits[?(@.id=='%s')].status".formatted(depositId)).isEqualTo("COMPLETED")
        ;
    }

    @Test
    @Order(9)
    void should_get_deposit_details() {
        // Given
        final String jwt = pierre.jwt();
        final var sponsorLead = userAuthHelper.authenticateAntho();
        final var sponsor = sponsorHelper.create(sponsorLead);
        final var depositId = depositHelper.create(sponsor.id(), ETHEREUM, USDC, TEN, PENDING);

        // When
        client.get()
                .uri(getApiURI(DEPOSIT.formatted(depositId)))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(depositId.toString())
                .jsonPath("$.sponsor.id").isEqualTo(sponsor.id().toString())
                .jsonPath("$.sponsor.name").isEqualTo(sponsor.name())
                .jsonPath("$.sponsor.url").isEqualTo(sponsor.url().toString())
                .jsonPath("$.sponsor.logoUrl").isEqualTo(sponsor.logoUrl().toString())
                .jsonPath("$.sponsor.leads.length()").isEqualTo(1)
                .jsonPath("$.sponsor.leads[0].githubUserId").isEqualTo(sponsorLead.user().getGithubUserId())
                .jsonPath("$.sponsor.leads[0].userId").isEqualTo(sponsorLead.user().getId().toString())
                .jsonPath("$.sponsor.leads[0].login").isEqualTo(sponsorLead.user().getGithubLogin())
                .jsonPath("$.transaction.id").isNotEmpty()
                .jsonPath("$.transaction.network").isEqualTo("ETHEREUM")
                .jsonPath("$.transaction.amount").isEqualTo(10)
                .jsonPath("$.transaction.blockExplorerUrl").value(url -> assertThat((String) url).startsWith("https://etherscan.io/tx/"))
                .jsonPath("$.currency.id").isEqualTo(USDC.toString())
                .jsonPath("$.currency.code").isEqualTo("USDC")
                .jsonPath("$.currency.name").isEqualTo("USD Coin")
                .jsonPath("$.billingInformation").isNotEmpty()
                .jsonPath("$.billingInformation.companyName").isNotEmpty()
                .jsonPath("$.billingInformation.companyAddress").isNotEmpty()
                .jsonPath("$.billingInformation.companyCountry").isNotEmpty()
                .jsonPath("$.billingInformation.companyId").isNotEmpty()
                .jsonPath("$.billingInformation.vatNumber").isNotEmpty()
                .jsonPath("$.billingInformation.billingEmail").isNotEmpty()
                .jsonPath("$.billingInformation.firstName").isNotEmpty()
                .jsonPath("$.billingInformation.lastName").isNotEmpty()
                .jsonPath("$.billingInformation.email").isNotEmpty()
        ;
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
}
