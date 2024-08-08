package onlydust.com.marketplace.api.it.bo;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import onlydust.com.backoffice.api.contract.model.AccountResponse;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.in.BillingProfileFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.PayoutPreferenceFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.PdfStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.RewardStatusStorage;
import onlydust.com.marketplace.accounting.domain.service.CachedAccountBookProvider;
import onlydust.com.marketplace.api.contract.model.CreateRewardResponse;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.api.read.repositories.BillingProfileReadRepository;
import onlydust.com.marketplace.api.suites.tags.TagBO;
import onlydust.com.marketplace.kernel.model.bank.BankAccount;
import onlydust.com.marketplace.kernel.model.blockchain.Aptos;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.model.blockchain.Optimism;
import onlydust.com.marketplace.kernel.model.blockchain.StarkNet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.shaded.org.apache.commons.lang3.mutable.MutableObject;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static onlydust.com.backoffice.api.contract.model.BillingProfileType.INDIVIDUAL;
import static onlydust.com.marketplace.api.helper.CurrencyHelper.*;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@TagBO
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
    private AccountBookEventRepository accountBookEventRepository;
    @Autowired
    private CachedAccountBookProvider accountBookProvider;
    @Autowired
    private BillingProfileFacadePort billingProfileFacadePort;
    @Autowired
    private BillingProfileReadRepository billingProfileReadRepository;
    @Autowired
    private PayoutPreferenceFacadePort payoutPreferenceFacadePort;
    @Autowired
    private BillingProfileStoragePort billingProfileStoragePort;
    @Autowired
    private KycRepository kycRepository;
    @Autowired
    private KybRepository kybRepository;
    @Autowired
    PdfStoragePort pdfStoragePort;
    @Autowired
    RewardStatusStorage rewardStatusStorage;
    UserAuthHelper.AuthenticatedBackofficeUser camille;

    @BeforeEach
    void setup() {
        accountBookEventRepository.deleteAll();
        accountBookRepository.deleteAll();
        sponsorAccountRepository.deleteAll();
        accountBookProvider.evictAll();
        camille = userAuthHelper.authenticateCamille();
    }

    @Test
    void should_allocate_budget_to_project_and_get_refunded_of_unspent_budget() {
        // When
        final var response = client.post()
                .uri(getApiURI(POST_SPONSORS_ACCOUNTS.formatted(COCA_COLAX)))
                .header("Authorization", "Bearer " + camille.jwt())
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
                .header("Authorization", "Bearer " + camille.jwt())
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
                .header("Authorization", "Bearer " + camille.jwt())
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
                .header("Authorization", "Bearer " + camille.jwt())
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
                .header("Authorization", "Bearer " + camille.jwt())
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
                .header("Authorization", "Bearer " + camille.jwt())
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
                .header("Authorization", "Bearer " + camille.jwt())
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
                .header("Authorization", "Bearer " + camille.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 5,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "transactions": [
                            {
                              "type": "WITHDRAWAL",
                              "network": "ETHEREUM",
                              "lockedUntil": null,
                              "project": null,
                              "amount": {
                                "amount": 30,
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
                              "type": "WITHDRAWAL",
                              "network": "ETHEREUM",
                              "lockedUntil": null,
                              "project": null,
                              "amount": {
                                "amount": 30,
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
                              "type": "REFUND",
                              "network": null,
                              "lockedUntil": null,
                              "project": {
                                "name": "Bretzel",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                              },
                              "amount": {
                                "amount": 50,
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
                              "type": "TRANSFER",
                              "network": null,
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
                .header("Authorization", "Bearer " + camille.jwt())
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
                .header("Authorization", "Bearer " + camille.jwt())
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
        final var currency1 = new MutableObject<String>();
        final var currency2 = new MutableObject<String>();

        client.get()
                .uri(getApiURI(GET_SPONSORS_ACCOUNTS.formatted(THEODO)))
                .header("Authorization", "Bearer " + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.accounts.size()").isEqualTo(2)
                .jsonPath("$.accounts[0].currency.code").value(currency1::setValue)
                .jsonPath("$.accounts[1].currency.code").value(currency2::setValue)
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

        assertThat(currency1.getValue().compareToIgnoreCase(currency2.getValue())).isLessThan(0);
    }

    @Test
    void should_delete_transaction_registered_by_mistake() {
        // Given
        final var account = client.post()
                .uri(getApiURI(POST_SPONSORS_ACCOUNTS.formatted(COCA_COLAX)))
                .header("Authorization", "Bearer " + camille.jwt())
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
                .header("Authorization", "Bearer " + camille.jwt())
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
                .header("Authorization", "Bearer " + camille.jwt())
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

        assertThat(response.getLockedUntil().toString()).isEqualTo("2024-01-31T00:00Z");

        // When
        client.patch()
                .uri(getApiURI(PATCH_SPONSOR_ACCOUNTS.formatted(response.getId())))
                .header("Authorization", "Bearer " + camille.jwt())
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
                .header("Authorization", "Bearer " + camille.jwt())
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
    void should_allocate_budget_to_project_and_pay_rewards_on_ethereum() {
        // Given
        final var antho = userAuthHelper.authenticateAnthony();
        final var ofux = userAuthHelper.authenticateOlivier();

        final var accountId = client.post()
                .uri(getApiURI(POST_SPONSORS_ACCOUNTS.formatted(REDBULL)))
                .header("Authorization", "Bearer " + camille.jwt())
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
                .header("Authorization", "Bearer " + camille.jwt())
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
                .header("Authorization", "Bearer " + camille.jwt())
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

        // When
        client.post()
                .uri(getApiURI(POST_REWARDS_PAY.formatted(rewardId)))
                .header("Authorization", "Bearer " + camille.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "network": "ETHEREUM",
                            "reference": "0x14"
                        }
                        """)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .json("""
                        {
                            "message": "Reward %s is not payable on ETHEREUM"
                        }
                        """.formatted(rewardId));

        // When
        invoiceReward(UserId.of(ofux.user().getId()), KAAPER, RewardId.of(rewardId));

        // When
        client.post()
                .uri(getApiURI(POST_REWARDS_PAY.formatted(rewardId)))
                .header("Authorization", "Bearer " + camille.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "network": "ETHEREUM",
                            "reference": "0x14"
                        }
                        """)
                .exchange()
                .expectStatus()
                .isNoContent();

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
                           "amount": {
                             "amount": 30,
                             "prettyAmount": 30,
                             "currency": {
                               "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                               "code": "USDC",
                               "name": "USD Coin",
                               "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                               "decimals": 6
                             },
                             "usdEquivalent": 30.30
                           },
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
                .header("Authorization", "Bearer " + camille.jwt())
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
                .header("Authorization", "Bearer " + camille.jwt())
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
                              "type": "TRANSFER",
                              "network": null,
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

    @Test
    void should_allocate_budget_to_project_and_pay_rewards_on_starknet() {
        // Given
        final var antho = userAuthHelper.authenticateAnthony();
        final var ofux = userAuthHelper.authenticateOlivier();

        client
                .post()
                .uri(getApiURI(CURRENCIES))
                .contentType(APPLICATION_JSON)
                .header("Authorization", "Bearer " + camille.jwt())
                .bodyValue("""
                        {
                            "type": "CRYPTO",
                            "standard": "ERC20",
                            "blockchain": "STARKNET",
                            "address": "0x053c91253bc9682c04929ca02ed00b3e423f6710d2ee7e0d5ebb06f3ecf368a8"
                        }
                        """)
                .exchange()
                .expectStatus()
                .isOk();

        final var accountId = client.post()
                .uri(getApiURI(POST_SPONSORS_ACCOUNTS.formatted(REDBULL)))
                .header("Authorization", "Bearer " + camille.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "currencyId": "%s",
                            "receipt": {
                                "reference": "0x01",
                                "amount": 100,
                                "network": "STARKNET",
                                "thirdPartyName": "RedBull",
                                "thirdPartyAccountNumber": "red-bull.stark"
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
                .header("Authorization", "Bearer " + camille.jwt())
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
                .header("Authorization", "Bearer " + camille.jwt())
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

        // When
        invoiceReward(UserId.of(ofux.user().getId()), KAAPER, RewardId.of(rewardId));

        // When
        client.post()
                .uri(getApiURI(POST_REWARDS_PAY.formatted(rewardId)))
                .header("Authorization", "Bearer " + camille.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "network": "STARKNET",
                            "reference": "0x16096a49c236dfdbc5808c31a1d6eee90a082ca5717366a73b03a2eb80cd252"
                        }
                        """)
                .exchange()
                .expectStatus()
                .isNoContent();

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
                           "amount": {
                             "amount": 30,
                             "prettyAmount": 30,
                             "currency": {
                               "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                               "code": "USDC",
                               "name": "USD Coin",
                               "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                               "decimals": 6
                             },
                             "usdEquivalent": 30.30,
                             "usdConversionRate": 1.01
                           },
                           "status": "COMPLETE",
                           "unlockDate": null,
                           "from": {
                             "githubUserId": 43467246,
                             "login": "AnthonyBuisset",
                             "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                             "isRegistered": true
                           },
                           "to": {
                             "githubUserId": 595505,
                             "login": "ofux",
                             "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                             "isRegistered": true
                           },
                           "project": {
                             "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                             "slug": "kaaper",
                             "name": "kaaper",
                             "logoUrl": null,
                             "shortDescription": "Documentation generator for Cairo projects.",
                             "visibility": "PUBLIC"
                           },
                           "receipt": {
                             "type": "CRYPTO",
                             "walletAddress": "0x0788b45a11Ee333293a1d4389430009529bC97D814233C2A5137c4F5Ff949905",
                             "transactionReference": "0x16096a49c236dfdbc5808c31a1d6eee90a082ca5717366a73b03a2eb80cd252",
                             "transactionReferenceLink": "https://starkscan.co/tx/0x016096a49c236dfdbc5808c31a1d6eee90a082ca5717366a73b03a2eb80cd252"
                           }
                         }
                         """
                );

        client.get()
                .uri(getApiURI(GET_SPONSORS_ACCOUNTS.formatted(REDBULL)))
                .header("Authorization", "Bearer " + camille.jwt())
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
                .header("Authorization", "Bearer " + camille.jwt())
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
                              "type": "TRANSFER",
                              "network": null,
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
                              "network": "STARKNET",
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

    @Test
    void should_allocate_budget_to_project_and_pay_rewards_on_aptos() {
        // Given
        final var antho = userAuthHelper.authenticateAnthony();
        final var ofux = userAuthHelper.authenticateOlivier();

        final var accountId = client.post()
                .uri(getApiURI(POST_SPONSORS_ACCOUNTS.formatted(REDBULL)))
                .header("Authorization", "Bearer " + camille.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "currencyId": "%s",
                            "receipt": {
                                "reference": "0x01",
                                "amount": 100,
                                "network": "APTOS",
                                "thirdPartyName": "RedBull",
                                "thirdPartyAccountNumber": "0xc4f5e07ce1de7369e5a408b9b153f32b5eb99e6b9b1c1a33549aba8f19fc3cc1"
                            }
                        }
                        """.formatted(APT))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(AccountResponse.class)
                .returnResult()
                .getResponseBody().getId();

        // Given
        client.post()
                .uri(getApiURI(POST_PROJECTS_BUDGETS_ALLOCATE.formatted(KAAPER)))
                .header("Authorization", "Bearer " + camille.jwt())
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
                        """.formatted(ofux.user().getGithubUserId(), APT))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(CreateRewardResponse.class)
                .returnResult().getResponseBody().getId();

        // Then
        client.get()
                .uri(getApiURI(GET_SPONSORS_ACCOUNTS.formatted(REDBULL)))
                .header("Authorization", "Bearer " + camille.jwt())
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

        // When
        invoiceReward(UserId.of(ofux.user().getId()), KAAPER, RewardId.of(rewardId));

        // When
        client.post()
                .uri(getApiURI(POST_REWARDS_PAY.formatted(rewardId)))
                .header("Authorization", "Bearer " + camille.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "network": "APTOS",
                            "reference": "0xffae983a8a8498980c4ecfd88eef5615037cad97ed1f1d7d727137421656cb2f"
                        }
                        """)
                .exchange()
                .expectStatus()
                .isNoContent();

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
                           "amount": {
                             "amount": 30,
                             "prettyAmount": 30,
                             "currency": {
                               "code": "APT"
                             }
                           },
                           "status": "COMPLETE",
                           "receipt": {
                             "type": "CRYPTO",
                             "walletAddress": "0x66cb05df2d855fbae92cdb2dfac9a0b29c969a03998fa817735d27391b52b189",
                             "transactionReference": "0xffae983a8a8498980c4ecfd88eef5615037cad97ed1f1d7d727137421656cb2f",
                             "transactionReferenceLink": "https://aptoscan.com/transaction/0xffae983a8a8498980c4ecfd88eef5615037cad97ed1f1d7d727137421656cb2f"
                           }
                         }
                         """
                );

        client.get()
                .uri(getApiURI(GET_SPONSORS_ACCOUNTS.formatted(REDBULL)))
                .header("Authorization", "Bearer " + camille.jwt())
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
                .header("Authorization", "Bearer " + camille.jwt())
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
                              "type": "TRANSFER",
                              "network": null,
                              "lockedUntil": null,
                              "project": {
                                "name": "kaaper",
                                "logoUrl": null
                              },
                              "amount": {
                                "amount": 100,
                                "currency": {
                                  "code": "APT"
                                }
                              }
                            },
                            {
                              "type": "DEPOSIT",
                              "network": "APTOS",
                              "amount": {
                                "amount": 100,
                                "currency": {
                                  "code": "APT"
                                }
                              }
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_get_billing_profile() {
        final var billingProfileId = BillingProfile.Id.of("1253b889-e5d5-49ee-8e8a-21405ccab8a6");
        final var kyb = kybRepository.findByBillingProfileId(billingProfileId.value()).orElseThrow();
        kyb.applicantId("123456");
        kybRepository.save(kyb);

        billingProfileStoragePort.savePayoutInfoForBillingProfile(PayoutInfo.builder()
                .bankAccount(BankAccount.builder()
                        .bic("AGFBFRCC")
                        .accountNumber("NL50RABO3741207772")
                        .build())
                .ethWallet(Ethereum.wallet("vitalik.eth"))
                .optimismAddress(Optimism.accountAddress("0x1111"))
                .aptosAddress(Aptos.accountAddress("0x2222"))
                .starknetAddress(StarkNet.accountAddress("0x3333"))
                .build(), billingProfileId);

        client.get()
                .uri(getApiURI(BILLING_PROFILE.formatted("1253b889-e5d5-49ee-8e8a-21405ccab8a6")))
                .header("Authorization", "Bearer " + camille.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "id": "1253b889-e5d5-49ee-8e8a-21405ccab8a6",
                          "subject": null,
                          "type": "SELF_EMPLOYED",
                          "verificationStatus": "NOT_STARTED",
                          "kyb": {
                            "name": null,
                            "registrationNumber": null,
                            "registrationDate": null,
                            "address": "19 rue pasteur, 92300, Levallois, France",
                            "country": "France",
                            "countryCode": "FRA",
                            "usEntity": null,
                            "subjectToEuropeVAT": null,
                            "euVATNumber": null,
                            "sumsubUrl": "https://cockpit.sumsub.com/checkus/#/applicant/123456/basicInfo?clientId=onlydust"
                          },
                          "kyc": null,
                          "admins": [
                            {
                              "githubUserId": 31901905,
                              "githubLogin": "kaelsky",
                              "githubAvatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                              "email": "chimansky.mickael@gmail.com",
                              "id": "a0da3c1e-6493-4ea1-8bd0-8c46d653f274",
                              "name": null
                            }
                          ],
                          "currentMonthRewardedAmounts": [],
                          "payoutInfos": {
                            "bankAccount": {
                              "bic": "AGFBFRCC",
                              "number": "NL50RABO3741207772"
                            },
                            "ethWallet": "vitalik.eth",
                            "optimismAddress": "0x1111",
                            "aptosAddress": "0x2222",
                            "starknetAddress": "0x3333"
                          }
                        }
                        """);
    }

    @Test
    void should_get_current_month_rewarded_amounts() {
        final var billingProfileId = BillingProfile.Id.of("9cae91ac-e70f-426f-af0d-e35c1d3578ed");
        final var kyb = kybRepository.findByBillingProfileId(billingProfileId.value()).orElseThrow();
        kyb.applicantId("123456");
        kybRepository.save(kyb);

        final var rewardIds = List.of(
                RewardId.of("1c56d096-5284-4ae3-af3c-dd2b3211fb73"),
                RewardId.of("4ccf3463-c77d-42cd-85f3-b393901a89c1"),
                RewardId.of("3c9064c2-4513-4876-b5dc-eab38f58f3f1"),
                RewardId.of("b0ceb0cc-294d-49e3-807e-d1a04acea11d")
        );

        rewardIds.forEach(rewardId -> rewardStatusStorage.updatePaidAt(rewardId, ZonedDateTime.now()));


        client.get()
                .uri(getApiURI(BILLING_PROFILE.formatted(billingProfileId)))
                .header("Authorization", "Bearer " + camille.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "currentMonthRewardedAmounts": [{
                            "amount": 2000,
                            "currency": {
                              "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                              "code": "USD",
                              "name": "US Dollar",
                              "logoUrl": null,
                              "decimals": 2
                            },
                            "dollarsEquivalent": 2000
                          },
                          {
                            "amount": 2000.00,
                            "currency": {
                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                              "code": "USDC",
                              "name": "USD Coin",
                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                              "decimals": 6
                            },
                            "dollarsEquivalent": 2020.0000
                          }]
                        }
                        """);
    }

    private Invoice.Id invoiceReward(UserId userId, ProjectId projectId, RewardId rewardId) {
        final var billingProfileId = billingProfileReadRepository.findByUserId(userId.value())
                .stream().filter(bp -> bp.type() == INDIVIDUAL)
                .findFirst()
                .map(billingProfileReadEntity -> BillingProfile.Id.of(billingProfileReadEntity.id()))
                .orElseGet(() -> billingProfileFacadePort.createIndividualBillingProfile(userId, "Personal", null).id());

        final var kyc = kycRepository.findByBillingProfileId(billingProfileId.value()).orElseThrow();

        billingProfileStoragePort.updateBillingProfileStatus(billingProfileId, VerificationStatus.VERIFIED);
        kycRepository.save(kyc.toBuilder()
                .firstName(faker.name().firstName())
                .address(faker.address().fullAddress())
                .consideredUsPersonQuestionnaire(false)
                .idDocumentCountryCode("FRA")
                .country("FRA")
                .build());

        billingProfileFacadePort.updatePayoutInfo(billingProfileId, userId,
                PayoutInfo.builder()
                        .ethWallet(Ethereum.wallet("ofux.eth"))
                        .starknetAddress(StarkNet.accountAddress("0x0788b45a11Ee333293a1d4389430009529bC97D814233C2A5137c4F5Ff949905"))
                        .aptosAddress(Aptos.accountAddress("0x66cb05df2d855fbae92cdb2dfac9a0b29c969a03998fa817735d27391b52b189"))
                        .build());
        payoutPreferenceFacadePort.setPayoutPreference(projectId, billingProfileId, userId);

        final var invoiceId = billingProfileFacadePort.previewInvoice(userId, billingProfileId, List.of(rewardId)).id();
        final var pdf = new ByteArrayInputStream(faker.lorem().paragraph().getBytes());

        when(pdfStoragePort.upload(eq(invoiceId.value() + ".pdf"), any())).then(invocation -> {
            final var fileName = invocation.getArgument(0, String.class);
            return new URL("https://s3.storage.com/%s".formatted(fileName));
        });

        billingProfileFacadePort.uploadGeneratedInvoice(userId, billingProfileId, invoiceId, pdf);

        return invoiceId;
    }
}
