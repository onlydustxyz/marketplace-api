package onlydust.com.marketplace.api.it.bo;

import com.github.tomakehurst.wiremock.client.WireMock;
import onlydust.com.backoffice.api.contract.model.ReplaceAndResetUserRequest;
import onlydust.com.marketplace.accounting.domain.model.Payment;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.api.helper.Auth0ApiClientStub;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.AccountBookEventEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.AccountBookEventRepository;
import onlydust.com.marketplace.api.suites.tags.TagBO;
import onlydust.com.marketplace.user.domain.model.BackofficeUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

@TagBO
public class BackofficeDebugApiIT extends AbstractMarketplaceBackOfficeApiIT {
    private static final String CHECK_ACCOUNTING_EVENTS = "/bo/v1/debug/accounting/check-events";
    private static final String DELETE_USERS = "/bo/v1/debug/reset-and-replace-user";

    @Autowired
    private AccountBookEventRepository accountBookEventRepository;

    UserAuthHelper.AuthenticatedBackofficeUser olivier;

    @BeforeEach
    void setUp() {
        olivier = userAuthHelper.authenticateBackofficeUser("olivier.fuxet@gmail.com", List.of(BackofficeUser.Role.BO_READER));
    }

    @Test
    public void should_check_accounting_events() {
        client
                .get()
                .uri(getApiURI(CHECK_ACCOUNTING_EVENTS))
                .header("Authorization", "Bearer " + olivier.jwt())
                .exchange()
                .expectStatus()
                .isNoContent();

        final var event = accountBookEventRepository.findAllByAccountBookIdOrderByIdAsc(UUID.fromString("c8f1d94a-e9d4-40d6-9b93-6818b9a7730c")).stream()
                .filter(e -> e.id().equals(168L)).findFirst().orElseThrow();

        final var invalidEvent = new AccountBookEventEntity(event.id(), event.accountBookId(), event.timestamp(),
                new AccountBookEventEntity.Payload(new AccountBookAggregate.BurnEvent(
                        AccountBook.AccountId.of(Payment.Id.of("b558e96f-10ac-483a-aca6-6e3de0895e77")), PositiveAmount.of(1_000_000_000L))));

        accountBookEventRepository.saveAndFlush(invalidEvent);

        client
                .get()
                .uri(getApiURI(CHECK_ACCOUNTING_EVENTS))
                .header("Authorization", "Bearer " + olivier.jwt())
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody()
                .json("""
                        {"message":"Cannot burn 1000000000 from b558e96f-10ac-483a-aca6-6e3de0895e77"}
                        """);
    }

    @Autowired
    Auth0ApiClientStub auth0ApiClientStub;

    @Test
    void should_reset_user() {
        // Given
        final ReplaceAndResetUserRequest replaceAndResetUserRequest = new ReplaceAndResetUserRequest();
        // Let's go to delete GregGamb account !!!
        replaceAndResetUserRequest.setUserId(UUID.fromString("743e096e-c922-4097-9e6f-8ea503055336"));
        replaceAndResetUserRequest.setGithubOAuthAppId(faker.number().digit());
        replaceAndResetUserRequest.setGithubOAuthAppSecret(faker.rickAndMorty().location());
        replaceAndResetUserRequest.setNewGithubLogin("aterrien");

        githubWireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/search/users?per_page=5&q=%s".formatted(replaceAndResetUserRequest.getNewGithubLogin())))
                .withHeader("Authorization", WireMock.equalTo("Bearer GITHUB_PAT"))
                .willReturn(okJson("""
                        {
                          "total_count": 1,
                          "incomplete_results": false,
                          "items": [
                            {
                              "login": "aterrien",
                              "id": 1234,
                              "node_id": "MDQ6VXNlcjMxMjIw",
                              "avatar_url": "https://avatars.githubusercontent.com/u/31220?v=4",
                              "gravatar_id": "",
                              "url": "https://api.github.com/users/aterrien",
                              "html_url": "https://github.com/aterrien",
                              "followers_url": "https://api.github.com/users/aterrien/followers",
                              "following_url": "https://api.github.com/users/aterrien/following{/other_user}",
                              "gists_url": "https://api.github.com/users/aterrien/gists{/gist_id}",
                              "starred_url": "https://api.github.com/users/aterrien/starred{/owner}{/repo}",
                              "subscriptions_url": "https://api.github.com/users/aterrien/subscriptions",
                              "organizations_url": "https://api.github.com/users/aterrien/orgs",
                              "repos_url": "https://api.github.com/users/aterrien/repos",
                              "events_url": "https://api.github.com/users/aterrien/events{/privacy}",
                              "received_events_url": "https://api.github.com/users/aterrien/received_events",
                              "type": "User",
                              "site_admin": false,
                              "score": 1.0
                            }
                          ]
                        }
                        """)));

        authM2M0WireMockServer.stubFor(WireMock.post(
                        WireMock.urlEqualTo("/oauth/token"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                            "grant_type" : "client_credentials",
                            "client_id" : "fake-m2m-client-id",
                            "client_secret" : "fake-m2m-client-secret",
                            "audience" : "%s/api/v2/"
                        }
                        """.formatted(authM2M0WireMockServer.baseUrl())))
                .willReturn(responseDefinition().withStatus(200).withBody("""
                        {"access_token":"fake-auth0-m2m-access-token","scope":"read:users read:user_idp_tokens","expires_in":100,"token_type":"Bearer"}
                        """)));


        authM2M0WireMockServer.stubFor(WireMock.get("/api/v2/users/github%7C122993337")
                .withHeader("Authorization", equalTo("Bearer %s".formatted("fake-auth0-m2m-access-token")))
                .willReturn(okJson("""
                        {
                            "blog": null,
                            "collaborators": 7,
                            "created_at": "2024-06-21T14:27:02.180Z",
                            "disk_usage": 77898,
                            "email": "gregoire@onlydust.xyz",
                            "email_verified": true,
                            "emails": [
                                {
                                    "email": "gregoire@onlydust.xyz",
                                    "primary": true,
                                    "verified": true,
                                    "visibility": "public"
                                }
                            ],
                            "events_url": "https://api.github.com/users/aterrien/events{/privacy}",
                            "followers": 7,
                            "followers_url": "https://api.github.com/users/aterrien/followers",
                            "following": 5,
                            "following_url": "https://api.github.com/users/aterrien/following{/other_user}",
                            "gists_url": "https://api.github.com/users/aterrien/gists{/gist_id}",
                            "gravatar_id": "",
                            "hireable": true,
                            "html_url": "https://github.com/aterrien",
                            "identities": [
                                {
                                    "access_token": "fake-github-personal-token",
                                    "provider": "github",
                                    "user_id": 1234,
                                    "connection": "github",
                                    "isSocial": true
                                }
                            ],
                            "location": "Paris",
                            "name": "Ilysse",
                            "nickname": "aterrien",
                            "node_id": "MDQ6VXNlcjE2NTkwNjU3",
                            "organizations_url": "https://api.github.com/users/aterrien/orgs",
                            "owned_private_repos": 10,
                            "picture": "https://avatars.githubusercontent.com/u/1234?v=4",
                            "plan": {
                                "name": "free",
                                "space": 976562499,
                                "collaborators": 0,
                                "private_repos": 10000
                            },
                            "private_gists": 3,
                            "public_gists": 0,
                            "public_repos": 16,
                            "received_events_url": "https://api.github.com/users/aterrien/received_events",
                            "repos_url": "https://api.github.com/users/aterrien/repos",
                            "site_admin": false,
                            "starred_url": "https://api.github.com/users/aterrien/starred{/owner}{/repo}",
                            "subscriptions_url": "https://api.github.com/users/aterrien/subscriptions",
                            "total_private_repos": 10,
                            "two_factor_authentication": true,
                            "type": "User",
                            "updated_at": "2024-07-01T13:12:33.336Z",
                            "url": "https://api.github.com/users/aterrien",
                            "user_id": "github|1234",
                            "last_ip": "109.190.119.179",
                            "last_login": "2024-07-01T13:12:33.335Z",
                            "logins_count": 6
                        }"""))
        );

        final String basicAuth = "Basic " + Base64.getEncoder().encodeToString("%s:%s".formatted(replaceAndResetUserRequest.getGithubOAuthAppId(),
                replaceAndResetUserRequest.getGithubOAuthAppSecret()).getBytes());

        githubWireMockServer.stubFor(delete("/applications/%s/grant".formatted(replaceAndResetUserRequest.getGithubOAuthAppId()))
                .withHeader("Authorization", equalTo(basicAuth))
                .withRequestBody(equalToJson("""
                        {"access_token": "fake-github-personal-token"}
                        """))
                .willReturn(ok())
        );

        authM2M0WireMockServer.stubFor(WireMock.delete("/api/v2/users/github%7C122993337")
                .withHeader("Authorization", equalTo("Bearer %s".formatted("fake-auth0-m2m-access-token")))
                .willReturn(ok()));

        // When
        client.method(HttpMethod.DELETE)
                .uri(getApiURI(DELETE_USERS))
                .header("Authorization", "Bearer " + olivier.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(replaceAndResetUserRequest))
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        indexerApiWireMockServer.verify(1, putRequestedFor(urlEqualTo("/api/v1/users/%d".formatted(1234L)))
                .withHeader("Content-Type", equalTo("application/json"))
        );
    }
}
