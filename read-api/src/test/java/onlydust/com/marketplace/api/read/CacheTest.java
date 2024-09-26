package onlydust.com.marketplace.api.read;

import onlydust.com.marketplace.api.read.properties.Cache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
        assertThat(cache.maxAge(maxAgeSeconds, TimeUnit.SECONDS).getHeaderValue()).isEqualTo("max-age=%d".formatted(expectedSeconds));
        assertThat(cache.maxAgeWithDefaultStale(maxAgeSeconds, TimeUnit.SECONDS).getHeaderValue()).isEqualTo("max-age=%d, stale-while-revalidate=10".formatted(expectedSeconds));
    }

    @ParameterizedTest
    @CsvSource("""
            10, 1, 600
            10, 2, 300
            10, 3, 200
            10, 4, 150
            10, 2000, 0
            """)
    void maxAgeMinutes(long maxAgeMinutes, long maxAgeDivisor, long expectedSeconds) {
        final var cache = new Cache(maxAgeDivisor, false, 10);
        assertThat(cache.maxAge(maxAgeMinutes, TimeUnit.MINUTES).getHeaderValue()).isEqualTo("max-age=%d".formatted(expectedSeconds));
        assertThat(cache.maxAgeWithDefaultStale(maxAgeMinutes, TimeUnit.MINUTES).getHeaderValue()).isEqualTo("max-age=%d, stale-while-revalidate=10".formatted(expectedSeconds));
    }

    @Test
    void noCache() {
        final var cache = new Cache(1L, true, 10);
        assertThat(cache.maxAge(20, TimeUnit.SECONDS).getHeaderValue()).isEqualTo("no-cache");
        assertThat(cache.maxAgeWithDefaultStale(20, TimeUnit.SECONDS).getHeaderValue()).isEqualTo("no-cache");
    }

    @Test
    void defaultStale() {
        final var cache = new Cache(1L, false, 40);
        assertThat(cache.maxAgeWithDefaultStale(20, TimeUnit.SECONDS).getHeaderValue()).isEqualTo("max-age=20, stale-while-revalidate=40");
    }
}