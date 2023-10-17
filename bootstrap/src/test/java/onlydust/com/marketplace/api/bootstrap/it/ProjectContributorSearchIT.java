package onlydust.com.marketplace.api.bootstrap.it;

import com.auth0.jwt.interfaces.JWTVerifier;
import onlydust.com.marketplace.api.bootstrap.helper.JwtVerifierStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

public class ProjectContributorSearchIT extends AbstractMarketplaceApiIT {
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
        searchContributors(projectId, login)
                .expectStatus().is2xxSuccessful()
                .expectBody().json(PROJECTS_SEARCH_CONTRIBUTORS_RESPONSE);
    }

    WebTestClient.ResponseSpec searchContributors(final UUID projectId, String login) {
        return client.get()
                .uri(getApiURI(String.format(PROJECTS_SEARCH_CONTRIBUTORS, projectId), "login", login))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                .exchange();
    }
}
