package onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0;

import onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt.JwtClaims;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Auth0JwtServiceTest {

    @Test
    void getAuthenticationFromJwt() {
        final String jwt = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IkQwa2xCQTBncnRhWTQxWmdqVHdSYyJ9.eyJuaWNrbmFtZSI6ImthZWxza3kiLCJuYW1lIjoiTWlja2FlbC5DIiwicGljdHVyZSI6Imh0dHBzOi8vYXZhdGFycy5naXRodWJ1c2VyY29udGVudC5jb20vdS8zMTkwMTkwNT92PTQiLCJ1cGRhdGVkX2F0IjoiMjAyMy0xMC0xMFQxMzo1NTo0OC4zMDhaIiwiaXNzIjoiaHR0cHM6Ly9vbmx5ZHVzdC1oYWNrYXRob24uZXUuYXV0aDAuY29tLyIsImF1ZCI6IjYyR0RnMmE2cENqbkFsbjFGY2NENTVlQ0tMSnRqNFQ1IiwiaWF0IjoxNjk2OTQ3OTMzLCJleHAiOjE2OTY5ODM5MzMsInN1YiI6ImdpdGh1YnwzMTkwMTkwNSIsInNpZCI6IjIxRkZFdDN5VTJFU0ZjVHRxVzV4QWlsUkZKMDRhdVViIiwibm9uY2UiOiJqNEN3WkkxMXV1VjN0RHp3cTRVeURFS2lXaUlnLVozZldXV1V6cDJVWElrIn0.MqeGFd6w3RuWTYwRHZ3s82P1C_SFOQJgLtOU6GYwe7KdigVaerPxjF8nwe8mrsg_g91_TpFxvpBlo3Hy6UiVrdN33HJjFGP29yJCYPR-PWCpt2rgboQCIuteq_OP4x6tdIL3ad0Ehm4PAeJZwg4RqKNPwj5EL0AV8tlNwN5elLG-9mVTZVWyEwV9xDgwAit4CJ4qGvheOhP-NQGIx4g9FElYy6Bw-XyI7rVFzT9h1Cxc3T2OWO2jgiuDVfHD_Q0Wz1uzD6s6eqPLuSNxmJtye7r-QOpuOgUIyVcKCs-WUuhhsQ4vad7lq3fmqUbSZ2xJPXBdwcfUZFfShAfAy3VK_g";
        final Auth0JwtService auth0JwtService = new Auth0JwtService("https://onlydust-hackathon.eu.auth0.com/");
        final Auth0Authentication authentication = auth0JwtService.getAuthenticationFromJwt(jwt);

        assertTrue(authentication.isAuthenticated());
        assertEquals("github|31901905", authentication.getName());

        final JwtClaims claims = (JwtClaims) authentication.getDetails();
        assertEquals("kaelsky", claims.getGithubLogin());
        assertEquals(31901905, claims.getGithubUserId());
    }
}