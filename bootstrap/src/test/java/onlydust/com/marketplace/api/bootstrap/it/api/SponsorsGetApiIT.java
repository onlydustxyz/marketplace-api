package onlydust.com.marketplace.api.bootstrap.it.api;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;


public class SponsorsGetApiIT extends AbstractMarketplaceApiIT {
    private final static SponsorId sponsorId = SponsorId.of("0980c5ab-befc-4314-acab-777fbf970cbb");
    private UserAuthHelper.AuthenticatedUser user;

    @BeforeEach
    void setup() {
        user = userAuthHelper.authenticateAnthony();
    }

    @Test
    void should_return_forbidden_if_not_admin() {
        getSponsor(sponsorId)
                .expectStatus()
                .isForbidden();

        getSponsorTransactions(sponsorId, 0, 1)
                .expectStatus()
                .isForbidden();
    }

    @Test
    void should_return_sponsor_by_id() {
        // Given
        addSponsorFor(user, sponsorId);

        // When
        getSponsor(sponsorId)
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "id": "0980c5ab-befc-4314-acab-777fbf970cbb",
                          "name": "Coca Cola",
                          "url": null,
                          "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj",
                          "availableBudgets": [
                            {
                              "currency": {
                                "id": "48388edb-fda2-4a32-b228-28152a147500",
                                "code": "APT",
                                "name": "Aptos Coin",
                                "logoUrl": null,
                                "decimals": 8
                              },
                              "currentAllowance": 0
                            },
                            {
                              "currency": {
                                "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                "code": "ETH",
                                "name": "Ether",
                                "logoUrl": null,
                                "decimals": 18
                              },
                              "currentAllowance": 0
                            },
                            {
                              "currency": {
                                "id": "00ca98a5-0197-4b76-a208-4bfc55ea8256",
                                "code": "OP",
                                "name": "Optimism",
                                "logoUrl": null,
                                "decimals": 18
                              },
                              "currentAllowance": 0
                            },
                            {
                              "currency": {
                                "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                                "code": "USD",
                                "name": "US Dollar",
                                "logoUrl": null,
                                "decimals": 2
                              },
                              "currentAllowance": 0
                            },
                            {
                              "currency": {
                                "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                "code": "USDC",
                                "name": "USD Coin",
                                "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                "decimals": 6
                              },
                              "currentAllowance": 0
                            }
                          ],
                          "projects": [
                            {
                              "name": "Bretzel",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png",
                              "remainingBudgets": [
                                {
                                  "amount": 400000,
                                  "currency": {
                                    "id": "48388edb-fda2-4a32-b228-28152a147500",
                                    "code": "APT",
                                    "name": "Aptos Coin",
                                    "logoUrl": null,
                                    "decimals": 8
                                  },
                                  "usdEquivalent": null
                                },
                                {
                                  "amount": 3000,
                                  "currency": {
                                    "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                    "code": "ETH",
                                    "name": "Ether",
                                    "logoUrl": null,
                                    "decimals": 18
                                  },
                                  "usdEquivalent": 5345940.00
                                },
                                {
                                  "amount": 17000,
                                  "currency": {
                                    "id": "00ca98a5-0197-4b76-a208-4bfc55ea8256",
                                    "code": "OP",
                                    "name": "Optimism",
                                    "logoUrl": null,
                                    "decimals": 18
                                  },
                                  "usdEquivalent": null
                                },
                                {
                                  "amount": 99250.00,
                                  "currency": {
                                    "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                    "code": "USDC",
                                    "name": "USD Coin",
                                    "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                    "decimals": 6
                                  },
                                  "usdEquivalent": 100242.5000
                                }
                              ]
                            },
                            {
                              "name": "Aiolia du Lion",
                              "logoUrl": "https://www.puregamemedia.fr/media/images/uploads/2019/11/ban_saint_seiya_awakening_kotz_aiolia_lion.jpg/?w=790&h=inherit&fm=webp&fit=contain&s=11e0e551affa5a88cc8c6de7f352449c",
                              "remainingBudgets": [
                                {
                                  "amount": 19827065,
                                  "currency": {
                                    "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                    "code": "USDC",
                                    "name": "USD Coin",
                                    "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                    "decimals": 6
                                  },
                                  "usdEquivalent": 20025335.65
                                }
                              ]
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_return_sponsor_transactions() {
        // Given
        addSponsorFor(user, sponsorId);

        // When
        getSponsorTransactions(sponsorId, 0, 3)
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 4,
                          "totalItemNumber": 11,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "transactions": [
                            {
                              "date": "2024-03-13T15:13:21.256797Z",
                              "type": "ALLOCATION",
                              "project": {
                                "name": "Bretzel",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                              },
                              "amount": {
                                "amount": 3000,
                                "currency": {
                                  "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                  "code": "ETH",
                                  "name": "Ether",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": null
                              }
                            },
                            {
                              "date": "2024-03-13T15:13:21.247974Z",
                              "type": "DEPOSIT",
                              "project": null,
                              "amount": {
                                "amount": 3000,
                                "currency": {
                                  "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                  "code": "ETH",
                                  "name": "Ether",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": null
                              }
                            },
                            {
                              "date": "2024-03-13T15:13:21.225044Z",
                              "type": "ALLOCATION",
                              "project": {
                                "name": "Aiolia du Lion",
                                "logoUrl": "https://www.puregamemedia.fr/media/images/uploads/2019/11/ban_saint_seiya_awakening_kotz_aiolia_lion.jpg/?w=790&h=inherit&fm=webp&fit=contain&s=11e0e551affa5a88cc8c6de7f352449c"
                              },
                              "amount": {
                                "amount": 19827190,
                                "currency": {
                                  "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                  "code": "USDC",
                                  "name": "USD Coin",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                  "decimals": 6
                                },
                                "usdEquivalent": null
                              }
                            }
                          ]
                        }
                        """);
    }

    @NonNull
    private WebTestClient.ResponseSpec getSponsor(SponsorId id) {
        return client.get()
                .uri(SPONSOR.formatted(id))
                .header("Authorization", "Bearer " + user.jwt())
                .exchange();
    }

    @NonNull
    private WebTestClient.ResponseSpec getSponsorTransactions(SponsorId id, Integer pageIndex, Integer pageSize) {
        return client.get()
                .uri(getApiURI(SPONSOR_TRANSACTIONS.formatted(id), Map.of("pageIndex", pageIndex.toString(), "pageSize", pageSize.toString())))
                .header("Authorization", "Bearer " + user.jwt())
                .exchange();
    }
}
