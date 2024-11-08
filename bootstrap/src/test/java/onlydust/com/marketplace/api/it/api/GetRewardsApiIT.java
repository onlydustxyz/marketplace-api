package onlydust.com.marketplace.api.it.api;

import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.Country;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.accounting.domain.model.Payment;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.api.contract.model.RewardPageResponse;
import onlydust.com.marketplace.api.contract.model.RewardStatusContract;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagMe;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetAccountAddress;
import onlydust.com.marketplace.project.domain.model.ProjectCategory;
import onlydust.com.marketplace.project.domain.model.RequestRewardCommand;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static onlydust.com.marketplace.api.helper.CurrencyHelper.*;
import static onlydust.com.marketplace.api.helper.DateHelper.at;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;


@TagMe
public class GetRewardsApiIT extends AbstractMarketplaceApiIT {

    private static final AtomicBoolean setupDone = new AtomicBoolean();
    private static UUID universe;
    private static UUID starknet;
    private static ProgramId explorationTeam;
    private static ProgramId nethermind;
    private static ProgramId ethGrantingProgram;
    private static UserAuthHelper.AuthenticatedUser caller;
    private static UserAuthHelper.AuthenticatedUser antho;
    private static UserAuthHelper.AuthenticatedUser pierre;
    private static UserAuthHelper.AuthenticatedUser mehdi;
    private static UserAuthHelper.AuthenticatedUser hayden;
    private static UserAuthHelper.AuthenticatedUser abdel;
    private static UserAuthHelper.AuthenticatedUser james;
    private static ProjectCategory defi;
    private static ProjectId onlyDust;
    private static ProjectId madara;
    private static BillingProfile anthoBillingProfile;
    private static BillingProfile pierreBillingProfile;
    private static UUID invoiceId = UUID.randomUUID();

    @AfterAll
    @SneakyThrows
    static void restore() {
        restoreIndexerDump();
    }

    @BeforeEach
    synchronized void setup() {
        if (setupDone.compareAndExchange(false, true)) return;

        currencyHelper.setQuote("2020-12-31T00:00:00Z", STRK, USD, BigDecimal.valueOf(0.5));
        currencyHelper.setQuote("2020-12-31T00:00:00Z", ETH, USD, BigDecimal.valueOf(2));

        caller = userAuthHelper.create("olivier");
        antho = userAuthHelper.create("antho");
        pierre = userAuthHelper.create("pierre");
        mehdi = userAuthHelper.create("mehdi");
        billingProfileHelper.verify(mehdi, Country.fromIso3("MAR"));
        hayden = userAuthHelper.create("hayden");
        billingProfileHelper.verify(hayden, Country.fromIso3("GBR"));
        abdel = userAuthHelper.create("abdel");
        billingProfileHelper.verify(abdel, Country.fromIso3("MAR"));
        james = userAuthHelper.create("james");
        billingProfileHelper.verify(james, Country.fromIso3("GBR"));

        universe = ecosystemHelper.create("Universe ecosystem", caller).id();
        starknet = ecosystemHelper.create("Starknet ecosystem").id();

        final var starknetFoundation = sponsorHelper.create("The Starknet Foundation");
        accountingHelper.createSponsorAccount(starknetFoundation.id(), 10_000, STRK);

        explorationTeam = programHelper.create(starknetFoundation.id(), "Starkware Exploration Team").id();
        accountingHelper.allocate(starknetFoundation.id(), explorationTeam, 7_000, STRK);
        nethermind = programHelper.create(starknetFoundation.id(), "Nethermind").id();
        accountingHelper.allocate(starknetFoundation.id(), nethermind, 3_000, STRK);

        final var ethFoundation = sponsorHelper.create("The Ethereum Foundation");
        accountingHelper.createSponsorAccount(ethFoundation.id(), 10_000, ETH);
        ethGrantingProgram = programHelper.create(ethFoundation.id(), "Ethereum Granting Program").id();
        accountingHelper.allocate(ethFoundation.id(), ethGrantingProgram, 3_000, ETH);

        final var od = projectHelper.create(pierre, "OnlyDust", List.of(universe));
        onlyDust = od.getLeft();
        at("2021-01-01T00:00:00Z", () -> accountingHelper.grant(nethermind, onlyDust, 100, STRK));
        at("2021-01-05T00:00:00Z", () -> accountingHelper.grant(ethGrantingProgram, onlyDust, 25, ETH));

        madara = projectHelper.create(hayden, "Madara", List.of(universe, starknet)).getLeft();
        at("2021-01-06T00:00:00Z", () -> accountingHelper.grant(explorationTeam, madara, 120, STRK));
        at("2021-01-05T00:00:00Z", () -> accountingHelper.grant(ethGrantingProgram, madara, 25, ETH));

        anthoBillingProfile = billingProfileHelper.verify(antho, Country.fromIso3("FRA"), Set.of(onlyDust));
        billingProfileHelper.addPayoutInfo(anthoBillingProfile.id(), PayoutInfo.builder()
                .ethWallet(Ethereum.wallet("antho.eth"))
                .starknetAddress(new StarknetAccountAddress("0x123"))
                .build());

        pierreBillingProfile = billingProfileHelper.verifyCompany(pierre, Country.fromIso3("FRA"), Set.of(madara), Set.of(), Set.of(antho.userId()));
        billingProfileHelper.addPayoutInfo(pierreBillingProfile.id(), PayoutInfo.builder()
                .ethWallet(Ethereum.wallet("pierre.eth"))
                .starknetAddress(new StarknetAccountAddress("0x666"))
                .build());

        // Antho selects pierreBillingProfile for project madara, so Pierre will be able to view the rewards of Antho on madara
        billingProfileHelper.selectForProject(antho.userId(), pierreBillingProfile.id(), madara);

        at("2024-06-01T00:00:00Z", () -> rewardHelper.create(onlyDust, pierre, antho.githubUserId(), 1, STRK, List.of(
                // contributionUUID = f9360345-3145-33f0-a9b0-cbaa3ba78a4e
                RequestRewardCommand.Item.builder()
                        .id("1981399962")
                        .number(12264L)
                        .repoId(350360184L)
                        .type(RequestRewardCommand.Item.Type.issue)
                        .build()
        )));
        at("2024-06-01T00:01:00Z", () -> {
            final var rewardId = rewardHelper.create(onlyDust, pierre, antho.githubUserId(), 2, ETH);
            accountingHelper.addInvoice(rewardId.value(), invoiceId, Date.from(ZonedDateTime.parse("2024-06-02T00:00:00Z").toInstant()));
            accountingHelper.setPaid(rewardId.value(), Date.from(ZonedDateTime.parse("2024-06-03T00:00:00Z").toInstant()),
                    new Payment.Reference(ZonedDateTime.parse("2024-06-03T00:00:00Z"), Network.ETHEREUM, "0x123", "Antho", "antho.eth"));
        });
        at("2024-06-01T00:02:00Z", () -> rewardHelper.create(onlyDust, pierre, james.githubUserId(), 3, ETH));

        at("2024-06-03T00:00:00Z", () -> rewardHelper.create(madara, hayden, antho.githubUserId(), 4, ETH));
        at("2024-06-10T00:00:00Z", () -> rewardHelper.create(madara, hayden, pierre.githubUserId(), 5, ETH));
        at("2024-06-03T00:00:00Z", () -> rewardHelper.create(madara, hayden, james.githubUserId(), 6, ETH));
    }

    @Test
    void should_list_pierre_rewards() {
        // When
        client.get()
                .uri(getApiURI(String.format(GET_REWARDS), Map.of(
                        "pageIndex", "0",
                        "pageSize", "10")
                ))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isEqualTo(HttpStatus.OK)
                .expectBody()
                .jsonPath("$.rewards[0].billingProfileId").isEqualTo(pierreBillingProfile.id().toString())
                .jsonPath("$.rewards[1].billingProfileId").isEqualTo(pierreBillingProfile.id().toString())
                .jsonPath("$.rewards[2].billingProfileId").isEqualTo(null)
                .jsonPath("$.rewards[3].billingProfileId").isEqualTo(anthoBillingProfile.id().toString())
                .jsonPath("$.rewards[4].billingProfileId").isEqualTo(anthoBillingProfile.id().toString())
                .jsonPath("$.rewards[3].invoiceId").isEqualTo(invoiceId.toString())
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 5,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "rewards": [
                            {
                              "amount": {
                                "amount": 5,
                                "prettyAmount": 5,
                                "currency": {
                                  "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                  "code": "ETH",
                                  "name": "Ether",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 8909.90,
                                "usdConversionRate": 1781.98
                              },
                              "status": "LOCKED",
                              "from": {
                                "login": "hayden"
                              },
                              "to": {
                                "login": "pierre"
                              },
                              "requestedAt": "2024-06-10T00:00:00Z",
                              "processedAt": null,
                              "unlockDate": null,
                              "invoiceId": null,
                              "transactionReference": null,
                              "transactionReferenceLink": null,
                              "items": [
                                "052b4d04-401d-3ed3-97d0-e278d950ce4e"
                              ]
                            },
                            {
                              "amount": {
                                "amount": 4,
                                "prettyAmount": 4,
                                "currency": {
                                  "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                  "code": "ETH",
                                  "name": "Ether",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 7127.92,
                                "usdConversionRate": 1781.98
                              },
                              "status": "LOCKED",
                              "from": {
                                "login": "hayden"
                              },
                              "to": {
                                "login": "antho"
                              },
                              "requestedAt": "2024-06-03T00:00:00Z",
                              "processedAt": null,
                              "unlockDate": null,
                              "invoiceId": null,
                              "transactionReference": null,
                              "transactionReferenceLink": null,
                              "items": [
                                "052b4d04-401d-3ed3-97d0-e278d950ce4e"
                              ]
                            },
                            {
                              "amount": {
                                "amount": 3,
                                "prettyAmount": 3,
                                "currency": {
                                  "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                  "code": "ETH",
                                  "name": "Ether",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 5345.94,
                                "usdConversionRate": 1781.98
                              },
                              "status": "PENDING_CONTRIBUTOR",
                              "from": {
                                "login": "pierre"
                              },
                              "to": {
                                "login": "james"
                              },
                              "requestedAt": "2024-06-01T00:02:00Z",
                              "processedAt": null,
                              "unlockDate": null,
                              "billingProfileId": null,
                              "invoiceId": null,
                              "transactionReference": null,
                              "transactionReferenceLink": null,
                              "items": [
                                "052b4d04-401d-3ed3-97d0-e278d950ce4e"
                              ]
                            },
                            {
                              "amount": {
                                "amount": 2,
                                "prettyAmount": 2,
                                "currency": {
                                  "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                  "code": "ETH",
                                  "name": "Ether",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 3563.96,
                                "usdConversionRate": 1781.98
                              },
                              "status": "COMPLETE",
                              "from": {
                                "login": "pierre"
                              },
                              "to": {
                                "login": "antho"
                              },
                              "requestedAt": "2024-06-01T00:01:00Z",
                              "processedAt": "2024-06-03T00:00:00Z",
                              "unlockDate": null,
                              "transactionReference": "0x123",
                              "transactionReferenceLink": "https://etherscan.io/tx/0x0123",
                              "items": [
                                "052b4d04-401d-3ed3-97d0-e278d950ce4e"
                              ]
                            },
                            {
                              "amount": {
                                "amount": 1,
                                "prettyAmount": 1,
                                "currency": {
                                  "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                                  "code": "STRK",
                                  "name": "StarkNet Token",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 0.5,
                                "usdConversionRate": 0.5
                              },
                              "status": "PENDING_CONTRIBUTOR",
                              "from": {
                                "login": "pierre"
                              },
                              "to": {
                                "login": "antho"
                              },
                              "requestedAt": "2024-06-01T00:00:00Z",
                              "processedAt": null,
                              "unlockDate": null,
                              "invoiceId": null,
                              "transactionReference": null,
                              "transactionReferenceLink": null,
                              "items": [
                                "f9360345-3145-33f0-a9b0-cbaa3ba78a4e"
                              ]
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_list_pierre_rewards_as_recipient_and_bp_admin_only() {
        // When
        client.get()
                .uri(getApiURI(String.format(GET_REWARDS), Map.of(
                        "pageIndex", "0",
                        "pageSize", "10",
                        "includeProjectLeds", "false")
                ))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isEqualTo(HttpStatus.OK)
                .expectBody()
                .jsonPath("$.rewards[0].billingProfileId").isEqualTo(pierreBillingProfile.id().toString())
                .jsonPath("$.rewards[1].billingProfileId").isEqualTo(pierreBillingProfile.id().toString())
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "rewards": [
                            {
                              "amount": {
                                "amount": 4,
                                "prettyAmount": 4,
                                "currency": {
                                  "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                  "code": "ETH",
                                  "name": "Ether",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 7127.92,
                                "usdConversionRate": 1781.98
                              },
                              "status": "LOCKED",
                              "from": {
                                "login": "hayden"
                              },
                              "to": {
                                "login": "antho"
                              },
                              "requestedAt": "2024-06-03T00:00:00Z",
                              "processedAt": null,
                              "unlockDate": null,
                              "invoiceId": null,
                              "transactionReference": null,
                              "transactionReferenceLink": null,
                              "items": [
                                "052b4d04-401d-3ed3-97d0-e278d950ce4e"
                              ]
                            },{
                              "amount": {
                                "amount": 5,
                                "prettyAmount": 5,
                                "currency": {
                                  "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                  "code": "ETH",
                                  "name": "Ether",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 8909.90,
                                "usdConversionRate": 1781.98
                              },
                              "status": "LOCKED",
                              "from": {
                                "login": "hayden"
                              },
                              "to": {
                                "login": "pierre"
                              },
                              "requestedAt": "2024-06-10T00:00:00Z",
                              "processedAt": null,
                              "unlockDate": null,
                              "invoiceId": null,
                              "transactionReference": null,
                              "transactionReferenceLink": null,
                              "items": [
                                "052b4d04-401d-3ed3-97d0-e278d950ce4e"
                              ]
                            }
                          ]
                        }
                        """);
    }

    private void test_get_rewards(Map<String, String> queryParamsWithValues, Consumer<RewardPageResponse> asserter, boolean assertNotEmpty) {
        final var queryParams = new HashMap<String, String>();
        queryParams.put("pageIndex", "0");
        queryParams.put("pageSize", "100");
        queryParams.putAll(queryParamsWithValues);
        final var response = client.get()
                .uri(getApiURI(GET_REWARDS, queryParams))
                .header("Authorization", BEARER_PREFIX + userAuthHelper.signInUser(pierre).jwt())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(RewardPageResponse.class).returnResult().getResponseBody();
        if (assertNotEmpty)
            assertThat(response.getRewards()).isNotEmpty();
        asserter.accept(response);
    }

    @Test
    public void should_get_rewards_with_filters() {
        test_get_rewards(Map.of("projectIds", onlyDust.toString()),
                response -> response.getRewards().forEach(reward -> {
                    assertThat(reward.getProject().getId()).isEqualTo(onlyDust.value());
                    assertThat(reward.getProject().getName()).contains("OnlyDust");
                }), true
        );
        test_get_rewards(Map.of("billingProfileIds", anthoBillingProfile.id().toString()),
                response -> response.getRewards().forEach(reward -> assertThat(reward.getBillingProfileId()).isEqualTo(anthoBillingProfile.id().value())), true
        );
        test_get_rewards(Map.of("recipientIds", pierre.githubUserId().toString()),
                response -> response.getRewards().forEach(reward -> assertThat(reward.getTo().getGithubUserId()).isEqualTo(pierre.githubUserId().value())), true
        );
        test_get_rewards(Map.of("recipientIds", james.githubUserId().toString()),
                response -> response.getRewards().forEach(reward -> assertThat(reward.getTo().getGithubUserId()).isEqualTo(james.githubUserId().value())), true
        );
        test_get_rewards(Map.of("statuses", "LOCKED"),
                response -> response.getRewards().forEach(reward -> assertThat(reward.getStatus() == RewardStatusContract.LOCKED || reward.getStatus() == RewardStatusContract.PENDING_CONTRIBUTOR)),
                true
        );
        test_get_rewards(Map.of("statuses", "GEO_BLOCKED"),
                response -> response.getRewards().forEach(reward -> assertThat(reward.getStatus() == RewardStatusContract.GEO_BLOCKED)),
                false
        );
        test_get_rewards(Map.of("contributionUUIDs", "f9360345-3145-33f0-a9b0-cbaa3ba78a4e"),
                response -> response.getRewards().forEach(reward -> {
                    assertThat(reward.getFrom().getId()).isEqualTo(pierre.userId().value());
                    assertThat(reward.getTo().getGithubUserId()).isEqualTo(antho.githubUserId().value());
                    assertThat(reward.getAmount().getCurrency().getCode()).isEqualTo("STRK");
                }), true
        );
    }

}
