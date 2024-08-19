package onlydust.com.marketplace.api.it.api;

import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.api.suites.tags.TagProject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static onlydust.com.marketplace.api.helper.CurrencyHelper.*;
import static onlydust.com.marketplace.api.helper.DateHelper.at;


@TagProject
public class ProjectGetStatsApiIT extends AbstractMarketplaceApiIT {
    ProjectId projectId;

    @BeforeEach
    void setUp() {
        final var program = programHelper.create(userAuthHelper.create());
        final var programId = SponsorId.of(program.id());

        final var projectLead = userAuthHelper.create();
        projectId = projectHelper.create(projectLead);
        final var recipient = userAuthHelper.create();
        final var recipientId = GithubUserId.of(recipient.user().getGithubUserId());

        accountingHelper.createSponsorAccount(programId, 1_000, USDC);
        accountingHelper.createSponsorAccount(programId, 2, ETH);
        accountingHelper.createSponsorAccount(programId, 3, BTC);

        at("2024-06-15T00:00:00Z", () -> {
            accountingHelper.grant(programId, projectId, 1_000, USDC);
            rewardHelper.create(projectId, projectLead, recipientId, 200, USDC);
            rewardHelper.create(projectId, projectLead, recipientId, 100, USDC);

            final var repo = githubHelper.createRepo();
            projectHelper.addRepo(projectId, repo.getId());
            githubHelper.createPullRequest(repo, recipient);
            githubHelper.createPullRequest(repo, recipient);
        });

        at("2024-07-12T00:00:00Z", () -> {
            accountingHelper.grant(programId, projectId, 2, ETH);
            rewardHelper.create(projectId, projectLead, recipientId, 1, ETH);
            rewardHelper.create(projectId, projectLead, recipientId, 1, ETH);
        });

        at("2024-08-03T00:00:00Z", () -> {
            accountingHelper.grant(programId, projectId, 3, BTC);
            rewardHelper.create(projectId, projectLead, recipientId, 1, BTC);
        });
    }

    @AfterAll
    @SneakyThrows
    static void restore() {
        restoreIndexerDump();
    }

    @Test
    void should_get_project_stats() {
        client.get()
                .uri(getApiURI(PROJECT_STATS.formatted(projectId), Map.of(
                        "fromDate", "2024-06-01",
                        "toDate", "2024-07-01"
                )))
                .exchange()
                // Then
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "activeContributorCount": 1,
                          "mergedPrCount": 2,
                          "rewardCount": 2,
                          "totalGranted": {
                            "totalUsdEquivalent": 4573.97,
                            "totalPerCurrency": [
                              {
                                "amount": 3,
                                "prettyAmount": 3,
                                "currency": {
                                  "id": "3f6e1c98-8659-493a-b941-943a803bd91f",
                                  "code": "BTC",
                                  "name": "Bitcoin",
                                  "logoUrl": null,
                                  "decimals": 8
                                },
                                "usdEquivalent": null,
                                "usdConversionRate": null
                              },
                              {
                                "amount": 2,
                                "prettyAmount": 2,
                                "currency": {
                                  "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                  "code": "ETH",
                                  "name": "Ether",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 3563.97,
                                "usdConversionRate": 1781.983987
                              },
                              {
                                "amount": 1000,
                                "prettyAmount": 1000,
                                "currency": {
                                  "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                  "code": "USDC",
                                  "name": "USD Coin",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                  "decimals": 6
                                },
                                "usdEquivalent": 1010.00,
                                "usdConversionRate": 1.010001
                              }
                            ]
                          },
                          "totalRewarded": {
                            "totalUsdEquivalent": 3866.97,
                            "totalPerCurrency": [
                              {
                                "amount": 1,
                                "prettyAmount": 1,
                                "currency": {
                                  "id": "3f6e1c98-8659-493a-b941-943a803bd91f",
                                  "code": "BTC",
                                  "name": "Bitcoin",
                                  "logoUrl": null,
                                  "decimals": 8
                                },
                                "usdEquivalent": null,
                                "usdConversionRate": null
                              },
                              {
                                "amount": 2,
                                "prettyAmount": 2,
                                "currency": {
                                  "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                  "code": "ETH",
                                  "name": "Ether",
                                  "logoUrl": null,
                                  "decimals": 18
                                },
                                "usdEquivalent": 3563.97,
                                "usdConversionRate": 1781.983987
                              },
                              {
                                "amount": 300,
                                "prettyAmount": 300,
                                "currency": {
                                  "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                  "code": "USDC",
                                  "name": "USD Coin",
                                  "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                  "decimals": 6
                                },
                                "usdEquivalent": 303.00,
                                "usdConversionRate": 1.010001
                              }
                            ]
                          }
                        }
                        """);
    }
}
