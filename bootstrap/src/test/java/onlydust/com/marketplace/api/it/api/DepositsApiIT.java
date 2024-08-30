package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagAccounting;
import onlydust.com.marketplace.project.domain.model.Sponsor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@TagAccounting
public class DepositsApiIT extends AbstractMarketplaceApiIT {
    UserAuthHelper.AuthenticatedUser caller;

    @BeforeEach
    void setUp() {
        caller = userAuthHelper.create();
    }

    @Nested
    class GivenMySponsor {
        Sponsor sponsor;

        @BeforeEach
        void setUp() {
            sponsor = sponsorHelper.create(caller);
        }

        @Test
        void should_be_preview_a_deposit_of_eth_on_ethereum() {
            // When
            client.post()
                    .uri(getApiURI(SPONSOR_DEPOSITS.formatted(sponsor.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                                "network": "ETHEREUM",
                                "transactionReference": "0x1234567890123456789012345678901234567890"
                            }
                            """)
                    .exchange()
                    // Then
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.id").isNotEmpty()
                    .jsonPath("$.senderInformation.name").isEqualTo(sponsor.name())
                    .json("""
                            {
                              "amount": {
                                "amount": 0.029180771065409698,
                                "prettyAmount": 0.02918,
                                "currency": {
                                  "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                  "code": "ETH",
                                  "name": "Ether",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 52.00,
                                "usdConversionRate": 1781.983987
                              },
                              "currentBalance": {
                                "amount": 0,
                                "prettyAmount": 0,
                                "currency": {
                                  "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                  "code": "ETH",
                                  "name": "Ether",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 0.00,
                                "usdConversionRate": 1781.983987
                              },
                              "finalBalance": {
                                "amount": 0.029180771065409698,
                                "prettyAmount": 0.02918,
                                "currency": {
                                  "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                  "code": "ETH",
                                  "name": "Ether",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 52.00,
                                "usdConversionRate": 1781.983987
                              },
                              "senderInformation": {
                                "accountNumber": "0x1f9090aae28b8a3dceadf281b0f12828e676c326",
                                "transactionReference": "0x1234567890123456789012345678901234567890"
                              },
                              "billingInformation": null
                            }
                            """);
        }

        @Test
        void should_be_preview_a_deposit_of_usdc_on_ethereum() {
            // When
            client.post()
                    .uri(getApiURI(SPONSOR_DEPOSITS.formatted(sponsor.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                                "network": "ETHEREUM",
                                "transactionReference": "0x626f7613dfb503b441cb15f205441a73608795b73974bc6d142e6b72e8b81a2f"
                            }
                            """)
                    .exchange()
                    // Then
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.id").isNotEmpty()
                    .jsonPath("$.senderInformation.name").isEqualTo(sponsor.name())
                    .json("""
                            {
                              "amount": {
                                "amount": 776.852779,
                                "prettyAmount": 776.85,
                                "currency": {
                                  "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                  "code": "USDC",
                                  "name": "USD Coin",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                  "decimals": 6
                                },
                                "usdEquivalent": 784.62,
                                "usdConversionRate": 1.010001
                              },
                              "currentBalance": {
                                "amount": 0,
                                "prettyAmount": 0,
                                "currency": {
                                  "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                  "code": "USDC",
                                  "name": "USD Coin",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                  "decimals": 6
                                },
                                "usdEquivalent": 0.00,
                                "usdConversionRate": 1.010001
                              },
                              "finalBalance": {
                                "amount": 776.852779,
                                "prettyAmount": 776.85,
                                "currency": {
                                  "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                  "code": "USDC",
                                  "name": "USD Coin",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                  "decimals": 6
                                },
                                "usdEquivalent": 784.62,
                                "usdConversionRate": 1.010001
                              },
                              "senderInformation": {
                                "accountNumber": "0xe6f63ed2d861e2a4d2de598262565250ffc11d24",
                                "transactionReference": "0x626f7613dfb503b441cb15f205441a73608795b73974bc6d142e6b72e8b81a2f"
                              },
                              "billingInformation": null
                            }
                            """);
        }

        @Test
        void should_be_preview_a_deposit_of_eth_on_optimism() {
            // When
            client.post()
                    .uri(getApiURI(SPONSOR_DEPOSITS.formatted(sponsor.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                                "network": "OPTIMISM",
                                "transactionReference": "0x1234567890123456789012345678901234567890"
                            }
                            """)
                    .exchange()
                    // Then
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.id").isNotEmpty()
                    .jsonPath("$.senderInformation.name").isEqualTo(sponsor.name())
                    .json("""
                            {
                              "amount": {
                                "amount": 0.029180771065409698,
                                "prettyAmount": 0.02918,
                                "currency": {
                                  "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                  "code": "ETH",
                                  "name": "Ether",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 52.00,
                                "usdConversionRate": 1781.983987
                              },
                              "currentBalance": {
                                "amount": 0,
                                "prettyAmount": 0,
                                "currency": {
                                  "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                  "code": "ETH",
                                  "name": "Ether",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 0.00,
                                "usdConversionRate": 1781.983987
                              },
                              "finalBalance": {
                                "amount": 0.029180771065409698,
                                "prettyAmount": 0.02918,
                                "currency": {
                                  "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                  "code": "ETH",
                                  "name": "Ether",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 52.00,
                                "usdConversionRate": 1781.983987
                              },
                              "senderInformation": {
                                "accountNumber": "0x1f9090aae28b8a3dceadf281b0f12828e676c326",
                                "transactionReference": "0x1234567890123456789012345678901234567890"
                              },
                              "billingInformation": null
                            }
                            """);
        }

        @Test
        void should_be_preview_a_deposit_of_op_on_optimism() {
            // When
            client.post()
                    .uri(getApiURI(SPONSOR_DEPOSITS.formatted(sponsor.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                                "network": "OPTIMISM",
                                "transactionReference": "0x821fd2b9b7c950d712ffa13b2b7bed56db35b3919f40baf10d816d4dc35a479f"
                            }
                            """)
                    .exchange()
                    // Then
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.id").isNotEmpty()
                    .jsonPath("$.senderInformation.name").isEqualTo(sponsor.name())
                    .json("""
                            {
                              "amount": {
                                "amount": 14.156839490418507000,
                                "prettyAmount": 14.156839490418507000,
                                "currency": {
                                  "id": "00ca98a5-0197-4b76-a208-4bfc55ea8256",
                                  "code": "OP",
                                  "name": "Optimism",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": null,
                                "usdConversionRate": null
                              },
                              "currentBalance": {
                                "amount": 0,
                                "prettyAmount": 0,
                                "currency": {
                                  "id": "00ca98a5-0197-4b76-a208-4bfc55ea8256",
                                  "code": "OP",
                                  "name": "Optimism",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": null,
                                "usdConversionRate": null
                              },
                              "finalBalance": {
                                "amount": 14.156839490418507000,
                                "prettyAmount": 14.156839490418507000,
                                "currency": {
                                  "id": "00ca98a5-0197-4b76-a208-4bfc55ea8256",
                                  "code": "OP",
                                  "name": "Optimism",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": null,
                                "usdConversionRate": null
                              },
                              "senderInformation": {
                                "accountNumber": "0x8d345c1dcf02495a1e7089a7bc61c77fe2326027",
                                "transactionReference": "0x821fd2b9b7c950d712ffa13b2b7bed56db35b3919f40baf10d816d4dc35a479f"
                              },
                              "billingInformation": null
                            }
                            """);
        }
    }

    @Nested
    class GivenNotMySponsor {
        Sponsor sponsor;

        @BeforeEach
        void setUp() {
            sponsor = sponsorHelper.create();
        }

        @Test
        void should_be_unauthorized_previewing_a_deposit() {
            // When
            client.post()
                    .uri(getApiURI(SPONSOR_DEPOSITS.formatted(sponsor.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                                "network": "ETHEREUM",
                                "transactionReference": "0x1234567890123456789012345678901234567890"
                            }
                            """)
                    .exchange()
                    // Then
                    .expectStatus()
                    .isForbidden();
        }
    }
}
