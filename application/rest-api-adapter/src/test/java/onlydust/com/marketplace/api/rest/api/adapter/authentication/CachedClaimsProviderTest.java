package onlydust.com.marketplace.api.rest.api.adapter.authentication;

import onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0.Auth0Properties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

class CachedClaimsProviderTest {
    @SuppressWarnings("unchecked")
    private final ClaimsProvider<String> claimsProvider = mock(ClaimsProvider.class);
    private final Auth0Properties properties = Auth0Properties.builder().accessTokenCacheTtlInSeconds(1L).build();
    private final CachedClaimsProvider<String> cachedClaimsProvider = new CachedClaimsProvider<>(claimsProvider, properties);

    @Test
    void should_cache_requests_for_a_given_time() {
        // Given
        when(claimsProvider.getClaimsFromAccessToken("access-token")).thenReturn("claims");

        // When
        assertThat(cachedClaimsProvider.getClaimsFromAccessToken("access-token")).isEqualTo("claims");
        assertThat(cachedClaimsProvider.getClaimsFromAccessToken("access-token")).isEqualTo("claims");
        assertThat(cachedClaimsProvider.getClaimsFromAccessToken("access-token")).isEqualTo("claims");

        // Then
        verify(claimsProvider, times(1)).getClaimsFromAccessToken("access-token");
    }

    @Test
    void should_invalidate_cache_after_ttl() throws InterruptedException {
        // Given
        when(claimsProvider.getClaimsFromAccessToken("access-token")).thenReturn("claims");

        // When
        assertThat(cachedClaimsProvider.getClaimsFromAccessToken("access-token")).isEqualTo("claims");
        assertThat(cachedClaimsProvider.getClaimsFromAccessToken("access-token")).isEqualTo("claims");
        Thread.sleep(2000);
        assertThat(cachedClaimsProvider.getClaimsFromAccessToken("access-token")).isEqualTo("claims");

        // Then
        verify(claimsProvider, times(2)).getClaimsFromAccessToken("access-token");
    }
}