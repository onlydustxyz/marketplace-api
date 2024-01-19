package onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class Auth0UserInfoServiceTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private Auth0UserInfoService auth0UserInfoService;
    private HttpClient httpClient;
    private JWTVerifier jwtVerifier;

    private final Auth0Properties auth0Properties = Auth0Properties.builder()
            .userInfoUrl("https://onlydust.eu.auth0.com/userinfo")
            .build();

    @BeforeEach
    void setUp() {
        jwtVerifier = mock(JWTVerifier.class);
        httpClient = mock(HttpClient.class);
        auth0UserInfoService = new Auth0UserInfoService(objectMapper, httpClient, auth0Properties, jwtVerifier);
    }

    @SneakyThrows
    @Test
    void getUserInfo() {
        // Given
        final var accessToken = "accessToken";

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(auth0Properties.userInfoUrl))
                .header("Authorization", "Bearer " + accessToken)
                .build();

        final DecodedJWT decodedJWT = mock(DecodedJWT.class);
        when(decodedJWT.getToken()).thenReturn(accessToken);
        when(decodedJWT.getPayload()).thenReturn(Base64.getUrlEncoder().encodeToString("""
                {
                  "exp": %d
                }
                """.formatted(System.currentTimeMillis() / 1000 + 2).getBytes(StandardCharsets.UTF_8)));

        final var response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("""
                {
                    "sub": "github|595505",
                    "nickname": "ofux",
                    "name": "Olivier",
                    "picture": "https://avatars.githubusercontent.com/u/595505?v=4",
                    "updated_at": "2023-12-11T12:33:51Z",
                    "email": "olivier@foo.org",
                    "email_verified": true
                }
                """);
        when(httpClient.send(request, HttpResponse.BodyHandlers.ofString())).thenReturn(response);
        when(jwtVerifier.verify(accessToken)).thenReturn(decodedJWT);

        // When
        var claims = auth0UserInfoService.getUserInfo(accessToken);

        // Then
        assertThat(claims.getGithubWithUserId()).isEqualTo("github|595505");
        assertThat(claims.getGithubLogin()).isEqualTo("ofux");
        assertThat(claims.getGithubAvatarUrl()).isEqualTo("https://avatars.githubusercontent.com/u/595505?v=4");
        assertThat(claims.getEmail()).isEqualTo("olivier@foo.org");
        verify(httpClient, times(1)).send(any(), any());


        // And when we call it again, the user info should be cached
        Mockito.reset(httpClient);
        claims = auth0UserInfoService.getUserInfo(accessToken);
        assertThat(claims.getGithubWithUserId()).isEqualTo("github|595505");
        assertThat(claims.getGithubLogin()).isEqualTo("ofux");
        assertThat(claims.getGithubAvatarUrl()).isEqualTo("https://avatars.githubusercontent.com/u/595505?v=4");
        assertThat(claims.getEmail()).isEqualTo("olivier@foo.org");
        verify(httpClient, never()).send(any(), any());


        // And when we call it again after some time, the user info should not be cached anymore
        Mockito.reset(httpClient);
        when(httpClient.send(request, HttpResponse.BodyHandlers.ofString())).thenReturn(response);
        Thread.sleep(2_100);
        claims = auth0UserInfoService.getUserInfo(accessToken);
        assertThat(claims.getGithubWithUserId()).isEqualTo("github|595505");
        assertThat(claims.getGithubLogin()).isEqualTo("ofux");
        assertThat(claims.getGithubAvatarUrl()).isEqualTo("https://avatars.githubusercontent.com/u/595505?v=4");
        assertThat(claims.getEmail()).isEqualTo("olivier@foo.org");
        verify(httpClient, times(1)).send(any(), any());
    }
}