package onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0;

import com.fasterxml.jackson.databind.ObjectMapper;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.app.Auth0OnlyDustAppAuthenticationService;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.app.OnlyDustAppAuthentication;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.backoffice.Auth0OnlyDustBackofficeAuthenticationService;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.backoffice.OnlyDustBackofficeAuthentication;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.github.GithubUserIdentity;
import onlydust.com.marketplace.user.domain.model.BackofficeUser;
import onlydust.com.marketplace.user.domain.port.input.AppUserFacadePort;
import onlydust.com.marketplace.user.domain.port.input.BackofficeUserFacadePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class Auth0JwtServiceTest {

    private static final Long ONE_CENTURY = 3153600000L;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String VALID_AUTH0_GITHUB_JWT = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IjJHNFZvZlVLMURVQklDUF9CcUdVcyJ9" +
                                                         ".eyJpc3MiOiJodHRwczovL2RldmVsb3Atb25seWR1c3QuZXUuYXV0aDAuY29tLyIsInN1YiI6ImdpdGh1YnwxNDMwMTEzNjQiLCJhdWQiOlsiaHR0cHM6Ly9kZXZlbG9wLW9ubHlkdXN0LmV1LmF1dGgwLmNvbS9hcGkvdjIvIiwiaHR0cHM6Ly9kZXZlbG9wLW9ubHlkdXN0LmV1LmF1dGgwLmNvbS91c2VyaW5mbyJdLCJpYXQiOjE3MDU0MTc1NjYsImV4cCI6MTcwNTUwMzk2NiwiYXpwIjoiZ2ZPZGlGT2x0WVlVTVllQnpOcGVOQWpNSG1iOWZXb1YiLCJzY29wZSI6Im9wZW5pZCBwcm9maWxlIGVtYWlsIG9mZmxpbmVfYWNjZXNzIn0.Z2WuBxiQy9DBOb2olIyj9JgidYClYenOy4rHG-Uu0ZFRuzEbzk4003NaWq-9j-O9EYYRLlT2hlIvXWAU0r0f2IDdtW3IpDpOV2Zj9-1ZMk-SYCWRlNaO3gh0bEqyGznIWmztr7gPdCtHPliG6l1A--uZBXKjUwi5XnVRhoLU9yJO6znPoGNQ-b5wnSGE8cmUyVkM_mGOU1FlkGIweG6ZKEQ-EoGPwC57nIxUpzRvmCD-5VUycb7M5vE4ktkyVFA_Bnp9O8FXLK_EIedFMgGJd0QWRboRbEerwQJHL9eghXXUDonq6P12zXdL3d_Edqzrq73F0UkI9ZMIEkex-SDfpQ";

    private static final String VALID_AUTH0_GOOGLE_JWT = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IjJHNFZvZlVLMURVQklDUF9CcUdVcyJ9" +
                                                         ".eyJpc3MiOiJodHRwczovL2RldmVsb3Atb25seWR1c3QuZXUuYXV0aDAuY29tLyIsInN1YiI6Imdvb2dsZS1vYXV0aDJ8MTAzMjIzMDA5NzY5OTExNzQ3Njk0IiwiYXVkIjpbImh0dHBzOi8vZGV2ZWxvcC1iby1hcGkub25seWR1c3QuY29tL2FwaSIsImh0dHBzOi8vZGV2ZWxvcC1vbmx5ZHVzdC5ldS5hdXRoMC5jb20vdXNlcmluZm8iXSwiaWF0IjoxNzA5MTMzODYzLCJleHAiOjE3MDkxMzUwNjMsImF6cCI6Imo3VFFRbUVNYWZwT245dkg2MW5JbHVBTmtmdWhtOUt3Iiwic2NvcGUiOiJvcGVuaWQgcHJvZmlsZSBlbWFpbCBvZmZsaW5lX2FjY2VzcyJ9.w8gEFwyw1qJ4J2ldhEJyu2AXDwov-ICG2LgW-i5FQ_KAHYAg8F-65BLETzvseYPbHdxnrRlpJ3LldxTp5bFoSQP2Ngpsw4mqLLZFNQsUcIDAfzjl8GqiI1j74j08EmaszISSk0pUEjvFdYb-nmOI_sUw24T16xGDP0AY7Ca-YlDavQOiB9pUYGLSWMzlPQSkspromE9azEP2yU-T4lUejstCIzJuqHsVwh3adtYn-7lVH70m46Rd7EtjEOfldS5lASuhxA2Ruksp-dg8npgGIrtgn6Ap3kIjvVlNkJs3Lgkh2bEKt0o7be4TxY_b7uVBMAPxu4z7o6Q5zSFSYXB6ag";

    AppUserFacadePort appUserFacadePort;
    BackofficeUserFacadePort backofficeUserFacadePort;
    Auth0Properties auth0Properties;
    Auth0JwtService auth0JwtService;
    Auth0UserInfoService auth0UserInfoService;

    @BeforeEach
    void setUp() {
        appUserFacadePort = mock(AppUserFacadePort.class);
        backofficeUserFacadePort = mock(BackofficeUserFacadePort.class);
        auth0UserInfoService = mock(Auth0UserInfoService.class);

        auth0Properties = Auth0Properties.builder()
                .jwksUrl("https://develop-onlydust.eu.auth0.com/")
                .expiresAtLeeway(ONE_CENTURY)
                .build();

        final var jwtVerifier = new Auth0JwtVerifier(auth0Properties);
        final var auth0OnlyDustAppAuthenticationService = new Auth0OnlyDustAppAuthenticationService(objectMapper, appUserFacadePort);
        final var auth0OnlyDustBackofficeAuthenticationService = new Auth0OnlyDustBackofficeAuthenticationService(backofficeUserFacadePort);
        auth0JwtService = new Auth0JwtService(auth0UserInfoService, jwtVerifier, auth0OnlyDustAppAuthenticationService,
                auth0OnlyDustBackofficeAuthenticationService);
    }

    @Test
    void should_authenticate_from_a_valid_github_jwt() throws IOException, InterruptedException {
        // Given
        when(auth0UserInfoService.getUserInfo(VALID_AUTH0_GITHUB_JWT)).thenReturn(Auth0JwtClaims.builder()
                .nickname("pixelfact")
                .sub("github|143011364")
                .picture("https://avatars.githubusercontent.com/u/143011364?v=4")
                .email("pixelfact.company@gmail.com")
                .name("Mehdi")
                .build());

        when(appUserFacadePort
                .getUserByGithubIdentity(GithubUserIdentity.builder()
                        .githubUserId(143011364L)
                        .login("pixelfact")
                        .avatarUrl("https://avatars.githubusercontent.com/u/143011364?v=4")
                        .email("pixelfact.company@gmail.com")
                        .build(), false)
        ).thenReturn(AuthenticatedUser.builder()
                .id(UUID.randomUUID())
                .login("pixelfact")
                .avatarUrl("https://avatars.githubusercontent.com/u/143011364?v=4")
                .githubUserId(143011364L)
                .roles(List.of(AuthenticatedUser.Role.USER))
                .build());


        // When
        final var authentication = (OnlyDustAppAuthentication) auth0JwtService.getAuthenticationFromJwt(VALID_AUTH0_GITHUB_JWT, null).orElseThrow();

        // Then
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.isImpersonating()).isFalse();
        assertThat(authentication.getImpersonator()).isNull();
        assertThat(authentication.getName()).isEqualTo("143011364");

        final var user = authentication.getUser();
        assertThat(user.login()).isEqualTo("pixelfact");
        assertThat(user.githubUserId()).isEqualTo(143011364L);
        assertThat(user.roles()).containsExactlyInAnyOrder(AuthenticatedUser.Role.USER);
    }

    @Test
    void should_authenticate_from_a_valid_google_jwt() throws IOException, InterruptedException {
        // Given
        when(auth0UserInfoService.getUserInfo(VALID_AUTH0_GOOGLE_JWT)).thenReturn(Auth0JwtClaims.builder()
                .nickname("meh")
                .sub("google-oauth2|103223009769911747694")
                .picture("https://avatars.google.com/u/143011364?v=4")
                .email("pixelfact.company@gmail.com")
                .name("Mehdi")
                .build());

        when(backofficeUserFacadePort
                .getUserByIdentity(BackofficeUser.Identity.builder()
                        .email("pixelfact.company@gmail.com")
                        .name("Mehdi")
                        .avatarUrl("https://avatars.google.com/u/143011364?v=4")
                        .build())
        ).thenReturn(new BackofficeUser(BackofficeUser.Id.random(), "pixelfact.company@gmail.com", "Mehdi",
                Set.of(BackofficeUser.Role.BO_READER), "https://avatars.google.com/u/143011364?v=4"));


        // When
        final var authentication = (OnlyDustBackofficeAuthentication) auth0JwtService.getAuthenticationFromJwt(VALID_AUTH0_GOOGLE_JWT, null).orElseThrow();

        // Then
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getName()).isEqualTo("pixelfact.company@gmail.com");

        final BackofficeUser user = authentication.getUser();
        assertThat(user.name()).isEqualTo("Mehdi");
        assertThat(user.email()).isEqualTo("pixelfact.company@gmail.com");
        assertThat(user.avatarUrl()).isEqualTo("https://avatars.google.com/u/143011364?v=4");
        assertThat(user.roles()).containsExactlyInAnyOrder(BackofficeUser.Role.BO_READER);
    }

    @Test
    void should_not_authenticate_from_an_invalid_jwt() throws IOException, InterruptedException {
        final String invalidJwt = "yolocrouteraclette";

        final var authentication = auth0JwtService.getAuthenticationFromJwt(invalidJwt, null);

        assertThat(authentication).isEmpty();
        verify(appUserFacadePort, never()).getUserByGithubIdentity(any(), anyBoolean());
        verify(auth0UserInfoService, never()).getUserInfo(any());
    }

    @Test
    void should_authenticate_given_a_valid_github_jwt_and_impersonation_header() throws IOException, InterruptedException {
        // Given
        when(auth0UserInfoService.getUserInfo(VALID_AUTH0_GITHUB_JWT)).thenReturn(Auth0JwtClaims.builder()
                .nickname("pixelfact")
                .sub("github|143011364")
                .picture("https://avatars.githubusercontent.com/u/143011364?v=4")
                .email("pixelfact.company@gmail.com")
                .name("Mehdi")
                .build());

        when(appUserFacadePort
                .getUserByGithubIdentity(GithubUserIdentity.builder()
                        .githubUserId(143011364L)
                        .login("pixelfact")
                        .avatarUrl("https://avatars.githubusercontent.com/u/143011364?v=4")
                        .email("pixelfact.company@gmail.com")
                        .build(), false)
        ).thenReturn(AuthenticatedUser.builder()
                .id(UUID.randomUUID())
                .login("pixelfact")
                .avatarUrl("https://avatars.githubusercontent.com/u/143011364?v=4")
                .githubUserId(143011364L)
                .roles(List.of(AuthenticatedUser.Role.USER, AuthenticatedUser.Role.ADMIN))
                .build());

        final String impersonationHeader = """
                {
                  "sub": "github|595505"
                }
                """;
        when(appUserFacadePort
                .getUserByGithubIdentity(GithubUserIdentity.builder()
                        .githubUserId(595505L)
                        .build(), true)
        ).thenReturn(AuthenticatedUser.builder()
                .id(UUID.randomUUID())
                .login("ofux")
                .avatarUrl("https://avatars.githubusercontent.com/u/595505?v=4")
                .githubUserId(595505L)
                .roles(List.of(AuthenticatedUser.Role.USER))
                .build());

        // When
        final var authentication =
                (OnlyDustAppAuthentication) auth0JwtService.getAuthenticationFromJwt(VALID_AUTH0_GITHUB_JWT, impersonationHeader).orElseThrow();

        // Then
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.isImpersonating()).isTrue();
        assertThat(authentication.getImpersonator()).isNotNull();
        assertThat(authentication.getName()).isEqualTo("595505");

        final var user = authentication.getUser();
        assertThat(user.login()).isEqualTo("ofux");
        assertThat(user.githubUserId()).isEqualTo(595505L);
        assertThat(user.roles()).containsExactlyInAnyOrder(AuthenticatedUser.Role.USER);

        final var impersonator = authentication.getImpersonator();
        assertThat(impersonator.login()).isEqualTo("pixelfact");
        assertThat(impersonator.githubUserId()).isEqualTo(143011364L);
    }

    @Test
    void should_reject_impersonation_when_impersonator_is_not_admin() throws IOException, InterruptedException {
        // Given
        when(auth0UserInfoService.getUserInfo(VALID_AUTH0_GITHUB_JWT)).thenReturn(Auth0JwtClaims.builder()
                .nickname("pixelfact")
                .sub("github|143011364")
                .picture("https://avatars.githubusercontent.com/u/143011364?v=4")
                .email("pixelfact.company@gmail.com")
                .name("Mehdi")
                .build());

        when(appUserFacadePort
                .getUserByGithubIdentity(GithubUserIdentity.builder()
                        .githubUserId(31901905L)
                        .login("kaelsky")
                        .avatarUrl("https://avatars.githubusercontent.com/u/31901905?v=4")
                        .build(), false)
        ).thenReturn(AuthenticatedUser.builder()
                .id(UUID.randomUUID())
                .login("kaelsky")
                .avatarUrl("https://avatars.githubusercontent.com/u/31901905?v=4")
                .githubUserId(31901905L)
                .roles(List.of(AuthenticatedUser.Role.USER))
                .build());

        final String impersonationHeader = """
                {
                  "sub": "github|595505"
                }
                """;
        when(appUserFacadePort
                .getUserByGithubIdentity(GithubUserIdentity.builder()
                        .githubUserId(595505L)
                        .build(), true)
        ).thenReturn(AuthenticatedUser.builder()
                .id(UUID.randomUUID())
                .login("ofux")
                .avatarUrl("https://avatars.githubusercontent.com/u/595505?v=4")
                .githubUserId(595505L)
                .roles(List.of(AuthenticatedUser.Role.USER))
                .build());

        // When
        final var authentication = auth0JwtService.getAuthenticationFromJwt(VALID_AUTH0_GITHUB_JWT, impersonationHeader);

        // Then
        assertThat(authentication).isNotPresent();
    }
}