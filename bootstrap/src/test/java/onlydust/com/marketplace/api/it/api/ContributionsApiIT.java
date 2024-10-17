package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.contract.model.ContributionActivityPageItemResponse;
import onlydust.com.marketplace.api.contract.model.ContributionActivityPageResponse;
import onlydust.com.marketplace.api.contract.model.ContributionActivityStatus;
import onlydust.com.marketplace.api.contract.model.ContributionType;
import onlydust.com.marketplace.api.suites.tags.TagProject;
import org.assertj.core.api.AbstractListAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparing;
import static org.assertj.core.api.Assertions.assertThat;


@TagProject
public class ContributionsApiIT extends AbstractMarketplaceApiIT {
    @Test
    void should_get_contribution() {
        // When
        client.get()
                .uri(getApiURI(CONTRIBUTIONS_BY_ID.formatted("43506983"), Map.of("pageSize", "1")))
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
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
                            "githubUserId": 1814312,
                            "login": "krzkaczor",
                            "avatarUrl": "https://avatars.githubusercontent.com/u/1814312?v=4"
                          },
                          "githubId": 43506983,
                          "githubNumber": 8,
                          "githubStatus": "CLOSED",
                          "githubTitle": "Support for promises",
                          "githubHtmlUrl": "https://github.com/IonicaBizau/node-cobol/pull/8",
                          "githubBody": "https://github.com/IonicaBizau/node-cobol/issues/7\\n",
                          "githubLabels": null,
                          "lastUpdatedAt": "2023-11-24T10:32:31.763233Z",
                          "createdAt": "2015-08-27T11:38:25Z",
                          "completedAt": "2015-08-30T19:18:14Z",
                          "activityStatus": "DONE",
                          "project": {
                            "id": "a852e8fd-de3c-4a14-813e-4b592af40d54",
                            "slug": "onlydust-marketplace",
                            "name": "OnlyDust Marketplace",
                            "logoUrl": null
                          },
                          "contributors": [
                            {
                              "githubUserId": 1814312,
                              "login": "krzkaczor",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/1814312?v=4"
                            }
                          ],
                          "applicants": null,
                          "languages": [
                            {
                              "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                              "slug": "javascript",
                              "name": "Javascript",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                            }
                          ],
                          "linkedIssues": null,
                          "totalRewardedUsdAmount": null
                        }
                        """);
    }

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
                          "totalPageNumber": 45508,
                          "totalItemNumber": 45508,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "contributions": [
                            {
                              "type": "ISSUE",
                              "repo": {
                                "id": 21339768,
                                "owner": "reactjs",
                                "name": "react-tutorial",
                                "description": "Code from the React tutorial.",
                                "htmlUrl": "https://github.com/reactjs/react-tutorial"
                              },
                              "githubAuthor": {
                                "githubUserId": 116432,
                                "login": "simonwhitaker",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/116432?v=4"
                              },
                              "githubId": 39536039,
                              "githubNumber": 1,
                              "githubStatus": "COMPLETED",
                              "githubTitle": "Tutorial uses POST with SimpleHTTPServer",
                              "githubHtmlUrl": "https://github.com/reactjs/react-tutorial/issues/1",
                              "githubBody": "The tutorial suggests [using `python -m SimpleHTTPServer`](http://facebook.github.io/react/docs/tutorial.html#updating-state) to serve content, but subsequently introduces a mechanism for [submitting comments via a POST request](http://facebook.github.io/react/docs/tutorial.html#callbacks-as-props), which `SimpleHTTPServer` doesn't support. It appears that at this point the tutorial assumes you're using the sample node.js server packaged with [the tutorial's GitHub repo](https://github.com/reactjs/react-tutorial) but that isn't explained.\\n",
                              "githubLabels": null,
                              "lastUpdatedAt": "2023-11-24T10:34:07.322779Z",
                              "createdAt": "2014-08-05T16:19:44Z",
                              "completedAt": "2015-01-16T21:21:47Z",
                              "activityStatus": "DONE",
                              "project": null,
                              "contributors": null,
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
    @CsvSource({"CREATED_AT,ASC", "CREATED_AT,DESC", "TYPE,ASC", "TYPE,DESC"})
    void should_sort_contributions(String sort, String direction) {
        var comparator = switch (sort) {
            case "CREATED_AT" -> comparing(ContributionActivityPageItemResponse::getCreatedAt);
            case "TYPE" -> comparing(ContributionActivityPageItemResponse::getType);
            default -> throw new IllegalArgumentException("Invalid sort field: " + sort);
        };

        comparator = direction.equals("DESC") ? comparator.reversed() : comparator;

        assertContributions(Map.of("sort", sort, "sortDirection", direction))
                .isSortedAccordingTo(comparator);
    }

    @Test
    void should_filter_contributions() {
        assertContributions(Map.of("types", "PULL_REQUEST"))
                .extracting(ContributionActivityPageItemResponse::getType)
                .containsOnly(ContributionType.PULL_REQUEST);

        assertContributions(Map.of("ids", "43506983"))
                .extracting(ContributionActivityPageItemResponse::getGithubId)
                .containsOnly(43506983L);

        assertContributions(Map.of("projectIds", "1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e"))
                .extracting(c -> c.getProject().getName())
                .containsOnly("Cal.com");

        assertContributions(Map.of("projectSlugs", "calcom"))
                .extracting(c -> c.getProject().getName())
                .containsOnly("Cal.com");

        assertContributions(Map.of("statuses", "IN_PROGRESS"))
                .extracting(ContributionActivityPageItemResponse::getActivityStatus)
                .containsOnly(ContributionActivityStatus.IN_PROGRESS);

        assertContributions(Map.of("repoIds", "493591124"))
                .extracting(c -> c.getRepo().getName())
                .containsOnly("kaaper");

        assertContributions(Map.of("contributorIds", "43467246"))
                .extracting(ContributionActivityPageItemResponse::getContributors)
                .allMatch(contributors -> contributors.stream().anyMatch(c -> c.getLogin().equals("AnthonyBuisset")));

        assertContributions(Map.of("hasBeenRewarded", "true"))
                .extracting(ContributionActivityPageItemResponse::getTotalRewardedUsdAmount)
                .allMatch(a -> a.compareTo(BigDecimal.ZERO) > 0);

        assertContributions(Map.of("hasBeenRewarded", "false"))
                .extracting(ContributionActivityPageItemResponse::getTotalRewardedUsdAmount)
                .allMatch(a -> a.compareTo(BigDecimal.ZERO) == 0);
    }

    private AbstractListAssert<?, ? extends List<? extends ContributionActivityPageItemResponse>, ContributionActivityPageItemResponse> assertContributions(Map<String, String> params) {
        final var q = new HashMap<String, String>();
        q.put("pageSize", "100");
        q.putAll(params);

        final var contributions = client.get()
                .uri(getApiURI(CONTRIBUTIONS, q))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(ContributionActivityPageResponse.class)
                .returnResult()
                .getResponseBody()
                .getContributions();

        return assertThat(contributions).isNotEmpty();
    }
}
