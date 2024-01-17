package onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0;

import com.fasterxml.jackson.databind.ObjectMapper;
import onlydust.com.marketplace.api.domain.model.GithubUserIdentity;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.model.UserRole;
import onlydust.com.marketplace.api.domain.port.input.UserFacadePort;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Auth0JwtServiceTest {

    private static final Long ONE_CENTURY = 3153600000L;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String VALID_JWT = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IjJHNFZvZlVLMURVQklDUF9CcUdVcyJ9" +
                                            ".eyJpc3MiOiJodHRwczovL2RldmVsb3Atb25seWR1c3QuZXUuYXV0aDAuY29tLyIsInN1YiI6ImdpdGh1YnwxNDMwMTEzNjQiLCJhdWQiOlsiaHR0cHM6Ly9kZXZlbG9wLW9ubHlkdXN0LmV1LmF1dGgwLmNvbS9hcGkvdjIvIiwiaHR0cHM6Ly9kZXZlbG9wLW9ubHlkdXN0LmV1LmF1dGgwLmNvbS91c2VyaW5mbyJdLCJpYXQiOjE3MDU0MTc1NjYsImV4cCI6MTcwNTUwMzk2NiwiYXpwIjoiZ2ZPZGlGT2x0WVlVTVllQnpOcGVOQWpNSG1iOWZXb1YiLCJzY29wZSI6Im9wZW5pZCBwcm9maWxlIGVtYWlsIG9mZmxpbmVfYWNjZXNzIn0.Z2WuBxiQy9DBOb2olIyj9JgidYClYenOy4rHG-Uu0ZFRuzEbzk4003NaWq-9j-O9EYYRLlT2hlIvXWAU0r0f2IDdtW3IpDpOV2Zj9-1ZMk-SYCWRlNaO3gh0bEqyGznIWmztr7gPdCtHPliG6l1A--uZBXKjUwi5XnVRhoLU9yJO6znPoGNQ-b5wnSGE8cmUyVkM_mGOU1FlkGIweG6ZKEQ-EoGPwC57nIxUpzRvmCD-5VUycb7M5vE4ktkyVFA_Bnp9O8FXLK_EIedFMgGJd0QWRboRbEerwQJHL9eghXXUDonq6P12zXdL3d_Edqzrq73F0UkI9ZMIEkex-SDfpQ";

    //    @Test
    void should_authenticate_from_a_valid_jwt() {
        // Given
        final UserFacadePort userFacadePort = mock(UserFacadePort.class);
        when(userFacadePort
                .getUserByGithubIdentity(GithubUserIdentity.builder()
                        .githubUserId(143011364L)
                        .githubLogin("pixelfact")
                        .githubAvatarUrl("https://avatars.githubusercontent.com/u/143011364?v=4")
                        .email("pixelfact.company@gmail.com")
                        .build(), false)
        ).thenReturn(User.builder()
                .id(UUID.randomUUID())
                .githubLogin("pixelfact")
                .githubAvatarUrl("https://avatars.githubusercontent.com/u/143011364?v=4")
                .githubUserId(143011364L)
                .roles(List.of(UserRole.USER))
                .hasSeenOnboardingWizard(true)
                .hasAcceptedLatestTermsAndConditions(true)
                .build());

        final Auth0Properties auth0Properties = Auth0Properties.builder()
                .jwksUrl("https://develop-onlydust.eu.auth0.com/")
                .expiresAtLeeway(ONE_CENTURY)
                .build();
        final Auth0JwtVerifier jwtVerifier = new Auth0JwtVerifier(auth0Properties);
        final Auth0JwtService auth0JwtService = new Auth0JwtService(objectMapper, jwtVerifier, userFacadePort, HttpClient.newHttpClient(), auth0Properties);

        // When
        final var authentication = auth0JwtService.getAuthenticationFromJwt(VALID_JWT, null).orElseThrow();

        // Then
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.isImpersonating()).isFalse();
        assertThat(authentication.getImpersonator()).isNull();
        assertThat(authentication.getName()).isEqualTo("143011364");

        final User user = authentication.getUser();
        assertThat(user.getGithubLogin()).isEqualTo("pixelfact");
        assertThat(user.getGithubUserId()).isEqualTo(143011364L);
        assertThat(user.getRoles()).containsExactlyInAnyOrder(UserRole.USER);
        assertThat(user.hasSeenOnboardingWizard()).isTrue();
        assertThat(user.hasAcceptedLatestTermsAndConditions()).isTrue();
    }

    @Test
    void should_not_authenticate_from_an_invalid_jwt() {
        final UserFacadePort userFacadePort = mock(UserFacadePort.class);
        when(userFacadePort
                .getUserByGithubIdentity(GithubUserIdentity.builder()
                        .githubUserId(31901905L)
                        .githubLogin("kaelsky")
                        .githubAvatarUrl("https://avatars.githubusercontent.com/u/31901905?v=4")
                        .build(), false)
        ).thenReturn(User.builder()
                .id(UUID.randomUUID())
                .githubLogin("kaelsky")
                .githubAvatarUrl("https://avatars.githubusercontent.com/u/31901905?v=4")
                .githubUserId(31901905L)
                .roles(List.of(UserRole.USER))
                .build());

        final String jwt = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IjJHNFZvZlVLMURVQklDUF9CcUdVcyJ9" +
                           ".eyJuaWNrbmFtZSI6InBpeGVsZmFjdCIsIm5hbWUiOiJNZWhkaSBIYW1yaSIsInBpY3R1cmUiOiJodHRwczovL2F2YXRhcnMuZ2l0aHVidXNlcmNvbnRlbnQuY29tL3UvMTQzMDExMzY0P3Y9NCIsInVwZGF0ZWRfYXQiOiIyMDI0LTAxLTA1VDA5OjUxOjI5LjI3NFoiLCJlbWFpbCI6InBpeGVsZmFjdC5jb21wYW55QGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJpc3MiOiJodHRwczovL2RldmVsb3Atb25seWR1c3QuZXUuYXV0aDAuY29tLyIsImF1ZCI6ImdmT2RpRk9sdFlZVU1ZZUJ6TnBlTkFqTUhtYjlmV29WIiwiaWF0IjoxNzA0NDYzMTQ1LCJleHAiOjE3MDQ0OTkxNDUsInN1YiI6ImdpdGh1YnwxNDMwMTEzNjQiLCJzaWQiOiI3dTZPanZ5c0kwamJ3MVg4MmMwM19YZ3Y5YXNCWVFkUyIsIm5vbmNlIjoiTURGWWVVZzNPRU14TUMxdFdsOUNjelJVTUVJdFNHWndUalpLVkhJdGIycFFYMFU1TVU1MVh6WkxTQT09In0.byC3f9c5DRnj5xmHw7ZpRJxUxDSqS_3h9tjTMG2cDeJp47KHVVGp7Tm_rPasoLwzTXgcJn37xw1I-ZETJmZVf81lVZR42JgP2V0QCrAU3jjJr_qEhyBZKSs7ip1KdZlcNLfn37wXbQiDE3OICJKMH3Pae_hv5NGQDYhvpz6AcjiFJpBkG0iYRpO3LHV7stxo8usStOwg5CZ04GGm9gu2LvtHK1d52tHKyFSPXnm-kVSB2g1VPpEdRF48pgjH3TJ6EI2sr19ShEBH6ZZBH1m3i_absMJk0UHNZM9VDo_goaJImEgSwXLgxJoYeGetHeoZYeX0r9jf-CJxgqGwIhIHWg";
        final Auth0Properties auth0Properties = Auth0Properties.builder()
                .jwksUrl("https://develop-onlydust.eu.auth0.com/")
                .expiresAtLeeway(ONE_CENTURY)
                .build();
        final Auth0JwtVerifier jwtVerifier = new Auth0JwtVerifier(auth0Properties);
        final Auth0JwtService auth0JwtService = new Auth0JwtService(objectMapper, jwtVerifier, userFacadePort, HttpClient.newHttpClient(), auth0Properties);
        final var authentication = auth0JwtService.getAuthenticationFromJwt(jwt, null);

        assertThat(authentication).isEmpty();
    }

    //    @Test
    void should_authenticate_given_a_valid_jwt_and_impersonation_header() {
        // Given
        final UserFacadePort userFacadePort = mock(UserFacadePort.class);
        when(userFacadePort
                .getUserByGithubIdentity(GithubUserIdentity.builder()
                        .githubUserId(143011364L)
                        .githubLogin("pixelfact")
                        .githubAvatarUrl("https://avatars.githubusercontent.com/u/143011364?v=4")
                        .email("pixelfact.company@gmail.com")
                        .build(), false)
        ).thenReturn(User.builder()
                .id(UUID.randomUUID())
                .githubLogin("pixelfact")
                .githubAvatarUrl("https://avatars.githubusercontent.com/u/143011364?v=4")
                .githubUserId(143011364L)
                .roles(List.of(UserRole.USER, UserRole.ADMIN))
                .hasSeenOnboardingWizard(true)
                .hasAcceptedLatestTermsAndConditions(true)
                .build());

        final String impersonationHeader = """
                {
                  "sub": "github|595505"
                }
                """;
        when(userFacadePort
                .getUserByGithubIdentity(GithubUserIdentity.builder()
                        .githubUserId(595505L)
                        .build(), true)
        ).thenReturn(User.builder()
                .id(UUID.randomUUID())
                .githubLogin("ofux")
                .githubAvatarUrl("https://avatars.githubusercontent.com/u/595505?v=4")
                .githubUserId(595505L)
                .roles(List.of(UserRole.USER))
                .build());

        final Auth0Properties auth0Properties = Auth0Properties.builder()
                .jwksUrl("https://develop-onlydust.eu.auth0.com/")
                .expiresAtLeeway(ONE_CENTURY)
                .build();
        final Auth0JwtVerifier jwtVerifier = new Auth0JwtVerifier(auth0Properties);
        final Auth0JwtService auth0JwtService = new Auth0JwtService(objectMapper, jwtVerifier, userFacadePort, HttpClient.newHttpClient(), auth0Properties);

        // When
        final var authentication = auth0JwtService.getAuthenticationFromJwt(VALID_JWT, impersonationHeader).orElseThrow();

        // Then
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.isImpersonating()).isTrue();
        assertThat(authentication.getImpersonator()).isNotNull();
        assertThat(authentication.getName()).isEqualTo("595505");

        final User user = authentication.getUser();
        assertThat(user.getGithubLogin()).isEqualTo("ofux");
        assertThat(user.getGithubUserId()).isEqualTo(595505L);
        assertThat(user.getRoles()).containsExactlyInAnyOrder(UserRole.USER);

        final User impersonator = authentication.getImpersonator();
        assertThat(impersonator.getGithubLogin()).isEqualTo("pixelfact");
        assertThat(impersonator.getGithubUserId()).isEqualTo(143011364L);
    }

    @Test
    void should_reject_impersonation_when_impersonator_is_not_admin() {
        // Given
        final UserFacadePort userFacadePort = mock(UserFacadePort.class);
        when(userFacadePort
                .getUserByGithubIdentity(GithubUserIdentity.builder()
                        .githubUserId(31901905L)
                        .githubLogin("kaelsky")
                        .githubAvatarUrl("https://avatars.githubusercontent.com/u/31901905?v=4")
                        .build(), false)
        ).thenReturn(User.builder()
                .id(UUID.randomUUID())
                .githubLogin("kaelsky")
                .githubAvatarUrl("https://avatars.githubusercontent.com/u/31901905?v=4")
                .githubUserId(31901905L)
                .roles(List.of(UserRole.USER))
                .build());

        final String impersonationHeader = """
                {
                  "sub": "github|595505"
                }
                """;
        when(userFacadePort
                .getUserByGithubIdentity(GithubUserIdentity.builder()
                        .githubUserId(595505L)
                        .build(), true)
        ).thenReturn(User.builder()
                .id(UUID.randomUUID())
                .githubLogin("ofux")
                .githubAvatarUrl("https://avatars.githubusercontent.com/u/595505?v=4")
                .githubUserId(595505L)
                .roles(List.of(UserRole.USER))
                .build());

        final Auth0Properties auth0Properties = Auth0Properties.builder()
                .jwksUrl("https://develop-onlydust.eu.auth0.com/")
                .expiresAtLeeway(ONE_CENTURY)
                .build();
        final Auth0JwtVerifier jwtVerifier = new Auth0JwtVerifier(auth0Properties);
        final Auth0JwtService auth0JwtService = new Auth0JwtService(objectMapper, jwtVerifier, userFacadePort, HttpClient.newHttpClient(), auth0Properties);

        // When
        final var authentication = auth0JwtService.getAuthenticationFromJwt(VALID_JWT, impersonationHeader);

        // Then
        assertThat(authentication).isNotPresent();
    }
}