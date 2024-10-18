package onlydust.com.marketplace.api.it.api;

import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.Country;
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

        var bp = billingProfileHelper.verify(antho, Country.fromIso3("FRA"), Set.of(onlyDust));
        billingProfileHelper.addPayoutInfo(bp.id(), PayoutInfo.builder()
                .ethWallet(Ethereum.wallet("antho.eth"))
                .starknetAddress(new StarknetAccountAddress("0x123"))
                .build());

        bp = billingProfileHelper.verify(pierre, Country.fromIso3("FRA"), Set.of(madara));
        billingProfileHelper.addPayoutInfo(bp.id(), PayoutInfo.builder()
                .ethWallet(Ethereum.wallet("pierre.eth"))
                .starknetAddress(new StarknetAccountAddress("0x666"))
                .build());

        at("2024-06-01T00:00:00Z", () -> rewardHelper.create(onlyDust, pierre, antho.githubUserId(), 1, STRK, List.of(
                // contributionUUID = 014a5c12-8973-3dd8-a530-98ad40be1dec
                RequestRewardCommand.Item.builder()
                        .id("1693393852")
                        .number(8634L)
                        .repoId(350360184L)
                        .type(RequestRewardCommand.Item.Type.issue)
                        .build()
        )));
        at("2024-06-01T00:00:00Z", () -> rewardHelper.create(onlyDust, pierre, antho.githubUserId(), 2, ETH));
        at("2024-06-01T00:00:00Z", () -> rewardHelper.create(onlyDust, pierre, james.githubUserId(), 3, ETH));

        at("2024-06-03T00:00:00Z", () -> rewardHelper.create(madara, hayden, antho.githubUserId(), 4, ETH));
        at("2024-06-10T00:00:00Z", () -> rewardHelper.create(madara, hayden, pierre.githubUserId(), 5, ETH));
    }

    @Test
    void should_list_pierre_rewards() {
        // When
        client.get()
                .uri(getApiURI(String.format(GET_REWARDS), Map.of(
                        "pageIndex", "0",
                        "pageSize", "5")
                ))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isEqualTo(HttpStatus.OK)
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 4,
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
                              "status": "INDIVIDUAL_LIMIT_REACHED",
                              "from": {
                                "login": "hayden",
                                "isRegistered": true
                              },
                              "to": {
                                "login": "pierre",
                                "isRegistered": true
                              },
                              "requestedAt": "2024-06-10T00:00:00Z",
                              "processedAt": null,
                              "unlockDate": null
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
                                "login": "pierre",
                                "isRegistered": true
                              },
                              "to": {
                                "login": "james",
                                "isRegistered": true
                              },
                              "requestedAt": "2024-06-01T00:00:00Z",
                              "processedAt": null,
                              "unlockDate": null
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
                                "login": "pierre",
                                "isRegistered": true
                              },
                              "to": {
                                "login": "antho",
                                "isRegistered": true
                              },
                              "requestedAt": "2024-06-01T00:00:00Z",
                              "processedAt": null,
                              "unlockDate": null
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
                              "status": "PENDING_CONTRIBUTOR",
                              "from": {
                                "login": "pierre",
                                "isRegistered": true
                              },
                              "to": {
                                "login": "antho",
                                "isRegistered": true
                              },
                              "requestedAt": "2024-06-01T00:00:00Z",
                              "processedAt": null,
                              "unlockDate": null
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
                response -> response.getRewards().forEach(reward -> assertThat(reward.getProjectId()).isEqualTo(onlyDust.value())), true
        );
        test_get_rewards(Map.of("recipientIds", pierre.githubUserId().toString()),
                response -> response.getRewards().forEach(reward -> assertThat(reward.getTo().getGithubUserId()).isEqualTo(pierre.githubUserId().value())), true
        );
        test_get_rewards(Map.of("recipientIds", james.githubUserId().toString()),
                response -> response.getRewards().forEach(reward -> assertThat(reward.getTo().getGithubUserId()).isEqualTo(james.githubUserId().value())), true
        );
        test_get_rewards(Map.of("statuses", "INDIVIDUAL_LIMIT_REACHED"),
                response -> response.getRewards().forEach(reward -> assertThat(reward.getStatus()).isEqualTo(RewardStatusContract.INDIVIDUAL_LIMIT_REACHED)),
                true
        );
        test_get_rewards(Map.of("contributionUUIDs", "014a5c12-8973-3dd8-a530-98ad40be1dec"),
                response -> response.getRewards().forEach(reward -> {
                    assertThat(reward.getFrom().getId()).isEqualTo(pierre.userId().value());
                    assertThat(reward.getTo().getGithubUserId()).isEqualTo(antho.githubUserId().value());
                    assertThat(reward.getAmount().getCurrency().getCode()).isEqualTo("STRK");
                }), true
        );
    }

}
