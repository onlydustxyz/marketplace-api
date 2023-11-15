package onlydust.com.marketplace.api.bootstrap.it;

import com.auth0.jwt.interfaces.JWTVerifier;
import onlydust.com.marketplace.api.bootstrap.helper.JwtVerifierStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import java.util.Map;
import java.util.UUID;

public class ContributorSearchIT extends AbstractMarketplaceApiIT {
    final static String JWT_TOKEN = "fake-jwt";
    final static String login = "antho";
    final static UUID projectId = UUID.fromString("298a547f-ecb6-4ab2-8975-68f4e9bf7b39"); // kaaper
    final static String PROJECTS_SEARCH_CONTRIBUTORS_RESPONSE = """
            {
              "internalContributors": [
                {
                  "githubUserId": 43467246,
                  "login": "AnthonyBuisset",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                  "isRegistered": true
                }
              ],
              "externalContributors": [
                {
                  "githubUserId": 31220,
                  "login": "antho",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/31220?v=4",
                  "isRegistered": false
                },
                {
                  "githubUserId": 3982077,
                  "login": "anthonychu",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/3982077?v=4",
                  "isRegistered": false
                },
                {
                  "githubUserId": 36125,
                  "login": "anthonyshort",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/36125?v=4",
                  "isRegistered": false
                },
                {
                  "githubUserId": 16854916,
                  "login": "AnthoPakPak",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/16854916?v=4",
                  "isRegistered": false
                },
                {
                  "githubUserId": 101401469,
                  "login": "AnthonyByansi",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/101401469?v=4",
                  "isRegistered": false
                }
              ]
            }
            """;

    final Long githubUserId = faker.number().randomNumber();
    final String avatarUrl = faker.internet().avatar();

    @Autowired
    JWTVerifier jwtVerifier;

    @BeforeEach
    void setup() {
        ((JwtVerifierStub) jwtVerifier).withJwtMock(JWT_TOKEN, githubUserId, login, avatarUrl);
    }

    @Test
    void should_fetch_project_contributors_and_suggest_external_contributors_from_github() {
        client.get()
                .uri(getApiURI(USERS_SEARCH_CONTRIBUTORS, Map.of("projectId", projectId.toString(), "login", login)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody().json(PROJECTS_SEARCH_CONTRIBUTORS_RESPONSE);
    }

    @Test
    void should_fetch_repos_contributors_and_suggest_external_contributors_from_github() {
        client.get()
                .uri(getApiURI(USERS_SEARCH_CONTRIBUTORS, Map.of("repoIds", "493591124,498695724", "login", login)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody().consumeWith(System.out::println)
                .json(PROJECTS_SEARCH_CONTRIBUTORS_RESPONSE);
    }

    @Test
    void should_fetch_repos_contributors_even_without_login_search() {
        client.get()
                .uri(getApiURI(USERS_SEARCH_CONTRIBUTORS, Map.of("repoIds", "498695724")))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.internalContributors.length()").isEqualTo(17)
                .jsonPath("$.externalContributors.length()").isEqualTo(0);
    }

    @Test
    void should_fetch_project_contributors_even_without_login_search() {
        client.get()
                .uri(getApiURI(USERS_SEARCH_CONTRIBUTORS, Map.of("projectId", projectId.toString())))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.internalContributors.length()").isEqualTo(21)
                .jsonPath("$.externalContributors.length()").isEqualTo(0);
    }

    @Test
    void should_fetch_external_contributors_even_without_project_nor_repo() {
        client.get()
                .uri(getApiURI(USERS_SEARCH_CONTRIBUTORS, "login", login))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.internalContributors.length()").isEqualTo(0)
                .jsonPath("$.externalContributors.length()").isEqualTo(5);
    }

    @Test
    void should_fetch_external_contributors_when_externalSearchOnly_is_true() {
        client.get()
                .uri(getApiURI(USERS_SEARCH_CONTRIBUTORS, Map.of("projectId", projectId.toString(), "login", login,
                        "externalSearchOnly", "true")))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "contributors": [
                            {
                              "githubUserId": 31220,
                              "login": "antho",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/31220?v=4",
                              "isRegistered": false
                            },
                            {
                              "githubUserId": 3982077,
                              "login": "anthonychu",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/3982077?v=4",
                              "isRegistered": false
                            },
                            {
                              "githubUserId": 36125,
                              "login": "anthonyshort",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/36125?v=4",
                              "isRegistered": false
                            },
                            {
                              "githubUserId": 16854916,
                              "login": "AnthoPakPak",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/16854916?v=4",
                              "isRegistered": false
                            },
                            {
                              "githubUserId": 101401469,
                              "login": "AnthonyByansi",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/101401469?v=4",
                              "isRegistered": false
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_return_400_when_no_param_is_provided() {
        client.get()
                .uri(getApiURI(USERS_SEARCH_CONTRIBUTORS))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void should_return_401_when_no_authentication_is_provided() {
        client.get()
                .uri(getApiURI(USERS_SEARCH_CONTRIBUTORS, "login", login))
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
