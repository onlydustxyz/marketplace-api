package onlydust.com.marketplace.api.auth0.api.client.adapter.authentication;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.maciejwalkowiak.wiremock.spring.ConfigureWireMock;
import com.maciejwalkowiak.wiremock.spring.EnableWireMock;
import com.maciejwalkowiak.wiremock.spring.InjectWireMock;
import lombok.SneakyThrows;
import onlydust.com.marketplace.api.auth0.api.client.adapter.Auth0ApiClientProperties;
import org.junit.jupiter.api.Test;

@EnableWireMock({
    @ConfigureWireMock(name = "auth0", property = "infrastructure.auth0.api.client.domain-base-uri")
})
class Auth0ApiAuthenticatorTest {

  @InjectWireMock("auth0")
  protected WireMockServer auth0ApiWireMockServer;

  @SneakyThrows
  @Test
  void getAuth0ManagementApiAccessToken() {
    // Given
    final var properties = Auth0ApiClientProperties.builder()
        .clientId("some-client-id")
        .clientSecret("some-client-secret")
        .domainBaseUri(auth0ApiWireMockServer.baseUrl())
        .build();
    final var authenticator = new Auth0ApiAuthenticator(properties);

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
        // Returns a token that will live one second in the cache
        .willReturn(responseDefinition().withStatus(200).withBody("""
            {"access_token":"my-management-token","scope":"read:users read:user_idp_tokens","expires_in":%d,"token_type":"Bearer"}
            """.formatted(Auth0ApiAuthenticator.CACHE_TTL_LEEWAY_IN_SECONDS + 1))));

    // When
    var token = authenticator.getAuth0ManagementApiAccessToken();
    // Then
    assertThat(token).isEqualTo("my-management-token");
    auth0ApiWireMockServer.verify(1, postRequestedFor(urlEqualTo("/oauth/token")));

    // When we call it again, the auth0 token should be cached
    auth0ApiWireMockServer.resetRequests();
    token = authenticator.getAuth0ManagementApiAccessToken();
    // Then
    assertThat(token).isEqualTo("my-management-token");
    auth0ApiWireMockServer.verify(0, postRequestedFor(urlEqualTo("/oauth/token")));

    // When we call it again after the token expired, a new auth0 token should be fetched
    auth0ApiWireMockServer.resetRequests();
    Thread.sleep(1_100);
    token = authenticator.getAuth0ManagementApiAccessToken();
    // Then
    assertThat(token).isEqualTo("my-management-token");
    auth0ApiWireMockServer.verify(1, postRequestedFor(urlEqualTo("/oauth/token")));
  }
}