package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.contract.model.ContributorResponse;
import onlydust.com.marketplace.api.contract.model.ContributorSearchResponse;
import onlydust.com.marketplace.api.suites.tags.TagProject;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@TagProject
public class ContributorSearchIT extends AbstractMarketplaceApiIT {
    final static String login = "antho";
    final static UUID projectId = UUID.fromString("298a547f-ecb6-4ab2-8975-68f4e9bf7b39"); // kaaper
    final static String PROJECTS_SEARCH_CONTRIBUTORS_RESPONSE = """
            {
              "internalContributors": [
                {
                  "githubUserId": 43467246,
                  "login": "AnthonyBuisset",
                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                  "isRegistered": true,
                  "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4"
                }
              ],
              "externalContributors": [
                {
                  "githubUserId": 31220,
                  "login": "antho",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/31220?v=4",
                  "isRegistered": false,
                  "id": null
                },
                {
                  "githubUserId": 3982077,
                  "login": "anthonychu",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/3982077?v=4",
                  "isRegistered": false,
                  "id": null
                },
                {
                  "githubUserId": 36125,
                  "login": "anthonyshort",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/36125?v=4",
                  "isRegistered": false,
                  "id": null
                },
                {
                  "githubUserId": 16854916,
                  "login": "AnthoPakPak",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/16854916?v=4",
                  "isRegistered": false,
                  "id": null
                },
                {
                  "githubUserId": 101401469,
                  "login": "AnthonyByansi",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/101401469?v=4",
                  "isRegistered": false,
                  "id": null
                }
              ]
            }
            """;

    @Test
    void should_fetch_project_contributors_and_suggest_external_contributors_from_github() {
        final String jwt = userAuthHelper.authenticateAntho().jwt();
        client.get()
                .uri(getApiURI(USERS_SEARCH_CONTRIBUTORS, Map.of("projectId", projectId.toString(), "login", login)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody().json(PROJECTS_SEARCH_CONTRIBUTORS_RESPONSE);
    }

    @Test
    void should_fetch_repos_contributors_and_suggest_external_contributors_from_github() {
        final String jwt = userAuthHelper.authenticateAntho().jwt();
        client.get()
                .uri(getApiURI(USERS_SEARCH_CONTRIBUTORS, Map.of("repoIds", "493591124,498695724", "login", login)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .json(PROJECTS_SEARCH_CONTRIBUTORS_RESPONSE);
    }

    @Test
    void should_fetch_repos_contributors_even_without_login_search() {
        final String jwt = userAuthHelper.authenticateAntho().jwt();
        client.get()
                .uri(getApiURI(USERS_SEARCH_CONTRIBUTORS, Map.of("repoIds", "498695724")))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.internalContributors.length()").isEqualTo(18)
                .jsonPath("$.externalContributors.length()").isEqualTo(0);
    }

    @Test
    void should_fetch_project_contributors_even_without_login_search() {
        final String jwt = userAuthHelper.authenticateAntho().jwt();
        client.get()
                .uri(getApiURI(USERS_SEARCH_CONTRIBUTORS, Map.of("projectId", projectId.toString())))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.internalContributors.length()").isEqualTo(22)
                .jsonPath("$.externalContributors.length()").isEqualTo(0);
    }

    @Test
    void should_fetch_external_contributors_when_externalSearchOnly_is_true() {
        final String jwt = userAuthHelper.authenticateAntho().jwt();
        client.get()
                .uri(getApiURI(USERS_SEARCH_CONTRIBUTORS, Map.of("projectId", projectId.toString(), "login", login,
                        "externalSearchOnly", "true")))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "internalContributors": [],
                          "externalContributors": [
                             {
                               "githubUserId": 31220,
                               "login": "antho",
                               "avatarUrl": "https://avatars.githubusercontent.com/u/31220?v=4",
                               "isRegistered": false,
                               "id": null
                             },
                             {
                               "githubUserId": 3982077,
                               "login": "anthonychu",
                               "avatarUrl": "https://avatars.githubusercontent.com/u/3982077?v=4",
                               "isRegistered": false,
                               "id": null
                             },
                             {
                               "githubUserId": 36125,
                               "login": "anthonyshort",
                               "avatarUrl": "https://avatars.githubusercontent.com/u/36125?v=4",
                               "isRegistered": false,
                               "id": null
                             },
                             {
                               "githubUserId": 16854916,
                               "login": "AnthoPakPak",
                               "avatarUrl": "https://avatars.githubusercontent.com/u/16854916?v=4",
                               "isRegistered": false,
                               "id": null
                             },
                             {
                               "githubUserId": 101401469,
                               "login": "AnthonyByansi",
                               "avatarUrl": "https://avatars.githubusercontent.com/u/101401469?v=4",
                               "isRegistered": false,
                               "id": null
                             }
                           ]
                         }
                        """);
    }

    @Test
    void should_return_all_when_no_param_is_provided() {
        final String jwt = userAuthHelper.authenticateAntho().jwt();
        client.get()
                .uri(getApiURI(USERS_SEARCH_CONTRIBUTORS, "maxInternalContributorCountToReturn", "5000"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.internalContributors.length()").isEqualTo(200)
                .jsonPath("$.externalContributors.length()").isEqualTo(0);
    }

    @Test
    void should_return_401_when_no_authentication_is_provided() {
        client.get()
                .uri(getApiURI(USERS_SEARCH_CONTRIBUTORS, "login", login))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void should_filter_by_registration_status() {
        final String jwt = userAuthHelper.authenticateAntho().jwt();

        {
            final var response = client.get()
                    .uri(getApiURI(USERS_SEARCH_CONTRIBUTORS, Map.of("isRegistered", "true",
                            "login", login)))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .exchange()
                    .expectStatus().is2xxSuccessful()
                    .expectBody(ContributorSearchResponse.class).returnResult().getResponseBody();

            assertThat(response).isNotNull();
            assertThat(response.getInternalContributors()).isNotEmpty().extracting(ContributorResponse::getIsRegistered).containsOnly(true);
            assertThat(response.getExternalContributors()).isEmpty();
        }

        {
            final var response = client.get()
                    .uri(getApiURI(USERS_SEARCH_CONTRIBUTORS, Map.of("isRegistered", "false",
                            "login", login,
                            "maxInternalContributorCountToTriggerExternalSearch", "20")))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .exchange()
                    .expectStatus().is2xxSuccessful()
                    .expectBody(ContributorSearchResponse.class).returnResult().getResponseBody();

            assertThat(response).isNotNull();
            assertThat(response.getInternalContributors()).isNotEmpty().extracting(ContributorResponse::getIsRegistered).containsOnly(false);
            assertThat(response.getExternalContributors()).isNotEmpty().extracting(ContributorResponse::getIsRegistered).containsOnly(false);
        }
    }
}
