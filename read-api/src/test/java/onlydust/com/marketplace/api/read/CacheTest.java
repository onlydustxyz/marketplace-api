package onlydust.com.marketplace.api.read;

import onlydust.com.marketplace.api.read.properties.Cache;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class CacheTest {

    @ParameterizedTest
    @CsvSource("""
            10, 1, 10
            10, 2, 5
            10, 3, 3
            10, 4, 2
            10, 20, 0
            """)
    void maxAge(long maxAgeSeconds, long maxAgeDivisor, long expectedSeconds) {
        final var cache = new Cache(maxAgeDivisor, false, 10);
        assertThat(cache.maxAge(maxAgeSeconds, TimeUnit.SECONDS).getHeaderValue())
                .isEqualTo("max-age=%d".formatted(expectedSeconds));
        assertThat(cache.forEverybody(maxAgeSeconds, TimeUnit.SECONDS).getHeaderValue())
                .isEqualTo("max-age=%d, stale-while-revalidate=10".formatted(expectedSeconds));
        assertThat(cache.whenAnonymous(Optional.empty(), maxAgeSeconds, TimeUnit.SECONDS).getHeaderValue())
                .isEqualTo(("max-age=%d, stale-while-revalidate=10").formatted(expectedSeconds));
        assertThat(cache.whenAnonymous(Optional.of(AuthenticatedUser.builder().build()), maxAgeSeconds, TimeUnit.SECONDS).getHeaderValue())
                .isEqualTo("private");
    }

    @ParameterizedTest
    @CsvSource("""
            10, 1, 600
            10, 2, 300
            10, 3, 200
            10, 4, 150
            10, 2000, 0
            """)
    void cache(long maxAgeMinutes, long maxAgeDivisor, long expectedSeconds) {
        final var cache = new Cache(maxAgeDivisor, false, 10);
        assertThat(cache.maxAge(maxAgeMinutes, TimeUnit.MINUTES).getHeaderValue())
                .isEqualTo("max-age=%d".formatted(expectedSeconds));
        assertThat(cache.forEverybody(maxAgeMinutes, TimeUnit.MINUTES).getHeaderValue())
                .isEqualTo("max-age=%d, stale-while-revalidate=10".formatted(expectedSeconds));
        assertThat(cache.whenAnonymous(Optional.empty(), maxAgeMinutes, TimeUnit.MINUTES).getHeaderValue())
                .isEqualTo(("max-age=%d, stale-while-revalidate=10").formatted(expectedSeconds));
        assertThat(cache.whenAnonymous(Optional.of(AuthenticatedUser.builder().build()), maxAgeMinutes, TimeUnit.MINUTES).getHeaderValue())
                .isEqualTo("private");
    }

    @Test
    void noCache() {
        final var cache = new Cache(1L, true, 10);
        assertThat(cache.maxAge(20, TimeUnit.SECONDS).getHeaderValue()).isEqualTo("no-store");
        assertThat(cache.forEverybody(20, TimeUnit.SECONDS).getHeaderValue()).isEqualTo("no-store");
    }

    @Test
    void defaultStale() {
        final var cache = new Cache(1L, false, 40);
        assertThat(cache.forEverybody(20, TimeUnit.SECONDS).getHeaderValue()).isEqualTo("max-age=20, stale-while-revalidate=40");
    }
}