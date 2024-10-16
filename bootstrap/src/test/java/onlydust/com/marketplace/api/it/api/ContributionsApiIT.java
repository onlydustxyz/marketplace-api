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
