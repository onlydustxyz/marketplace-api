package onlydust.com.marketplace.api.bootstrap.it.bo;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import onlydust.com.backoffice.api.contract.model.AccountResponse;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.in.BillingProfileFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.PayoutPreferenceFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.PdfStoragePort;
import onlydust.com.marketplace.api.contract.model.CreateRewardResponse;
import onlydust.com.marketplace.api.postgres.adapter.repository.AccountBookRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.SponsorAccountRepository;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static onlydust.com.marketplace.api.bootstrap.helper.CurrencyHelper.*;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackOfficeAccountingApiIT extends AbstractMarketplaceBackOfficeApiIT {
    static final SponsorId COCA_COLAX = SponsorId.of("44c6807c-48d1-4987-a0a6-ac63f958bdae");
    static final SponsorId THEODO = SponsorId.of("2639563e-4437-4bde-a4f4-654977c0cb39");
    static final SponsorId REDBULL = SponsorId.of("0d66ba03-cecb-45a4-ab7d-98f0cc18a3aa");

    static final ProjectId BRETZEL = ProjectId.of("7d04163c-4187-4313-8066-61504d34fc56");
    static final ProjectId KAAPER = ProjectId.of("298a547f-ecb6-4ab2-8975-68f4e9bf7b39");


    @Autowired
    private SponsorAccountRepository sponsorAccountRepository;
    @Autowired
    private AccountBookRepository accountBookRepository;
    @Autowired
    private BillingProfileFacadePort billingProfileFacadePort;
    @Autowired
    private PayoutPreferenceFacadePort payoutPreferenceFacadePort;
    @Autowired
    private BillingProfileStoragePort billingProfileStoragePort;
    @Autowired
    PdfStoragePort pdfStoragePort;

    @BeforeEach
    void setup() {
        accountBookRepository.deleteAll();
        sponsorAccountRepository.deleteAll();
    }

    @Test
    void should_allocate_budget_to_project_and_get_refunded_of_unspent_budget() {
        // When
        final var response = client.post()
                .uri(getApiURI(POST_SPONSORS_ACCOUNTS.formatted(COCA_COLAX)))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "currencyId": "%s",
                            "allowance": 100
                        }
                        """.formatted(STRK))
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(AccountResponse.class)
                .returnResult()
                .getResponseBody();

        final var accountId = response.getId();
        assertThat(accountId).isNotNull();
        assertThat(response.getSponsorId()).isEqualTo(COCA_COLAX.value());
        assertThat(response.getCurrency().getId()).isEqualTo(STRK.value());
        assertThat(response.getInitialAllowance()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(response.getCurrentAllowance()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(response.getInitialBalance()).isEqualTo(BigDecimal.ZERO);
        assertThat(response.getCurrentBalance()).isEqualTo(BigDecimal.ZERO);
        assertThat(response.getDebt()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(response.getLockedUntil()).isNull();
        assertThat(response.getAwaitingPaymentAmount()).isEqualTo(BigDecimal.ZERO);

        // When
        client.post()
                .uri(getApiURI(POST_SPONSOR_ACCOUNTS_RECEIPTS.formatted(accountId)))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "receipt": {
                                "reference": "0x01",
                                "amount": 100,
                                "network": "ETHEREUM",
                                "thirdPartyName": "Coca Cola LTD",
                                "thirdPartyAccountNumber": "coca.cola.eth"
                            }
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.reference").isEqualTo("0x01")
                .jsonPath("$.amount").isEqualTo(100)
                .jsonPath("$.network").isEqualTo("ETHEREUM")
                .jsonPath("$.thirdPartyName").isEqualTo("Coca Cola LTD")
                .jsonPath("$.thirdPartyAccountNumber").isEqualTo("coca.cola.eth")
        ;

        // When
        client.post()
                .uri(getApiURI(POST_PROJECTS_BUDGETS_ALLOCATE.formatted(BRETZEL)))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "sponsorAccountId": "%s",
                            "amount": 90
                        }
                        """.formatted(accountId))
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();


        // When
        client.post()
                .uri(getApiURI(POST_PROJECTS_BUDGETS_UNALLOCATE.formatted(BRETZEL)))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "sponsorAccountId": "%s",
                            "amount": 50
                        }
                        """.formatted(accountId))
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        // When
        client.post()
                .uri(getApiURI(POST_SPONSOR_ACCOUNTS_RECEIPTS.formatted(accountId)))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "receipt": {
                                "reference": "0x02",
                                "amount": -30,
                                "network": "ETHEREUM",
                                "thirdPartyName": "Coca Cola LTD",
                                "thirdPartyAccountNumber": "coca.cola.eth"
                            }
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.reference").isEqualTo("0x02")
                .jsonPath("$.amount").isEqualTo(-30)
                .jsonPath("$.network").isEqualTo("ETHEREUM")
                .jsonPath("$.thirdPartyName").isEqualTo("Coca Cola LTD")
                .jsonPath("$.thirdPartyAccountNumber").isEqualTo("coca.cola.eth")
        ;

        client.post()
                .uri(getApiURI(POST_SPONSOR_ACCOUNTS_RECEIPTS.formatted(accountId)))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "receipt": {
                                "reference": "0x02",
                                "amount": -30,
                                "network": "ETHEREUM",
                                "thirdPartyName": "Coca Cola LTD",
                                "thirdPartyAccountNumber": "coca.cola.eth"
                            }
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.reference").isEqualTo("0x02")
                .jsonPath("$.amount").isEqualTo(-30)
                .jsonPath("$.network").isEqualTo("ETHEREUM")
                .jsonPath("$.thirdPartyName").isEqualTo("Coca Cola LTD")
                .jsonPath("$.thirdPartyAccountNumber").isEqualTo("coca.cola.eth")
        ;


        // When
        client.put()
                .uri(getApiURI(POST_SPONSOR_ACCOUNTS_ALLOWANCE.formatted(accountId)))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "allowance": -40
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.initialBalance").isEqualTo(40)
                .jsonPath("$.currentBalance").isEqualTo(40)
                .jsonPath("$.initialAllowance").isEqualTo(60)
                .jsonPath("$.currentAllowance").isEqualTo(20)
                .jsonPath("$.debt").isEqualTo(20)
                .jsonPath("$.awaitingPaymentAmount").isEqualTo(0)
        ;


        client.get()
                .uri(getApiURI(GET_SPONSORS_TRANSACTIONS.formatted(COCA_COLAX), Map.of(
                        "pageIndex", "0",
                        "pageSize", "10"
                )))
                .header("Api-Key", apiKey())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 4,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "transactions": [
                            {
                              "type": "DEPOSIT",
                              "network": "ETHEREUM",
                              "lockedUntil": null,
                              "project": null,
                              "amount": {
                                "amount": -30,
                                "currency": {
                                  "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                                  "code": "STRK",
                                  "name": "StarkNet Token",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "dollarsEquivalent": null,
                                "conversionRate": null
                              }
                            },
                            {
                              "type": "DEPOSIT",
                              "network": "ETHEREUM",
                              "lockedUntil": null,
                              "project": null,
                              "amount": {
                                "amount": -30,
                                "currency": {
                                  "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                                  "code": "STRK",
                                  "name": "StarkNet Token",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "dollarsEquivalent": null,
                                "conversionRate": null
                              }
                            },
                            {
                              "type": "ALLOCATION",
                              "network": "ETHEREUM",
                              "lockedUntil": null,
                              "project": {
                                "name": "Bretzel",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                              },
                              "amount": {
                                "amount": 90,
                                "currency": {
                                  "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                                  "code": "STRK",
                                  "name": "StarkNet Token",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "dollarsEquivalent": null,
                                "conversionRate": null
                              }
                            },
                            {
                              "type": "DEPOSIT",
                              "network": "ETHEREUM",
                              "lockedUntil": null,
                              "project": null,
                              "amount": {
                                "amount": 100,
                                "currency": {
                                  "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                                  "code": "STRK",
                                  "name": "StarkNet Token",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "dollarsEquivalent": null,
                                "conversionRate": null
                              }
                            }
                          ]
                        }
                        """);

    }

    @Test
    void should_get_list_of_sponsor_accounts() {
        // Given
        client.post()
                .uri(getApiURI(POST_SPONSORS_ACCOUNTS.formatted(THEODO)))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "currencyId": "%s",
                             "receipt": {
                                "reference": "0x01",
                                "amount": 100,
                                "network": "ETHEREUM",
                                "thirdPartyName": "Coca Cola LTD",
                                "thirdPartyAccountNumber": "coca.cola.eth"
                            }
                        }
                        """.formatted(STRK))
                // Then
                .exchange()
                .expectStatus()
                .isOk();

        client.post()
                .uri(getApiURI(POST_SPONSORS_ACCOUNTS.formatted(THEODO)))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "currencyId": "%s",
                            "allowance": 200
                        }
                        """.formatted(BTC))
                // Then
                .exchange()
                .expectStatus()
                .isOk();

        // When
        client.get()
                .uri(getApiURI(GET_SPONSORS_ACCOUNTS.formatted(THEODO)))
                .header("Api-Key", apiKey())
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "accounts": [
                            {
                              "sponsorId": "2639563e-4437-4bde-a4f4-654977c0cb39",
                              "currency": {
                                "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                                "code": "STRK",
                                "name": "StarkNet Token",
                                "logoUrl": null,
                                "decimals": 18
                              },
                              "initialBalance": 100,
                              "currentBalance": 100,
                              "initialAllowance": 100,
                              "currentAllowance": 100,
                              "debt": 0,
                              "awaitingPaymentAmount": 0,
                              "lockedUntil": null,
                              "receipts": [
                                {
                                  "reference": "0x01",
                                  "amount": 100,
                                  "network": "ETHEREUM",
                                  "thirdPartyName": "Coca Cola LTD",
                                  "thirdPartyAccountNumber": "coca.cola.eth"
                                }
                              ]
                            },
                            {
                              "sponsorId": "2639563e-4437-4bde-a4f4-654977c0cb39",
                              "currency": {
                                "id": "3f6e1c98-8659-493a-b941-943a803bd91f",
                                "code": "BTC",
                                "name": "Bitcoin",
                                "logoUrl": null,
                                "decimals": 8
                              },
                              "currentBalance": 0,
                              "currentAllowance": 200,
                              "awaitingPaymentAmount": 0,
                              "lockedUntil": null,
                              "receipts": []
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_delete_transaction_registered_by_mistake() {
        // Given
        final var account = client.post()
                .uri(getApiURI(POST_SPONSORS_ACCOUNTS.formatted(COCA_COLAX)))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "currencyId": "%s",
                            "receipt": {
                                "amount": 100,
                                "network": "ETHEREUM",
                                "reference": "0x01",
                                "thirdPartyName": "Coca Cola LTD",
                                "thirdPartyAccountNumber": "coca.cola.eth"
                            }
                        }
                        """.formatted(STRK))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(AccountResponse.class)
                .returnResult().getResponseBody();

        // When
        client.delete()
                .uri(getApiURI(DELETE_SPONSOR_ACCOUNTS_RECEIPTS.formatted(account.getId(), account.getReceipts().get(0).getId())))
                .header("Api-Key", apiKey())
                .exchange()
                // Then
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.initialBalance").isEqualTo(0)
                .jsonPath("$.currentBalance").isEqualTo(0)
                .jsonPath("$.initialAllowance").isEqualTo(100)
                .jsonPath("$.currentAllowance").isEqualTo(100)
                .jsonPath("$.debt").isEqualTo(100)
                .jsonPath("$.receipts.size()").isEqualTo(0)
        ;
    }

    @Test
    void should_update_sponsor_account() {
        // Given
        final var response = client.post()
                .uri(getApiURI(POST_SPONSORS_ACCOUNTS.formatted(COCA_COLAX)))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "currencyId": "%s",
                            "lockedUntil": "2024-01-31T00:00:00Z",
                            "receipt": {
                                "amount": 100,
                                "network": "ETHEREUM",
                                "reference": "0x01",
                                "thirdPartyName": "Coca Cola LTD",
                                "thirdPartyAccountNumber": "coca.cola.eth"
                            }
                        }
                        """.formatted(STRK))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(AccountResponse.class)
                .returnResult().getResponseBody();

        assertThat(response.getLockedUntil().toString()).isEqualTo("2024-01-31T00:00Z[UTC]");

        // When
        client.patch()
                .uri(getApiURI(PATCH_SPONSOR_ACCOUNTS.formatted(response.getId())))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "lockedUntil": "2024-03-31T00:00:00Z"
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.lockedUntil").isEqualTo("2024-03-31T00:00:00Z")
        ;

        // When
        client.patch()
                .uri(getApiURI(PATCH_SPONSOR_ACCOUNTS.formatted(response.getId())))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "lockedUntil": null
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.lockedUntil").isEqualTo(null)
        ;
    }

    @Test
    void should_allocate_budget_to_project_and_pay_rewards() {
        // Given
        final var antho = userAuthHelper.authenticateAnthony();
        final var ofux = userAuthHelper.authenticateOlivier();

        final var accountId = client.post()
                .uri(getApiURI(POST_SPONSORS_ACCOUNTS.formatted(REDBULL)))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "currencyId": "%s",
                            "receipt": {
                                "reference": "0x01",
                                "amount": 100,
                                "network": "ETHEREUM",
                                "thirdPartyName": "RedBull",
                                "thirdPartyAccountNumber": "red-bull.eth"
                            }
                        }
                        """.formatted(USDC))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(AccountResponse.class)
                .returnResult()
                .getResponseBody().getId();

        // Given
        client.post()
                .uri(getApiURI(POST_PROJECTS_BUDGETS_ALLOCATE.formatted(KAAPER)))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "sponsorAccountId": "%s",
                            "amount": 100
                        }
                        """.formatted(accountId))
                .exchange()
                .expectStatus()
                .isNoContent();

        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/%d".formatted(ofux.user().getGithubUserId())))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

        // When
        final var rewardId = client.post()
                .uri(getApiURI(PROJECTS_REWARDS.formatted(KAAPER)))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "recipientId": "%d",
                            "amount": 30,
                            "currencyId": "%s",
                            "items": [{
                                "type": "PULL_REQUEST",
                                "id": "1703880973",
                                "number": 325,
                                "repoId": 698096830
                            }]
                        }
                        """.formatted(ofux.user().getGithubUserId(), USDC))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(CreateRewardResponse.class)
                .returnResult().getResponseBody().getId();

        // Then
        client.get()
                .uri(getApiURI(GET_SPONSORS_ACCOUNTS.formatted(REDBULL)))
                .header("Api-Key", apiKey())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.accounts[0].initialBalance").isEqualTo(100)
                .jsonPath("$.accounts[0].currentBalance").isEqualTo(100)
                .jsonPath("$.accounts[0].initialAllowance").isEqualTo(100)
                .jsonPath("$.accounts[0].currentAllowance").isEqualTo(0)
                .jsonPath("$.accounts[0].debt").isEqualTo(0)
                .jsonPath("$.accounts[0].awaitingPaymentAmount").isEqualTo(30)
        ;

        // TODO: reject payment of reward if not invoiced and approved

        // When
        final var invoiceId = invoiceReward(UserId.of(ofux.user().getId()), KAAPER, RewardId.of(rewardId));

        // When
        client.post()
                .uri(getApiURI(POST_REWARDS_SEARCH))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                            {
                                "invoiceIds": ["%s"]
                            }
                        """.formatted(invoiceId))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.rewards.length()").isEqualTo(1)
                .jsonPath("$.rewards[0].id").isEqualTo(rewardId.toString());

        // When
        client.post()
                .uri(getApiURI(POST_REWARDS_PAY.formatted(rewardId)))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "network": "ETHEREUM",
                            "reference": "0x14",
                            "recipientAccount": "ofux.eth"
                        }
                        """)
                .exchange()
                .expectStatus()
                .isNoContent();

        // When
        client.post()
                .uri(getApiURI(POST_REWARDS_SEARCH))
                .header("Api-Key", apiKey())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                            {
                                "invoiceIds": ["%s"]
                            }
                        """.formatted(invoiceId))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.rewards.length()").isEqualTo(0);


        client.get()
                .uri(getApiURI(String.format(ME_REWARD, rewardId)))
                .header("Authorization", BEARER_PREFIX + ofux.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                           "currency": {"id":"562bbf65-8a71-4d30-ad63-520c0d68ba27","code":"USDC","name":"USD Coin","logoUrl":"https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png","decimals":6},
                           "amount": 30,
                           "dollarsEquivalent": 30.3,
                           "status": "COMPLETE",
                           "from": {
                             "login": "AnthonyBuisset"
                           },
                           "to": {
                             "login": "ofux"
                           },
                           "receipt": {
                             "type": "CRYPTO",
                             "walletAddress": "ofux.eth",
                             "transactionReference": "0x14",
                             "transactionReferenceLink": "https://etherscan.io/tx/0x14"
                           }
                         }
                         """
                );

        client.get()
                .uri(getApiURI(GET_SPONSORS_ACCOUNTS.formatted(REDBULL)))
                .header("Api-Key", apiKey())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.accounts[0].initialBalance").isEqualTo(100)
                .jsonPath("$.accounts[0].currentBalance").isEqualTo(70)
                .jsonPath("$.accounts[0].initialAllowance").isEqualTo(100)
                .jsonPath("$.accounts[0].currentAllowance").isEqualTo(0)
                .jsonPath("$.accounts[0].debt").isEqualTo(0)
                .jsonPath("$.accounts[0].awaitingPaymentAmount").isEqualTo(0)
        ;

        client.get()
                .uri(getApiURI(GET_SPONSORS_TRANSACTIONS.formatted(REDBULL), Map.of(
                        "pageIndex", "0",
                        "pageSize", "10"
                )))
                .header("Api-Key", apiKey())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "transactions": [
                            {
                              "type": "ALLOCATION",
                              "network": "ETHEREUM",
                              "lockedUntil": null,
                              "project": {
                                "name": "kaaper",
                                "logoUrl": null
                              },
                              "amount": {
                                "amount": 100,
                                "currency": {
                                  "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                  "code": "USDC",
                                  "name": "USD Coin",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                  "decimals": 6
                                },
                                "dollarsEquivalent": 101.00,
                                "conversionRate": 1.01
                              }
                            },
                            {
                              "type": "DEPOSIT",
                              "network": "ETHEREUM",
                              "lockedUntil": null,
                              "project": null,
                              "amount": {
                                "amount": 100,
                                "currency": {
                                  "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                  "code": "USDC",
                                  "name": "USD Coin",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                  "decimals": 6
                                },
                                "dollarsEquivalent": 101.00,
                                "conversionRate": 1.01
                              }
                            }
                          ]
                        }
                        """);
    }

    private Invoice.Id invoiceReward(UserId userId, ProjectId projectId, RewardId rewardId) {
        final var billingProfile = billingProfileFacadePort.createIndividualBillingProfile(userId, "Personal", null);
        billingProfileStoragePort.updateBillingProfileStatus(billingProfile.id(), VerificationStatus.VERIFIED);
        billingProfileStoragePort.saveKyc(billingProfile.kyc().toBuilder().firstName(faker.name().firstName()).address(faker.address().fullAddress()).usCitizen(false).country(Country.fromIso3("FRA")).build());
        billingProfileFacadePort.updatePayoutInfo(billingProfile.id(), userId, PayoutInfo.builder().ethWallet(Ethereum.wallet("ofux.eth")).build());
        payoutPreferenceFacadePort.setPayoutPreference(projectId, billingProfile.id(), userId);

        final var invoiceId = billingProfileFacadePort.previewInvoice(userId, billingProfile.id(), List.of(rewardId)).id();
        final var pdf = new ByteArrayInputStream(faker.lorem().paragraph().getBytes());

        when(pdfStoragePort.upload(eq(invoiceId.value() + ".pdf"), any())).then(invocation -> {
            final var fileName = invocation.getArgument(0, String.class);
            return new URL("https://s3.storage.com/%s".formatted(fileName));
        });

        billingProfileFacadePort.uploadGeneratedInvoice(userId, billingProfile.id(), invoiceId, pdf);

        return invoiceId;
    }
}
