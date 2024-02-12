package onlydust.com.marketplace.api.bootstrap.it.bo;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import onlydust.com.backoffice.api.contract.model.AccountResponse;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.api.contract.model.CreateRewardResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public class BackOfficeAccountingApiIT extends AbstractMarketplaceBackOfficeApiIT {
    static final SponsorId COCA_COLAX = SponsorId.of("44c6807c-48d1-4987-a0a6-ac63f958bdae");
    static final SponsorId THEODO = SponsorId.of("2639563e-4437-4bde-a4f4-654977c0cb39");
    static final SponsorId REDBULL = SponsorId.of("0d66ba03-cecb-45a4-ab7d-98f0cc18a3aa");

    static final ProjectId BRETZEL = ProjectId.of("7d04163c-4187-4313-8066-61504d34fc56");
    static final ProjectId KAAPER = ProjectId.of("298a547f-ecb6-4ab2-8975-68f4e9bf7b39");

    static final Currency.Id BTC = Currency.Id.of("3f6e1c98-8659-493a-b941-943a803bd91f");
    static final Currency.Id STRK = Currency.Id.of("81b7e948-954f-4718-bad3-b70a0edd27e1");

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
                        """.formatted(BTC))
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
        assertThat(response.getCurrencyId()).isEqualTo(BTC.value());
        assertThat(response.getAllowance()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(response.getBalance()).isEqualTo(BigDecimal.ZERO);
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
                .jsonPath("$.balance").isEqualTo(100)
                .jsonPath("$.allowance").isEqualTo(100)
                .jsonPath("$.receipts[0].reference").isEqualTo("0x01")
                .jsonPath("$.receipts[0].amount").isEqualTo(100)
                .jsonPath("$.receipts[0].network").isEqualTo("ETHEREUM")
                .jsonPath("$.receipts[0].thirdPartyName").isEqualTo("Coca Cola LTD")
                .jsonPath("$.receipts[0].thirdPartyAccountNumber").isEqualTo("coca.cola.eth");
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
                                "amount": -60,
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
                .jsonPath("$.balance").isEqualTo(40)
                .jsonPath("$.allowance").isEqualTo(60)
                .jsonPath("$.receipts[0].reference").isEqualTo("0x01")
                .jsonPath("$.receipts[0].amount").isEqualTo(100)
                .jsonPath("$.receipts[0].network").isEqualTo("ETHEREUM")
                .jsonPath("$.receipts[0].thirdPartyName").isEqualTo("Coca Cola LTD")
                .jsonPath("$.receipts[0].thirdPartyAccountNumber").isEqualTo("coca.cola.eth")
                .jsonPath("$.receipts[1].reference").isEqualTo("0x02")
                .jsonPath("$.receipts[1].amount").isEqualTo(-60)
                .jsonPath("$.receipts[1].network").isEqualTo("ETHEREUM")
                .jsonPath("$.receipts[1].thirdPartyName").isEqualTo("Coca Cola LTD")
                .jsonPath("$.receipts[1].thirdPartyAccountNumber").isEqualTo("coca.cola.eth")
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
                .jsonPath("$.balance").isEqualTo(40)
                .jsonPath("$.allowance").isEqualTo(20)
                .jsonPath("$.awaitingPaymentAmount").isEqualTo(0)
        ;
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
                            "allowance": 100,
                             "receipt": {
                                "reference": "0x01",
                                "amount": 100,
                                "network": "ETHEREUM",
                                "thirdPartyName": "Coca Cola LTD",
                                "thirdPartyAccountNumber": "coca.cola.eth"
                            }
                        }
                        """.formatted(BTC))
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
                              "currencyId": "3f6e1c98-8659-493a-b941-943a803bd91f",
                              "balance": 100,
                              "allowance": 100,
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
                              "currencyId": "3f6e1c98-8659-493a-b941-943a803bd91f",
                              "balance": 0,
                              "allowance": 200,
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
                            "allowance": 100,
                            "receipt": {
                                "amount": 100,
                                "network": "ETHEREUM",
                                "reference": "0x01",
                                "thirdPartyName": "Coca Cola LTD",
                                "thirdPartyAccountNumber": "coca.cola.eth"
                            }
                        }
                        """.formatted(BTC))
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
                .jsonPath("$.balance").isEqualTo(0)
                .jsonPath("$.allowance").isEqualTo(100)
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
                            "allowance": 100,
                            "lockedUntil": "2024-01-31T00:00:00Z",
                            "receipt": {
                                "amount": 100,
                                "network": "ETHEREUM",
                                "reference": "0x01",
                                "thirdPartyName": "Coca Cola LTD",
                                "thirdPartyAccountNumber": "coca.cola.eth"
                            }
                        }
                        """.formatted(BTC))
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
                            "allowance": 100,
                            "receipt": {
                                "reference": "0x01",
                                "amount": 100,
                                "network": "ETHEREUM",
                                "thirdPartyName": "RedBull",
                                "thirdPartyAccountNumber": "red-bull.eth"
                            }
                        }
                        """.formatted(STRK))
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
                            "currency": "STRK",
                            "items": [{
                                "type": "PULL_REQUEST",
                                "id": "1703880973",
                                "number": 325,
                                "repoId": 698096830
                            }]
                        }
                        """.formatted(ofux.user().getGithubUserId()))
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
                .jsonPath("$.accounts[0].balance").isEqualTo(100)
                .jsonPath("$.accounts[0].allowance").isEqualTo(0)
                .jsonPath("$.accounts[0].awaitingPaymentAmount").isEqualTo(30)
        ;

        // When
        final var response = client.get()
                .uri(getApiURI(GET_PENDING_PAYMENTS))
                .header("Api-Key", apiKey())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.payments.size()").isEqualTo(1)
                .jsonPath("$.payments[0].amount").isEqualTo(30)
                .jsonPath("$.payments[0].currency.code").isEqualTo("STRK")
                .jsonPath("$.payments[0].currency.name").isEqualTo("StarkNet Token")
                .jsonPath("$.payments[0].currency.type").isEqualTo("CRYPTO")
                .jsonPath("$.payments[0].currency.standard").isEqualTo("ERC20")
                .jsonPath("$.payments[0].currency.blockchain").isEqualTo("ETHEREUM")
                .jsonPath("$.payments[0].currency.address").isEqualTo("0xCa14007Eff0dB1f8135f4C25B34De49AB0d42766")
                .jsonPath("$.payments[0].recipientAccountNumber").doesNotExist()
                .jsonPath("$.payments[0].rewardId").isEqualTo(rewardId.toString());

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
        client.get()
                .uri(getApiURI(GET_PENDING_PAYMENTS))
                .header("Api-Key", apiKey())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.payments.size()").isEqualTo(0)
        ;
    }

    @Test
    void should_not_list_cancelled_rewards() {
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
                            "allowance": 100,
                            "receipt": {
                                "reference": "0x01",
                                "amount": 100,
                                "network": "ETHEREUM",
                                "thirdPartyName": "RedBull",
                                "thirdPartyAccountNumber": "red-bull.eth"
                            }
                        }
                        """.formatted(STRK))
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

        // Given
        final var rewardId = client.post()
                .uri(getApiURI(PROJECTS_REWARDS.formatted(KAAPER)))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "recipientId": "%d",
                            "amount": 30,
                            "currency": "STRK",
                            "items": [{
                                "type": "PULL_REQUEST",
                                "id": "1703880973",
                                "number": 325,
                                "repoId": 698096830
                            }]
                        }
                        """.formatted(ofux.user().getGithubUserId()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(CreateRewardResponse.class)
                .returnResult().getResponseBody().getId();

        // Given
        client.delete()
                .uri(getApiURI(PROJECTS_REWARD.formatted(KAAPER, rewardId)))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                .expectStatus()
                .isNoContent();

        // When
        client.get()
                .uri(getApiURI(GET_PENDING_PAYMENTS))
                .header("Api-Key", apiKey())
                .exchange()
                .expectStatus()
                .isOk()
                // Then
                .expectBody()
                .jsonPath("$.payments.size()").isEqualTo(0)
        ;
    }
}
