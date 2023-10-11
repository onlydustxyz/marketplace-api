package onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0;

import onlydust.com.marketplace.api.domain.model.GithubUserIdentity;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.port.input.UserFacadePort;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Auth0JwtServiceTest {

    private static final Long ONE_CENTURY = 3153600000L;

    @Test
    void getAuthenticationFromJwt() {
        final UserFacadePort userFacadePort = mock(UserFacadePort.class);
        when(userFacadePort
                .getUserByGithubIdentity(GithubUserIdentity.builder()
                        .githubUserId(31901905L)
                        .githubLogin("kaelsky")
                        .githubAvatarUrl("https://avatars.githubusercontent.com/u/31901905?v=4")
                        .build())
        ).thenReturn(User.builder()
                .id(UUID.randomUUID())
                .login("kaelsky")
                .avatarUrl("https://avatars.githubusercontent.com/u/31901905?v=4")
                .githubUserId(31901905L)
                .permissions(List.of("me"))
                .build());

        final String jwt = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IkQwa2xCQTBncnRhWTQxWmdqVHdSYyJ9.eyJuaWNrbmFtZSI6ImthZWxza3kiLCJuYW1lIjoiTWlja2FlbC5DIiwicGljdHVyZSI6Imh0dHBzOi8vYXZhdGFycy5naXRodWJ1c2VyY29udGVudC5jb20vdS8zMTkwMTkwNT92PTQiLCJ1cGRhdGVkX2F0IjoiMjAyMy0xMC0xMFQxMzo1NTo0OC4zMDhaIiwiaXNzIjoiaHR0cHM6Ly9vbmx5ZHVzdC1oYWNrYXRob24uZXUuYXV0aDAuY29tLyIsImF1ZCI6IjYyR0RnMmE2cENqbkFsbjFGY2NENTVlQ0tMSnRqNFQ1IiwiaWF0IjoxNjk2OTQ3OTMzLCJleHAiOjE2OTY5ODM5MzMsInN1YiI6ImdpdGh1YnwzMTkwMTkwNSIsInNpZCI6IjIxRkZFdDN5VTJFU0ZjVHRxVzV4QWlsUkZKMDRhdVViIiwibm9uY2UiOiJqNEN3WkkxMXV1VjN0RHp3cTRVeURFS2lXaUlnLVozZldXV1V6cDJVWElrIn0.MqeGFd6w3RuWTYwRHZ3s82P1C_SFOQJgLtOU6GYwe7KdigVaerPxjF8nwe8mrsg_g91_TpFxvpBlo3Hy6UiVrdN33HJjFGP29yJCYPR-PWCpt2rgboQCIuteq_OP4x6tdIL3ad0Ehm4PAeJZwg4RqKNPwj5EL0AV8tlNwN5elLG-9mVTZVWyEwV9xDgwAit4CJ4qGvheOhP-NQGIx4g9FElYy6Bw-XyI7rVFzT9h1Cxc3T2OWO2jgiuDVfHD_Q0Wz1uzD6s6eqPLuSNxmJtye7r-QOpuOgUIyVcKCs-WUuhhsQ4vad7lq3fmqUbSZ2xJPXBdwcfUZFfShAfAy3VK_g";
        final Auth0JwtVerifier jwtVerifier = new Auth0JwtVerifier(Auth0Properties.builder()
                .jwksUrl("https://onlydust-hackathon.eu.auth0.com/")
                .expiresAtLeeway(ONE_CENTURY)
                .build());
        final Auth0JwtService auth0JwtService = new Auth0JwtService(jwtVerifier, userFacadePort);
        final Auth0Authentication authentication = auth0JwtService.getAuthenticationFromJwt(jwt);

        assertTrue(authentication.isAuthenticated());
        assertEquals("github|31901905", authentication.getName());

        final User user = (User) authentication.getDetails();
        assertEquals("kaelsky", user.getLogin());
        assertEquals(31901905, user.getGithubUserId());
    }
}