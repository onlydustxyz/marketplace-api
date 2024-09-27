package onlydust.com.marketplace.api.it.api;

import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.api.suites.tags.TagBI;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.project.domain.port.input.ProjectFacadePort;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static onlydust.com.marketplace.api.helper.CurrencyHelper.*;
import static onlydust.com.marketplace.api.helper.DateHelper.at;


@TagBI
public class ProjectGetStatsApiIT extends AbstractMarketplaceApiIT {
    private static ProjectId projectId;
    private static String projectSlug;

    @Autowired
    ProjectFacadePort projectFacadePort;

    private static final AtomicBoolean setupDone = new AtomicBoolean();
    private static final String PROJECT_FINANCIAL_RESPONSE = """
            {
              "totalAvailable": {
                "totalUsdEquivalent": 707.00,
                "totalPerCurrency": [
                  {
                    "amount": 2,
                    "prettyAmount": 2,
                    "currency": {
                      "id": "3f6e1c98-8659-493a-b941-943a803bd91f",
                      "code": "BTC",
                      "name": "Bitcoin",
                      "logoUrl": null,
                      "decimals": 8
                    },
                    "usdEquivalent": null,
                    "usdConversionRate": null,
                    "ratio": null
                  },
                  {
                    "amount": 700,
                    "prettyAmount": 700,
                    "currency": {
                      "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                      "code": "USDC",
                      "name": "USD Coin",
                      "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                      "decimals": 6
                    },
                    "usdEquivalent": 707.00,
                    "usdConversionRate": 1.010001,
                    "ratio": 100
                  },
                  {
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
                    "usdConversionRate": 1781.983987,
                    "ratio": 0
                  }
                ]
              },
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
                    "usdConversionRate": null,
                    "ratio": null
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
                    "usdConversionRate": 1781.983987,
                    "ratio": 78
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
                    "usdConversionRate": 1.010001,
                    "ratio": 22
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
                    "usdConversionRate": null,
                    "ratio": null
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
                    "usdConversionRate": 1781.983987,
                    "ratio": 92
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
                    "usdConversionRate": 1.010001,
                    "ratio": 8
                  }
                ]
              }
            }
            """;

    @BeforeEach
    void setUp() {
        if (setupDone.compareAndExchange(false, true)) return;

        final var sponsor = sponsorHelper.create();
        final var sponsorId = SponsorId.of(sponsor.id().value());
        final var program = programHelper.create(sponsorId, userAuthHelper.create());
        final var programId = ProgramId.of(program.id().value());

        final var projectLead = userAuthHelper.create();
        final var project = projectHelper.create(projectLead);
        projectId = project.getLeft();
        projectSlug = project.getRight();
        final var newUser = userAuthHelper.create();
        final var oldUser = userAuthHelper.create();
        final var recipientId = GithubUserId.of(newUser.user().getGithubUserId());

        accountingHelper.createSponsorAccount(sponsorId, 1_000, USDC);
        accountingHelper.createSponsorAccount(sponsorId, 2, ETH);
        accountingHelper.createSponsorAccount(sponsorId, 3, BTC);

        accountingHelper.allocate(sponsorId, programId, 1_000, USDC);
        accountingHelper.allocate(sponsorId, programId, 2, ETH);
        accountingHelper.allocate(sponsorId, programId, 3, BTC);

        final var repo = githubHelper.createRepo();
        projectHelper.addRepo(projectId, repo.getId());

        at("2022-06-15T00:00:00Z", () -> {
            githubHelper.createPullRequest(repo, oldUser);
        });

        at("2024-06-15T00:00:00Z", () -> {
            accountingHelper.grant(programId, projectId, 1_000, USDC);
            rewardHelper.create(projectId, projectLead, recipientId, 200, USDC);
            rewardHelper.create(projectId, projectLead, recipientId, 100, USDC);

            githubHelper.createPullRequest(repo, oldUser);
            githubHelper.createPullRequest(repo, newUser);
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

        projectFacadePort.refreshStats();
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
                          "activeContributorCount": 2,
                          "onboardedContributorCount": 1,
                          "mergedPrCount": 2
                        }
                        """);
    }

    @Test
    void should_get_project_financial_details() {
        client.get()
                .uri(getApiURI(PROJECT_FINANCIAL.formatted(projectId)))
                .exchange()
                // Then
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(projectId.toString())
                .jsonPath("$.slug").isEqualTo(projectSlug)
                .jsonPath("$.name").isNotEmpty()
                .json(PROJECT_FINANCIAL_RESPONSE);
    }

    @Test
    void should_get_project_financial_details_by_slug() {
        client.get()
                .uri(getApiURI(PROJECT_FINANCIAL_BY_SLUG.formatted(projectSlug)))
                .exchange()
                // Then
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(projectId.toString())
                .jsonPath("$.slug").isEqualTo(projectSlug)
                .jsonPath("$.name").isNotEmpty()
                .json(PROJECT_FINANCIAL_RESPONSE);
    }
}
