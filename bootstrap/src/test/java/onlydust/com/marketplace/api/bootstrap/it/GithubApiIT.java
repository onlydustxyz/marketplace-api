package onlydust.com.marketplace.api.bootstrap.it;

import com.github.tomakehurst.wiremock.client.WireMock;
import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import onlydust.com.marketplace.api.postgres.adapter.repository.GithubAppInstallationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;

@ActiveProfiles("hasura_auth")
public class GithubApiIT extends AbstractMarketplaceApiIT {
    private static final String ONLYDUST_ACCOUNT_JSON = """
            {
              "id": 123456,
              "organization": {
                "id": 98735558,
                "login": "onlydustxyz",
                "avatarUrl": "https://avatars.githubusercontent.com/u/98735558?v=4",
                "htmlUrl": "https://github.com/onlydustxyz",
                "name": "OnlyDust",
                "repos": [
                  {
                    "id": 480776993,
                    "owner": "onlydustxyz",
                    "name": "starklings",
                    "description": "An interactive tutorial to get you up and running with Starknet",
                    "htmlUrl": "https://github.com/onlydustxyz/starklings"
                  },
                  {
                    "id": 481932781,
                    "owner": "onlydustxyz",
                    "name": "starkonquest",
                    "description": "An educational game to learn Cairo where you implement ship AIs that fight to catch as much dust as possible!",
                    "htmlUrl": "https://github.com/onlydustxyz/starkonquest"
                  },
                  {
                    "id": 493591124,
                    "owner": "onlydustxyz",
                    "name": "kaaper",
                    "description": "Documentation generator for Cairo projects.",
                    "htmlUrl": "https://github.com/onlydustxyz/kaaper"
                  },
                  {
                    "id": 498695724,
                    "owner": "onlydustxyz",
                    "name": "marketplace-frontend",
                    "description": "Contributions marketplace backend services",
                    "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                  },
                  {
                    "id": 593218280,
                    "owner": "onlydustxyz",
                    "name": "octocrab",
                    "description": "A modern, extensible GitHub API Client for Rust.",
                    "htmlUrl": "https://github.com/onlydustxyz/octocrab"
                  },
                  {
                    "id": 593701982,
                    "owner": "onlydustxyz",
                    "name": "gateway",
                    "description": null,
                    "htmlUrl": "https://github.com/onlydustxyz/gateway"
                  }
                ]
              }
            }
            """;

    @Autowired
    GithubAppInstallationRepository githubAppInstallationRepository;
    @Autowired
    HasuraUserHelper hasuraUserHelper;

    @Test
    void should_get_github_account_from_installation_id() {
        final String jwt = hasuraUserHelper.authenticatePierre().jwt();
        client.get()
                .uri(getApiURI(GITHUB_INSTALLATIONS_GET + "/123456"))
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(ONLYDUST_ACCOUNT_JSON);
    }

    @Test
    void should_return_404_when_not_found() {
        final String jwt = hasuraUserHelper.authenticatePierre().jwt();
        client.get()
                .uri(getApiURI(GITHUB_INSTALLATIONS_GET + "/0"))
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                // Then
                .expectStatus()
                .isNotFound()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Installation 0 not found");
    }

    @Test
    void should_return_user_organizations() {
        // Given
        final String githubPAT = faker.rickAndMorty().character() + faker.random().nextLong();
        final HasuraUserHelper.AuthenticatedUser pierre = hasuraUserHelper.authenticatePierre(githubPAT);
        final String jwt = pierre.jwt();
        githubWireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/user/orgs?per_page=100&page=1"))
                .withHeader("Authorization", WireMock.equalTo("Bearer " + githubPAT))
                .willReturn(okJson("""
                                [
                                    {
                                        "login": "Barbicane-fr",
                                        "id": 58205251,
                                        "node_id": "MDEyOk9yZ2FuaXphdGlvbjU4MjA1MjUx",
                                        "url": "https://api.github.com/orgs/Barbicane-fr",
                                        "repos_url": "https://api.github.com/orgs/Barbicane-fr/repos",
                                        "events_url": "https://api.github.com/orgs/Barbicane-fr/events",
                                        "hooks_url": "https://api.github.com/orgs/Barbicane-fr/hooks",
                                        "issues_url": "https://api.github.com/orgs/Barbicane-fr/issues",
                                        "members_url": "https://api.github.com/orgs/Barbicane-fr/members{/member}",
                                        "public_members_url": "https://api.github.com/orgs/Barbicane-fr/public_members{/member}",
                                        "avatar_url": "https://avatars.githubusercontent.com/u/58205251?v=4",
                                        "description": ""
                                    },
                                    {
                                        "login": "onlydustxyz",
                                        "id": 98735558,
                                        "node_id": "O_kgDOBeKVxg",
                                        "url": "https://api.github.com/orgs/onlydustxyz",
                                        "repos_url": "https://api.github.com/orgs/onlydustxyz/repos",
                                        "events_url": "https://api.github.com/orgs/onlydustxyz/events",
                                        "hooks_url": "https://api.github.com/orgs/onlydustxyz/hooks",
                                        "issues_url": "https://api.github.com/orgs/onlydustxyz/issues",
                                        "members_url": "https://api.github.com/orgs/onlydustxyz/members{/member}",
                                        "public_members_url": "https://api.github.com/orgs/onlydustxyz/public_members{/member}",
                                        "avatar_url": "https://avatars.githubusercontent.com/u/98735558?v=4",
                                        "description": ""
                                    },
                                    {
                                        "login": "symeo-io",
                                        "id": 105865802,
                                        "node_id": "O_kgDOBk9iSg",
                                        "url": "https://api.github.com/orgs/symeo-io",
                                        "repos_url": "https://api.github.com/orgs/symeo-io/repos",
                                        "events_url": "https://api.github.com/orgs/symeo-io/events",
                                        "hooks_url": "https://api.github.com/orgs/symeo-io/hooks",
                                        "issues_url": "https://api.github.com/orgs/symeo-io/issues",
                                        "members_url": "https://api.github.com/orgs/symeo-io/members{/member}",
                                        "public_members_url": "https://api.github.com/orgs/symeo-io/public_members{/member}",
                                        "avatar_url": "https://avatars.githubusercontent.com/u/105865802?v=4",
                                        "description": ""
                                    }
                                ]
                        """)));

        // When
        client.get()
                .uri(getApiURI(GITHUB_USERS_ORGANIZATIONS_GET))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        [
                           {
                             "id": 58205251,
                             "login": "Barbicane-fr",
                             "avatarUrl": "https://avatars.githubusercontent.com/u/58205251?v=4",
                             "htmlUrl": "https://github.com/Barbicane-fr",
                             "name": "Barbicane",
                             "repos": [
                               {
                                 "id": 347315291,
                                 "owner": "Barbicane-fr",
                                 "name": "maston",
                                 "description": "Open source java project providing simple kafka streams tools using Vavr",
                                 "htmlUrl": "https://github.com/Barbicane-fr/maston"
                               }
                             ],
                             "installed": true,
                             "installationId": 43914822
                           },
                           {
                             "id": 98735558,
                             "login": "onlydustxyz",
                             "avatarUrl": "https://avatars.githubusercontent.com/u/98735558?v=4",
                             "htmlUrl": "https://github.com/onlydustxyz",
                             "name": "OnlyDust",
                             "repos": [
                               {
                                 "id": 480776993,
                                 "owner": "onlydustxyz",
                                 "name": "starklings",
                                 "description": "An interactive tutorial to get you up and running with Starknet",
                                 "htmlUrl": "https://github.com/onlydustxyz/starklings"
                               },
                               {
                                 "id": 481932781,
                                 "owner": "onlydustxyz",
                                 "name": "starkonquest",
                                 "description": "An educational game to learn Cairo where you implement ship AIs that fight to catch as much dust as possible!",
                                 "htmlUrl": "https://github.com/onlydustxyz/starkonquest"
                               },
                               {
                                 "id": 493591124,
                                 "owner": "onlydustxyz",
                                 "name": "kaaper",
                                 "description": "Documentation generator for Cairo projects.",
                                 "htmlUrl": "https://github.com/onlydustxyz/kaaper"
                               },
                               {
                                 "id": 498695724,
                                 "owner": "onlydustxyz",
                                 "name": "marketplace-frontend",
                                 "description": "Contributions marketplace backend services",
                                 "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                               },
                               {
                                 "id": 593218280,
                                 "owner": "onlydustxyz",
                                 "name": "octocrab",
                                 "description": "A modern, extensible GitHub API Client for Rust.",
                                 "htmlUrl": "https://github.com/onlydustxyz/octocrab"
                               },
                               {
                                 "id": 593701982,
                                 "owner": "onlydustxyz",
                                 "name": "gateway",
                                 "description": null,
                                 "htmlUrl": "https://github.com/onlydustxyz/gateway"
                               }
                             ],
                             "installed": true,
                             "installationId": 123456
                           },
                           {
                             "id": 105865802,
                             "login": "symeo-io",
                             "avatarUrl": "https://avatars.githubusercontent.com/u/105865802?v=4",
                             "htmlUrl": "https://github.com/symeo-io",
                             "name": "Symeo.io",
                             "repos": [
                               {
                                 "id": 495382833,
                                 "owner": "symeo-io",
                                 "name": "symeo-monolithic-backend",
                                 "description": null,
                                 "htmlUrl": "https://github.com/symeo-io/symeo-monolithic-backend"
                               },
                               {
                                 "id": 593536214,
                                 "owner": "symeo-io",
                                 "name": "symeo-js",
                                 "description": null,
                                 "htmlUrl": "https://github.com/symeo-io/symeo-js"
                               },
                               {
                                 "id": 595202901,
                                 "owner": "symeo-io",
                                 "name": "symeo-python",
                                 "description": "The Symeo SDK made for interacting with your Symeo secrets and configuration from python applications",
                                 "htmlUrl": "https://github.com/symeo-io/symeo-python"
                               },
                               {
                                 "id": 595278857,
                                 "owner": "symeo-io",
                                 "name": "symeo-webapp",
                                 "description": null,
                                 "htmlUrl": "https://github.com/symeo-io/symeo-webapp"
                               },
                               {
                                 "id": 624779785,
                                 "owner": "symeo-io",
                                 "name": "symeo-cli",
                                 "description": null,
                                 "htmlUrl": "https://github.com/symeo-io/symeo-cli"
                               },
                               {
                                 "id": 636240099,
                                 "owner": "symeo-io",
                                 "name": "symeo-js-template",
                                 "description": null,
                                 "htmlUrl": "https://github.com/symeo-io/symeo-js-template"
                               },
                               {
                                 "id": 641970419,
                                 "owner": "symeo-io",
                                 "name": "symeo-python-template",
                                 "description": null,
                                 "htmlUrl": "https://github.com/symeo-io/symeo-python-template"
                               },
                               {
                                 "id": 649551461,
                                 "owner": "symeo-io",
                                 "name": "symeo-reactjs-template",
                                 "description": null,
                                 "htmlUrl": "https://github.com/symeo-io/symeo-reactjs-template"
                               },
                               {
                                 "id": 656103826,
                                 "owner": "symeo-io",
                                 "name": "symeo-cloud-function-example",
                                 "description": null,
                                 "htmlUrl": "https://github.com/symeo-io/symeo-cloud-function-example"
                               },
                               {
                                 "id": 656112347,
                                 "owner": "symeo-io",
                                 "name": "symeo-lambda-example",
                                 "description": null,
                                 "htmlUrl": "https://github.com/symeo-io/symeo-lambda-example"
                               }
                             ],
                             "installed": true,
                             "installationId": 43872836
                           },
                           {
                             "id": 16590657,
                             "login": "PierreOucif",
                             "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                             "htmlUrl": null,
                             "name": null,
                             "repos": [],
                             "installed": false,
                             "installationId": null
                           }
                         ]
                        """);
    }
}
