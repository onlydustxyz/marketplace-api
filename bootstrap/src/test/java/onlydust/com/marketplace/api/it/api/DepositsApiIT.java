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
