package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.contract.model.ContributionActivityPageItemResponse;
import onlydust.com.marketplace.api.contract.model.ContributionActivityPageResponse;
import onlydust.com.marketplace.api.suites.tags.TagProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Consumer;

import static java.util.Collections.reverse;
import static org.assertj.core.api.Assertions.assertThat;


@TagProject
public class ContributionsApiIT extends AbstractMarketplaceApiIT {
    @Test
    void should_get_contributions() {
        // When
        client.get()
                .uri(getApiURI(CONTRIBUTIONS, Map.of("pageSize", "1")))
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 57700,
                          "totalItemNumber": 57700,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "contributions": [
                            {
                              "type": "PULL_REQUEST",
                              "repo": {
                                "id": 40652912,
                                "owner": "IonicaBizau",
                                "name": "node-cobol",
                                "description": ":tv: COBOL bridge for NodeJS which allows you to run COBOL code from NodeJS.",
                                "htmlUrl": "https://github.com/IonicaBizau/node-cobol"
                              },
                              "githubAuthor": {
                                "githubUserId": 1717382,
                                "login": "BernhardBehrendt",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/1717382?v=4"
                              },
                              "githubId": 42703857,
                              "githubNumber": 3,
                              "githubStatus": "MERGED",
                              "githubTitle": "German press article",
                              "githubHtmlUrl": "https://github.com/IonicaBizau/node-cobol/pull/3",
                              "githubBody": "by Alexander Neumann published on heise online\\n",
                              "githubLabels": null,
                              "lastUpdatedAt": "2023-11-24T10:32:31.695306Z",
                              "id": "93545fddecd2e42b0fccd23f62415dab09e37ef3ea1b131e68a472b67062cd48",
                              "createdAt": "2015-08-18T14:19:08Z",
                              "completedAt": "2015-08-18T16:25:53Z",
                              "activityStatus": "DONE",
                              "project": {
                                "id": "a852e8fd-de3c-4a14-813e-4b592af40d54",
                                "slug": "onlydust-marketplace",
                                "name": "OnlyDust Marketplace",
                                "logoUrl": null
                              },
                              "contributors": [
                                {
                                  "githubUserId": 1717382,
                                  "login": "BernhardBehrendt",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/1717382?v=4"
                                }
                              ],
                              "applicants": null,
                              "languages": null,
                              "linkedIssues": null,
                              "totalRewardedUsdAmount": null
                            }
                          ]
                        }
                        """);
    }

    @ParameterizedTest
    @CsvSource({
            "CREATED_AT,ASC",
            "CREATED_AT,DESC",
            "TYPE,ASC",
            "TYPE,DESC"
    })
    void should_get_contributions_with_sorting(String sort, String direction) {
        // When
        assertContributions(Map.of("sort", sort, "sortDirection", direction), r -> {
            // Then
            final var contributions = r.getContributions();
            if (direction.equals("DESC"))
                reverse(contributions);

            switch (sort) {
                case "CREATED_AT":
                    assertThat(contributions).isSortedAccordingTo(Comparator.comparing(ContributionActivityPageItemResponse::getCreatedAt));
                    break;
                case "TYPE":
                    assertThat(contributions).isSortedAccordingTo(Comparator.comparing(ContributionActivityPageItemResponse::getType));
                    break;
            }
        });
    }

    private WebTestClient.BodySpec<ContributionActivityPageResponse, ?> assertContributions(Map<String, String> params,
                                                                                            Consumer<ContributionActivityPageResponse> asserter) {
        return client.get()
                .uri(getApiURI(CONTRIBUTIONS, params))
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(ContributionActivityPageResponse.class)
                .consumeWith(r -> asserter.accept(r.getResponseBody()))
                ;
    }
}
