package onlydust.com.marketplace.api.auth0.api.client.adapter;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.maciejwalkowiak.wiremock.spring.ConfigureWireMock;
import com.maciejwalkowiak.wiremock.spring.EnableWireMock;
import com.maciejwalkowiak.wiremock.spring.InjectWireMock;
import lombok.SneakyThrows;
import onlydust.com.marketplace.api.auth0.api.client.adapter.authentication.Auth0ApiAuthenticator;
import org.junit.jupiter.api.Test;

@EnableWireMock({
    @ConfigureWireMock(name = "auth0", property = "infrastructure.auth0.api.client.domain-base-uri")
})
class Auth0ApiClientAdapterTest {

  @InjectWireMock("auth0")
  protected WireMockServer auth0ApiWireMockServer;

  @SneakyThrows
  @Test
  void getGithubPersonalToken() {
    // Given
    final var properties = Auth0ApiClientProperties.builder()
        .clientId("some-client-id")
        .clientSecret("some-client-secret")
        .domainBaseUri(auth0ApiWireMockServer.baseUrl())
        .patCacheTtlInSeconds(1)
        .build();
    final var auth0ApiClientAdapter = new Auth0ApiClientAdapter(properties, new Auth0ApiHttpClient(properties,
        new Auth0ApiAuthenticator(properties)));

    auth0ApiWireMockServer.stubFor(WireMock.post(
            WireMock.urlEqualTo("/oauth/token"))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(equalToJson("""
            {
                "grant_type" : "client_credentials",
                "client_id" : "some-client-id",
                "client_secret" : "some-client-secret",
                "audience" : "%s/api/v2/"
            }
            """.formatted(auth0ApiWireMockServer.baseUrl())))
        .willReturn(responseDefinition().withStatus(200).withBody("""
            {"access_token":"my-management-token","scope":"read:users read:user_idp_tokens","expires_in":100,"token_type":"Bearer"}
            """)));

    auth0ApiWireMockServer.stubFor(WireMock.get(
            WireMock.urlEqualTo("/api/v2/users/github%7C595505"))
        .withHeader("Content-Type", equalTo("application/json"))
        .withHeader("Authorization", equalTo("Bearer my-management-token"))
        .willReturn(responseDefinition().withStatus(200).withBody("""
            {
              "bio": "Web3, Cloud, Unity3D",
              "blog": "",
              "collaborators": 2,
              "company": "@onlydustxyz",
              "created_at": "2023-12-20T10:12:10.826Z",
              "disk_usage": 779248,
              "email": "foobar@gmail.com",
              "email_verified": true,
              "emails": [
                {
                  "email": "foobar@gmail.com",
                  "primary": true,
                  "verified": true,
                  "visibility": "private"
                }
              ],
              "events_url": "https://api.github.com/users/ofux/events{/privacy}",
              "followers": 19,
              "followers_url": "https://api.github.com/users/ofux/followers",
              "following": 5,
              "following_url": "https://api.github.com/users/ofux/following{/other_user}",
              "gists_url": "https://api.github.com/users/ofux/gists{/gist_id}",
              "gravatar_id": "",
              "html_url": "https://github.com/ofux",
              "identities": [
                {
                  "access_token": "ofux-github-pat",
                  "provider": "github",
                  "user_id": 595505,
                  "connection": "github",
                  "isSocial": true
                }
              ],
              "location": "France",
              "name": "Olivier F",
              "nickname": "ofux",
              "node_id": "MDQ6VXNlcjU5NTUwNQ==",
              "organizations_url": "https://api.github.com/users/ofux/orgs",
              "owned_private_repos": 33,
              "picture": "https://avatars.githubusercontent.com/u/595505?v=4",
              "private_gists": 2,
              "public_gists": 1,
              "public_repos": 44,
              "received_events_url": "https://api.github.com/users/ofux/received_events",
              "repos_url": "https://api.github.com/users/ofux/repos",
              "site_admin": false,
              "starred_url": "https://api.github.com/users/ofux/starred{/owner}{/repo}",
              "subscriptions_url": "https://api.github.com/users/ofux/subscriptions",
              "total_private_repos": 33,
              "twitter_username": "fuxeto",
              "two_factor_authentication": true,
              "type": "User",
              "updated_at": "2023-12-20T10:12:10.826Z",
              "url": "https://api.github.com/users/ofux",
              "user_id": "github|595505",
              "last_ip": "2001:861:24a0:f1d0:1024:dda6:deb0:a289",
              "last_login": "2023-12-20T10:12:10.822Z",
              "logins_count": 1
            }
            """)));

    // When
    var pat = auth0ApiClientAdapter.getGithubPersonalToken(595505L);
    // Then
    assertThat(pat).isEqualTo("ofux-github-pat");
    auth0ApiWireMockServer.verify(1, getRequestedFor(urlEqualTo("/api/v2/users/github%7C595505")));

    // When we call it again, the PAT should be cached
    auth0ApiWireMockServer.resetRequests();
    pat = auth0ApiClientAdapter.getGithubPersonalToken(595505L);
    // Then
    assertThat(pat).isEqualTo("ofux-github-pat");
    auth0ApiWireMockServer.verify(0, getRequestedFor(urlEqualTo("/api/v2/users/github%7C595505")));

    // When we call it after a long time, a new PAT should be fetched
    Thread.sleep(1_100);
    auth0ApiWireMockServer.resetRequests();
    pat = auth0ApiClientAdapter.getGithubPersonalToken(595505L);
    // Then
    assertThat(pat).isEqualTo("ofux-github-pat");
    auth0ApiWireMockServer.verify(1, getRequestedFor(urlEqualTo("/api/v2/users/github%7C595505")));
  }
}