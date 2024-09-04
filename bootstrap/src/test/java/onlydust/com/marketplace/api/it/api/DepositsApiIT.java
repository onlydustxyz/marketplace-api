package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Deposit;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.accounting.domain.model.OnlyDustWallets;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.repository.DepositRepository;
import onlydust.com.marketplace.api.slack.SlackApiAdapter;
import onlydust.com.marketplace.api.suites.tags.TagAccounting;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.project.domain.model.Sponsor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@TagAccounting
public class DepositsApiIT extends AbstractMarketplaceApiIT {
    UserAuthHelper.AuthenticatedUser caller;

    @Autowired
    DepositRepository depositRepository;
    @Autowired
    OnlyDustWallets onlyDustWallets;

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
        void should_preview_a_deposit_of_eth_on_ethereum() {
            // Given
            onlyDustWallets.setEthereum("0xb060429d14266d06a8be63281205668be823604f");

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
        void should_preview_a_deposit_of_usdc_on_ethereum() {
            // Given
            onlyDustWallets.setEthereum("0x8371e21f595dbf98caffdcef665ebcaccb983cb1");

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
        void should_preview_a_deposit_of_eth_on_optimism() {
            // Given
            onlyDustWallets.setOptimism("0xb060429d14266d06a8be63281205668be823604f");

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
        void should_preview_a_deposit_of_op_on_optimism() {
            // Given
            onlyDustWallets.setOptimism("0x1c9d4522e258138f36b4b356bb8afc6be013f902");

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

        @Test
        void should_preview_a_deposit_of_usdc_on_starknet() {
            // Given
            onlyDustWallets.setStarknet("0x039b01fbac905c359757a65ae41b75d5c4b0f16a0f0e8dd9aca1506da545eef8");
            currencyHelper.addERC20Support(Blockchain.STARKNET, Currency.Code.USDC);

            // When
            client.post()
                    .uri(getApiURI(SPONSOR_DEPOSITS.formatted(sponsor.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                                "network": "STARKNET",
                                "transactionReference": "0x015b2c010276e6a8eb3739972869d6f9bf4e0ce441c047895a8b44bd8ea9bfdb"
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
                                "amount": 138.000000,
                                "prettyAmount": 138.00,
                                "currency": {
                                  "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                  "code": "USDC",
                                  "name": "USD Coin",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                  "decimals": 6
                                },
                                "usdEquivalent": 139.38,
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
                                "amount": 138.000000,
                                "prettyAmount": 138.00,
                                "currency": {
                                  "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                  "code": "USDC",
                                  "name": "USD Coin",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                  "decimals": 6
                                },
                                "usdEquivalent": 139.38,
                                "usdConversionRate": 1.010001
                              },
                              "senderInformation": {
                                "accountNumber": "0x8a518279505bb4b95ff3f2f9700501ccef4c8720f259480c76cbf49dd2b6b8",
                                "transactionReference": "0x015b2c010276e6a8eb3739972869d6f9bf4e0ce441c047895a8b44bd8ea9bfdb"
                              },
                              "billingInformation": null
                            }
                            """);
        }

        @Autowired
        SlackApiAdapter slackApiAdapter;

        @Test
        void should_update_deposit() {
            // Given
            final var deposit = depositHelper.preview(sponsor.id(), Network.ETHEREUM);

            // When
            client.put()
                    .uri(getApiURI(DEPOSIT_BY_ID.formatted(deposit.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                                "billingInformation": {
                                    "companyName": "TechCorp Solutions",
                                    "companyAddress": "123 Innovation Street, Tech City, TC 12345",
                                    "companyCountry": "United States",
                                    "companyId": "TC-987654321",
                                    "vatNumber": "VAT123456789",
                                    "billingEmail": "billing@techcorp.com",
                                    "firstName": "John",
                                    "lastName": "Doe",
                                    "email": "john.doe@techcorp.com"
                                }
                            }
                            """)
                    .exchange()
                    // Then
                    .expectStatus()
                    .is2xxSuccessful();

            // Then
            final var depositEntity = depositRepository.findById(deposit.id().value()).orElseThrow();
            assertThat(depositEntity.billingInformation().companyName()).isEqualTo("TechCorp Solutions");
            assertThat(depositEntity.status()).isEqualTo(Deposit.Status.PENDING);
            verify(slackApiAdapter).onDepositSubmittedByUser(UserId.of(caller.user().getId()), deposit.id());

            // When another preview is made, the latest billing information should be returned
            client.post()
                    .uri(getApiURI(SPONSOR_DEPOSITS.formatted(sponsor.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                                "network": "ETHEREUM",
                                "transactionReference": "0x999888777"
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
                              "senderInformation": {
                                "accountNumber": "0x1f9090aae28b8a3dceadf281b0f12828e676c326",
                                "transactionReference": "0x0999888777"
                              },
                              "billingInformation": {
                                "companyName": "TechCorp Solutions",
                                "companyAddress": "123 Innovation Street, Tech City, TC 12345",
                                "companyCountry": "United States",
                                "companyId": "TC-987654321",
                                "vatNumber": "VAT123456789",
                                "billingEmail": "billing@techcorp.com",
                                "firstName": "John",
                                "lastName": "Doe",
                                "email": "john.doe@techcorp.com"
                              }
                            }
                            """);
        }

        @Test
        void should_preview_a_deposit_with_same_transaction_reference() {
            // Given
            onlyDustWallets.setEthereum("0xb060429d14266d06a8be63281205668be823604f");
            final var deposit = depositHelper.preview(sponsor.id(), Network.ETHEREUM, "0x111112222233333");

            // When
            client.post()
                    .uri(getApiURI(SPONSOR_DEPOSITS.formatted(sponsor.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                                "network": "ETHEREUM",
                                "transactionReference": "0x111112222233333"
                            }
                            """)
                    .exchange()
                    // Then
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(deposit.id().value().toString());
        }

        @Test
        void should_preview_a_deposit_of_apt_on_aptos() {
            // Given
            onlyDustWallets.setAptos("0xa35864ccdb3abcb64c144da4511c66457f743ee0ddf95c1b5bbfabaf67e6ac73");
            currencyHelper.addERC20Support(Blockchain.APTOS, Currency.Code.APT);

            // When
            client.post()
                    .uri(getApiURI(SPONSOR_DEPOSITS.formatted(sponsor.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                                "network": "APTOS",
                                "transactionReference": "0xa16d8c7b6891ccaa63bcb854762a092a1d8ab4c9d9f2b8b11ca0880dfda9526a"
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
                                "amount": 0.00010000,
                                "prettyAmount": 0,
                                "currency": {
                                  "id": "48388edb-fda2-4a32-b228-28152a147500",
                                  "code": "APT",
                                  "name": "Aptos Coin",
                                  "logoUrl": null,
                                  "decimals": 8
                                },
                                "usdEquivalent": 0.00,
                                "usdConversionRate": 0.30134
                              },
                              "currentBalance": {
                                "amount": 0,
                                "prettyAmount": 0,
                                "currency": {
                                  "id": "48388edb-fda2-4a32-b228-28152a147500",
                                  "code": "APT",
                                  "name": "Aptos Coin",
                                  "logoUrl": null,
                                  "decimals": 8
                                },
                                "usdEquivalent": 0.00,
                                "usdConversionRate": 0.30134
                              },
                              "finalBalance": {
                                "amount": 0.00010000,
                                "prettyAmount": 0,
                                "currency": {
                                  "id": "48388edb-fda2-4a32-b228-28152a147500",
                                  "code": "APT",
                                  "name": "Aptos Coin",
                                  "logoUrl": null,
                                  "decimals": 8
                                },
                                "usdEquivalent": 0.00,
                                "usdConversionRate": 0.30134
                              },
                              "senderInformation": {
                                "accountNumber": "0x4fdad0b542bae2fa39f42af8c0d347190c3508fed0eeaa8710701c12aaa16f63",
                                "transactionReference": "0xa16d8c7b6891ccaa63bcb854762a092a1d8ab4c9d9f2b8b11ca0880dfda9526a"
                              },
                              "billingInformation": null
                            }
                            """);
        }

        @Test
        void should_preview_a_deposit_of_usdc_on_aptos() {
            // Given
            onlyDustWallets.setAptos("0xa35864ccdb3abcb64c144da4511c66457f743ee0ddf95c1b5bbfabaf67e6ac73");
            currencyHelper.addERC20Support(Blockchain.APTOS, Currency.Code.USDC);

            // When
            client.post()
                    .uri(getApiURI(SPONSOR_DEPOSITS.formatted(sponsor.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                                "network": "APTOS",
                                "transactionReference": "0x77d88076dbd3fb848b0fc6ce48123e6270974d7369419326983fe540ca5384db"
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
                                "amount": 0.049308,
                                "prettyAmount": 0.05,
                                "currency": {
                                  "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                  "code": "USDC",
                                  "name": "USD Coin",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                  "decimals": 6
                                },
                                "usdEquivalent": 0.05,
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
                                "amount": 0.049308,
                                "prettyAmount": 0.05,
                                "currency": {
                                  "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                  "code": "USDC",
                                  "name": "USD Coin",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                  "decimals": 6
                                },
                                "usdEquivalent": 0.05,
                                "usdConversionRate": 1.010001
                              },
                              "senderInformation": {
                                "accountNumber": "0x4fdad0b542bae2fa39f42af8c0d347190c3508fed0eeaa8710701c12aaa16f63",
                                "transactionReference": "0x77d88076dbd3fb848b0fc6ce48123e6270974d7369419326983fe540ca5384db"
                              },
                              "billingInformation": null
                            }
                            """);
        }

        @Test
        void should_preview_a_deposit_of_xlm_on_stellar() {
            // Given
            onlyDustWallets.setStellar("GD2VXOJ7SJS6GYWT3LIKS5KWBXYTZGIJ5TBLP5TGIKBPPWRALCZMGXEF");
            currencyHelper.addNativeCryptoSupport(Currency.Code.XLM);

            // When
            client.post()
                    .uri(getApiURI(SPONSOR_DEPOSITS.formatted(sponsor.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                                "network": "STELLAR",
                                "transactionReference": "01cab5c04cf265b2995a2e5c4e961cad82d38bfb9e950ec3f6e33e5ff28500d8"
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
                                "amount": 1.2760000,
                                "prettyAmount": 1.2760000,
                                "currency": {
                                  "code": "XLM",
                                  "name": "Stellar",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/512.png",
                                  "decimals": 7
                                },
                                "usdEquivalent": null,
                                "usdConversionRate": null
                              },
                              "currentBalance": {
                                "amount": 0,
                                "prettyAmount": 0,
                                "currency": {
                                  "code": "XLM",
                                  "name": "Stellar",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/512.png",
                                  "decimals": 7
                                },
                                "usdEquivalent": null,
                                "usdConversionRate": null
                              },
                              "finalBalance": {
                                "amount": 1.2760000,
                                "prettyAmount": 1.2760000,
                                "currency": {
                                  "code": "XLM",
                                  "name": "Stellar",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/512.png",
                                  "decimals": 7
                                },
                                "usdEquivalent": null,
                                "usdConversionRate": null
                              },
                              "senderInformation": {
                                "accountNumber": "GAIYZIEWGAEYIVMX5TMSD43HROWXX5WG35KTL6467P52S477IQQJIUEL",
                                "transactionReference": "01cab5c04cf265b2995a2e5c4e961cad82d38bfb9e950ec3f6e33e5ff28500d8"
                              },
                              "billingInformation": null
                            }
                            """);
        }

        @Test
        void should_preview_a_deposit_of_usdc_on_stellar() {
            // Given
            onlyDustWallets.setStellar("GD2VXOJ7SJS6GYWT3LIKS5KWBXYTZGIJ5TBLP5TGIKBPPWRALCZMGXEF");

            // When
            client.post()
                    .uri(getApiURI(SPONSOR_DEPOSITS.formatted(sponsor.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                                "network": "STELLAR",
                                "transactionReference": "97157d6c947af69ea379edee2883562cb4b18a7882d366d368b595d899d82835"
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
                                "amount": 0.1837502,
                                "prettyAmount": 0.18,
                                "currency": {
                                  "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                  "code": "USDC",
                                  "name": "USD Coin",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                  "decimals": 6
                                },
                                "usdEquivalent": 0.19,
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
                                "amount": 0.1837502,
                                "prettyAmount": 0.18,
                                "currency": {
                                  "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                  "code": "USDC",
                                  "name": "USD Coin",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                  "decimals": 6
                                },
                                "usdEquivalent": 0.19,
                                "usdConversionRate": 1.010001
                              },
                              "senderInformation": {
                                "accountNumber": "GAIYZIEWGAEYIVMX5TMSD43HROWXX5WG35KTL6467P52S477IQQJIUEL",
                                "transactionReference": "97157d6c947af69ea379edee2883562cb4b18a7882d366d368b595d899d82835"
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

        @Test
        void should_be_unauthorized_to_update_a_deposit() {
            // Given
            final var deposit = depositHelper.preview(sponsor.id(), Network.ETHEREUM);

            // When
            client.put()
                    .uri(getApiURI(DEPOSIT_BY_ID.formatted(deposit.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                                "billingInformation": {
                                    "companyName": "TechCorp Solutions",
                                    "companyAddress": "123 Innovation Street, Tech City, TC 12345",
                                    "companyCountry": "United States",
                                    "companyId": "TC-987654321",
                                    "vatNumber": "VAT123456789",
                                    "billingEmail": "billing@techcorp.com",
                                    "firstName": "John",
                                    "lastName": "Doe",
                                    "email": "john.doe@techcorp.com"
                                }
                            }
                            """)
                    .exchange()
                    // Then
                    .expectStatus()
                    .isForbidden();
        }
    }
}
