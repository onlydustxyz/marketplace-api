package onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0;

import com.fasterxml.jackson.databind.ObjectMapper;
import onlydust.com.marketplace.api.domain.model.GithubUserIdentity;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.model.UserRole;
import onlydust.com.marketplace.api.domain.port.input.UserFacadePort;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Auth0JwtServiceTest {

    private static final Long ONE_CENTURY = 3153600000L;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void should_authenticate_from_a_valid_jwt() {
        // Given
        final UserFacadePort userFacadePort = mock(UserFacadePort.class);
        when(userFacadePort
                .getUserByGithubIdentity(GithubUserIdentity.builder()
                        .githubUserId(143011364L)
                        .githubLogin("pixelfact")
                        .githubAvatarUrl("https://avatars.githubusercontent.com/u/143011364?v=4")
                        .email("pixelfact.company@gmail.com")
                        .build(), true)
        ).thenReturn(User.builder()
                .id(UUID.randomUUID())
                .githubLogin("pixelfact")
                .githubAvatarUrl("https://avatars.githubusercontent.com/u/143011364?v=4")
                .githubUserId(143011364L)
                .roles(List.of(UserRole.USER))
                .hasSeenOnboardingWizard(true)
                .hasAcceptedLatestTermsAndConditions(true)
                .build());

        final String jwt = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IjJHNFZvZlVLMURVQklDUF9CcUdVcyJ9" +
                           ".eyJuaWNrbmFtZSI6InBpeGVsZmFjdCIsIm5hbWUiOiJNZWhkaSBIYW1yaSIsInBpY3R1cmUiOiJodHRwczovL2F2YXRhcnMuZ2l0aHVidXNlcmNvbnRlbnQuY29tL3UvMTQzMDExMzY0P3Y9NCIsInVwZGF0ZWRfYXQiOiIyMDI0LTAxLTA1VDA5OjUxOjI5LjI3NFoiLCJlbWFpbCI6InBpeGVsZmFjdC5jb21wYW55QGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJpc3MiOiJodHRwczovL2RldmVsb3Atb25seWR1c3QuZXUuYXV0aDAuY29tLyIsImF1ZCI6ImdmT2RpRk9sdFlZVU1ZZUJ6TnBlTkFqTUhtYjlmV29WIiwiaWF0IjoxNzA0NDYzMTQ1LCJleHAiOjE3MDQ0OTkxNDUsInN1YiI6ImdpdGh1YnwxNDMwMTEzNjQiLCJzaWQiOiI3dTZPanZ5c0kwamJ3MVg4MmMwM19YZ3Y5YXNCWVFkUyIsIm5vbmNlIjoiTURGWWVVZzNPRU14TUMxdFdsOUNjelJVTUVJdFNHWndUalpLVkhJdGIycFFYMFU1TVU1MVh6WkxTQT09In0.byC3f9c5DRnj5xmHw7ZpRJxUxDSqS_3h9tjTMG2cDeJp47KHVVGp7Tm_rPasoLwzTXgcJn37xw1I-ZETJmZVf81lVZR42JgP2V0QCrAU3jjJr_qEhyBZKSs7ip1KdZlcNLfn37wXbQiDE3OICJKMH3Pae_hv5NGQDYhvpz6AcjiFJpBkG0iYRpO3LHV7stxo8usStOwg5CZ04GGm9gu2LvtHK1d52tHKyFSPXnm-kVSB2g1VPpEdRF48pgjH3TJ6EI2sr19ShEBH6ZZBH1m3i_absMJk0UHNZM9VDo_goaJImEgSwXLgxJoYeGetHeoZYeX0r9jf-CJxgqGwIhIHWg";
        final Auth0JwtVerifier jwtVerifier = new Auth0JwtVerifier(Auth0Properties.builder()
                .jwksUrl("https://develop-onlydust.eu.auth0.com/")
                .expiresAtLeeway(ONE_CENTURY)
                .build());
        final Auth0JwtService auth0JwtService = new Auth0JwtService(objectMapper, jwtVerifier, userFacadePort);

        // When
        final var authentication = auth0JwtService.getAuthenticationFromJwt(jwt, null).orElseThrow();

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
                        .build(), true)
        ).thenReturn(User.builder()
                .id(UUID.randomUUID())
                .githubLogin("kaelsky")
                .githubAvatarUrl("https://avatars.githubusercontent.com/u/31901905?v=4")
                .githubUserId(31901905L)
                .roles(List.of(UserRole.USER))
                .build());

        final String jwt = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IjJHNFZvZlVLMURVQklDUF9CcUdVcyJ9" +
                           ".eyJuaWNrbmFtZSI6InBpeGVsZmFjdCIsIm5hbWUiOiJNZWhkaSBIYW1yaSIsInBpY3R1cmUiOiJodHRwczovL2F2YXRhcnMuZ2l0aHVidXNlcmNvbnRlbnQuY29tL3UvMTQzMDExMzY0P3Y9NCIsInVwZGF0ZWRfYXQiOiIyMDI0LTAxLTA1VDA5OjUxOjI5LjI3NFoiLCJlbWFpbCI6InBpeGVsZmFjdC5jb21wYW55QGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJpc3MiOiJodHRwczovL2RldmVsb3Atb25seWR1c3QuZXUuYXV0aDAuY29tLyIsImF1ZCI6ImdmT2RpRk9sdFlZVU1ZZUJ6TnBlTkFqTUhtYjlmV29WIiwiaWF0IjoxNzA0NDYzMTQ1LCJleHAiOjE3MDQ0OTkxNDUsInN1YiI6ImdpdGh1YnwxNDMwMTEzNjQiLCJzaWQiOiI3dTZPanZ5c0kwamJ3MVg4MmMwM19YZ3Y5YXNCWVFkUyIsIm5vbmNlIjoiTURGWWVVZzNPRU14TUMxdFdsOUNjelJVTUVJdFNHWndUalpLVkhJdGIycFFYMFU1TVU1MVh6WkxTQT09In0.byC3f9c5DRnj5xmHw7ZpRJxUxDSqS_3h9tjTMG2cDeJp47KHVVGp7Tm_rPasoLwzTXgcJn37xw1I-ZETJmZVf81lVZR42JgP2V0QCrAU3jjJr_qEhyBZKSs7ip1KdZlcNLfn37wXbQiDE3OICJKMH3Pae_hv5NGQDYhvpz6AcjiFJpBkG0iYRpO3LHV7stxo8usStOwg5CZ04GGm9gu2LvtHK1d52tHKyFSPXnm-kVSB2g1VPpEdRF48pgjH3TJ6EI2sr19ShEBH6ZZBH1m3i_absMJk0UHNZM9VDo_goaJImEgSwXLgxJoYeGetHeoZYeX0r9jf-CJxgqGwIhIHWg";
        final Auth0JwtVerifier jwtVerifier = new Auth0JwtVerifier(Auth0Properties.builder()
                .jwksUrl("https://develop-onlydust.eu.auth0.com/")
                .expiresAtLeeway(ONE_CENTURY)
                .build());
        final Auth0JwtService auth0JwtService = new Auth0JwtService(objectMapper, jwtVerifier, userFacadePort);
        final var authentication = auth0JwtService.getAuthenticationFromJwt(jwt, null);

        assertThat(authentication).isEmpty();
    }

    @Test
    void should_authenticate_given_a_valid_jwt_and_impersonation_header() {
        // Given
        final UserFacadePort userFacadePort = mock(UserFacadePort.class);
        when(userFacadePort
                .getUserByGithubIdentity(GithubUserIdentity.builder()
                        .githubUserId(143011364L)
                        .githubLogin("pixelfact")
                        .githubAvatarUrl("https://avatars.githubusercontent.com/u/143011364?v=4")
                        .email("pixelfact.company@gmail.com")
                        .build(), true)
        ).thenReturn(User.builder()
                .id(UUID.randomUUID())
                .githubLogin("pixelfact")
                .githubAvatarUrl("https://avatars.githubusercontent.com/u/143011364?v=4")
                .githubUserId(143011364L)
                .roles(List.of(UserRole.USER, UserRole.ADMIN))
                .hasSeenOnboardingWizard(true)
                .hasAcceptedLatestTermsAndConditions(true)
                .build());

        final String jwt = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IjJHNFZvZlVLMURVQklDUF9CcUdVcyJ9" +
                           ".eyJuaWNrbmFtZSI6InBpeGVsZmFjdCIsIm5hbWUiOiJNZWhkaSBIYW1yaSIsInBpY3R1cmUiOiJodHRwczovL2F2YXRhcnMuZ2l0aHVidXNlcmNvbnRlbnQuY29tL3UvMTQzMDExMzY0P3Y9NCIsInVwZGF0ZWRfYXQiOiIyMDI0LTAxLTA1VDA5OjUxOjI5LjI3NFoiLCJlbWFpbCI6InBpeGVsZmFjdC5jb21wYW55QGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJpc3MiOiJodHRwczovL2RldmVsb3Atb25seWR1c3QuZXUuYXV0aDAuY29tLyIsImF1ZCI6ImdmT2RpRk9sdFlZVU1ZZUJ6TnBlTkFqTUhtYjlmV29WIiwiaWF0IjoxNzA0NDYzMTQ1LCJleHAiOjE3MDQ0OTkxNDUsInN1YiI6ImdpdGh1YnwxNDMwMTEzNjQiLCJzaWQiOiI3dTZPanZ5c0kwamJ3MVg4MmMwM19YZ3Y5YXNCWVFkUyIsIm5vbmNlIjoiTURGWWVVZzNPRU14TUMxdFdsOUNjelJVTUVJdFNHWndUalpLVkhJdGIycFFYMFU1TVU1MVh6WkxTQT09In0.byC3f9c5DRnj5xmHw7ZpRJxUxDSqS_3h9tjTMG2cDeJp47KHVVGp7Tm_rPasoLwzTXgcJn37xw1I-ZETJmZVf81lVZR42JgP2V0QCrAU3jjJr_qEhyBZKSs7ip1KdZlcNLfn37wXbQiDE3OICJKMH3Pae_hv5NGQDYhvpz6AcjiFJpBkG0iYRpO3LHV7stxo8usStOwg5CZ04GGm9gu2LvtHK1d52tHKyFSPXnm-kVSB2g1VPpEdRF48pgjH3TJ6EI2sr19ShEBH6ZZBH1m3i_absMJk0UHNZM9VDo_goaJImEgSwXLgxJoYeGetHeoZYeX0r9jf-CJxgqGwIhIHWg";
        final Auth0JwtVerifier jwtVerifier = new Auth0JwtVerifier(Auth0Properties.builder()
                .jwksUrl("https://develop-onlydust.eu.auth0.com/")
                .expiresAtLeeway(ONE_CENTURY)
                .build());

        final String impersonationHeader = """
                {
                  "nickname": "ofux",
                  "picture": "https://avatars.githubusercontent.com/u/595505?v=4",
                  "sub": "github|595505"
                }
                """;
        when(userFacadePort
                .getUserByGithubIdentity(GithubUserIdentity.builder()
                        .githubUserId(595505L)
                        .githubLogin("ofux")
                        .githubAvatarUrl("https://avatars.githubusercontent.com/u/595505?v=4")
                        .build(), false)
        ).thenReturn(User.builder()
                .id(UUID.randomUUID())
                .githubLogin("ofux")
                .githubAvatarUrl("https://avatars.githubusercontent.com/u/595505?v=4")
                .githubUserId(595505L)
                .roles(List.of(UserRole.USER))
                .build());

        final Auth0JwtService auth0JwtService = new Auth0JwtService(objectMapper, jwtVerifier, userFacadePort);

        // When
        final var authentication = auth0JwtService.getAuthenticationFromJwt(jwt, impersonationHeader).orElseThrow();

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
                        .build(), true)
        ).thenReturn(User.builder()
                .id(UUID.randomUUID())
                .githubLogin("kaelsky")
                .githubAvatarUrl("https://avatars.githubusercontent.com/u/31901905?v=4")
                .githubUserId(31901905L)
                .roles(List.of(UserRole.USER))
                .build());

        final String jwt = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IkQwa2xCQTBncnRhWTQxWmdqVHdSYyJ9" +
                           ".eyJuaWNrbmFtZSI6ImthZWxza3kiLCJuYW1lIjoiTWlja2FlbC5DIiwicGljdHVyZSI6Imh0dHBzOi8vYXZhdGFycy5naXRodWJ1c2VyY29udGVudC5jb20vdS8zMTkwMTkwNT92PTQiLCJ1cGRhdGVkX2F0IjoiMjAyMy0xMC0xMFQxMzo1NTo0OC4zMDhaIiwiaXNzIjoiaHR0cHM6Ly9vbmx5ZHVzdC1oYWNrYXRob24uZXUuYXV0aDAuY29tLyIsImF1ZCI6IjYyR0RnMmE2cENqbkFsbjFGY2NENTVlQ0tMSnRqNFQ1IiwiaWF0IjoxNjk2OTQ3OTMzLCJleHAiOjE2OTY5ODM5MzMsInN1YiI6ImdpdGh1YnwzMTkwMTkwNSIsInNpZCI6IjIxRkZFdDN5VTJFU0ZjVHRxVzV4QWlsUkZKMDRhdVViIiwibm9uY2UiOiJqNEN3WkkxMXV1VjN0RHp3cTRVeURFS2lXaUlnLVozZldXV1V6cDJVWElrIn0.MqeGFd6w3RuWTYwRHZ3s82P1C_SFOQJgLtOU6GYwe7KdigVaerPxjF8nwe8mrsg_g91_TpFxvpBlo3Hy6UiVrdN33HJjFGP29yJCYPR-PWCpt2rgboQCIuteq_OP4x6tdIL3ad0Ehm4PAeJZwg4RqKNPwj5EL0AV8tlNwN5elLG-9mVTZVWyEwV9xDgwAit4CJ4qGvheOhP-NQGIx4g9FElYy6Bw-XyI7rVFzT9h1Cxc3T2OWO2jgiuDVfHD_Q0Wz1uzD6s6eqPLuSNxmJtye7r-QOpuOgUIyVcKCs-WUuhhsQ4vad7lq3fmqUbSZ2xJPXBdwcfUZFfShAfAy3VK_g";
        final Auth0JwtVerifier jwtVerifier = new Auth0JwtVerifier(Auth0Properties.builder()
                .jwksUrl("https://onlydust-hackathon.eu.auth0.com/")
                .expiresAtLeeway(ONE_CENTURY)
                .build());

        final String impersonationHeader = """
                {
                  "nickname": "ofux",
                  "picture": "https://avatars.githubusercontent.com/u/595505?v=4",
                  "sub": "github|595505"
                }
                """;
        when(userFacadePort
                .getUserByGithubIdentity(GithubUserIdentity.builder()
                        .githubUserId(595505L)
                        .githubLogin("ofux")
                        .githubAvatarUrl("https://avatars.githubusercontent.com/u/595505?v=4")
                        .build(), false)
        ).thenReturn(User.builder()
                .id(UUID.randomUUID())
                .githubLogin("ofux")
                .githubAvatarUrl("https://avatars.githubusercontent.com/u/595505?v=4")
                .githubUserId(595505L)
                .roles(List.of(UserRole.USER))
                .build());

        final Auth0JwtService auth0JwtService = new Auth0JwtService(objectMapper, jwtVerifier, userFacadePort);

        // When
        final var authentication = auth0JwtService.getAuthenticationFromJwt(jwt, impersonationHeader);

        // Then
        assertThat(authentication).isNotPresent();
    }
}