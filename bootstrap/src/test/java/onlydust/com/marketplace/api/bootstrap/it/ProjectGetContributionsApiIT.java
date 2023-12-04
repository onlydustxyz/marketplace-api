package onlydust.com.marketplace.api.bootstrap.it;

import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

@ActiveProfiles({"hasura_auth"})
public class ProjectGetContributionsApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    HasuraUserHelper userHelper;

    private final static String KAAPER = "298a547f-ecb6-4ab2-8975-68f4e9bf7b39";

    @Test
    void should_get_project_contributions() {
        // Given
        final String jwt = userHelper.authenticateAnthony().jwt();

        // When
        client.get()
                .uri(getApiURI(PROJECTS_GET_CONTRIBUTIONS.formatted(KAAPER), Map.of("pageSize", "1")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .json("""
                        {
                          "contributions": [
                            {
                              "type": "PULL_REQUEST",
                              "repo": {
                                "id": 493591124,
                                "owner": "onlydustxyz",
                                "name": "kaaper",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/kaaper"
                              },
                              "githubAuthor": {
                                "githubUserId": 45264458,
                                "login": "abdelhamidbakhta",
                                "htmlUrl": "https://github.com/abdelhamidbakhta",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/45264458?v=4"
                              },
                              "githubNumber": 1,
                              "githubStatus": "MERGED",
                              "githubTitle": "\\uD83D\\uDCDD update project description",
                              "githubHtmlUrl": "https://github.com/onlydustxyz/kaaper/pull/1",
                              "githubBody": null,
                              "githubCodeReviewOutcome": null,
                              "id": "f78954de50fec850fa0726247457c790387a866872c00653339f09ca2f32690f",
                              "createdAt": "2022-05-18T09:40:28Z",
                              "completedAt": "2022-05-18T09:40:33Z",
                              "status": "COMPLETED",
                              "githubPullRequestReviewState": "PENDING_REVIEWER",
                              "rewardIds": [],
                              "contributor": {
                                "githubUserId": 45264458,
                                "login": "abdelhamidbakhta",
                                "htmlUrl": "https://github.com/abdelhamidbakhta",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/45264458?v=4",
                                "isRegistered": null
                              },
                              "links": []
                            }
                          ],
                          "hasMore": true,
                          "totalPageNumber": 3604,
                          "totalItemNumber": 3604,
                          "nextPageIndex": 1
                        }
                        """);
    }

    @Test
    void should_get_project_contributions_filtered_by_date() {
        // Given
        final String jwt = userHelper.authenticateAnthony().jwt();

        // When
        client.get()
                .uri(getApiURI(PROJECTS_GET_CONTRIBUTIONS.formatted(KAAPER), Map.of(
                        "pageSize", "100",
                        "fromDate", "2023-02-21",
                        "toDate", "2023-02-21"
                        )))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus().isOk() // Make sure we get all results
                .expectBody()
                // we have at least one correct date
                .jsonPath("$.contributions[?(@.createdAt >= '2023-02-21')]").exists()
                .jsonPath("$.contributions[?(@.createdAt < '2023-02-22')]").exists()
                // we do not have any incorrect date
                .jsonPath("$.contributions[?(@.createdAt < '2023-02-21')]").doesNotExist()
                .jsonPath("$.contributions[?(@.createdAt > '2023-02-22')]").doesNotExist()
                ;
    }


    @Test
    void should_get_project_contributions_ordered_by_contributor_login() {
        // Given
        final String jwt = userHelper.authenticateAnthony().jwt();

        // When
        client.get()
                .uri(getApiURI(PROJECTS_GET_CONTRIBUTIONS.formatted(KAAPER), Map.of(
                        "pageSize", "100",
                        "sort", "CONTRIBUTOR_LOGIN"
                )))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.contributions[0].contributor.login").isEqualTo("0xAurelou")
                .jsonPath("$.contributions[99].contributor.login").isEqualTo("AnthonyBuisset")
        ;

        client.get()
                .uri(getApiURI(PROJECTS_GET_CONTRIBUTIONS.formatted(KAAPER), Map.of(
                        "pageSize", "100",
                        "sort", "CONTRIBUTOR_LOGIN",
                        "direction", "DESC"
                )))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.contributions[0].contributor.login").isEqualTo("tekkac")
                .jsonPath("$.contributions[99].contributor.login").isEqualTo("tdelabro")
        ;
    }

    @Test
    void should_reject_as_bad_request_if_provided_date_is_ill_formed() {
        // Given
        final String jwt = userHelper.authenticateAnthony().jwt();

        // When
        client.get()
                .uri(getApiURI(PROJECTS_GET_CONTRIBUTIONS.formatted(KAAPER), Map.of("fromDate", "2023-55-21")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus().isBadRequest()
        ;

        // When
        client.get()
                .uri(getApiURI(PROJECTS_GET_CONTRIBUTIONS.formatted(KAAPER), Map.of("toDate", "2023-55-21")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus().isBadRequest()
        ;
    }
}
