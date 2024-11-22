package onlydust.com.marketplace.api.read;

import onlydust.com.marketplace.api.read.cache.Cache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class CacheTest {

    @ParameterizedTest
    @CsvSource({
            "10, 1, 10",
            "10, 2, 5",
            "10, 3, 3",
            "10, 4, 2",
            "10, 20, 0"
    })
    void seconds(long maxAgeSeconds, long maxAgeDivisor, long expectedSeconds) {
        final var cache = new Cache(maxAgeDivisor, false, 10);

        assertThat(cache.forEverybody(Duration.ofSeconds(maxAgeSeconds)).getHeaderValue())
                .isEqualTo("max-age=%d, public, stale-while-revalidate=10".formatted(expectedSeconds));
        assertThat(cache.inBrowser(Duration.ofSeconds(maxAgeSeconds)).getHeaderValue())
                .isEqualTo("max-age=%d, private".formatted(expectedSeconds));
    }

    @ParameterizedTest
    @CsvSource({
            "10, 1, 600",
            "10, 2, 300",
            "10, 3, 200",
            "10, 4, 150",
            "10, 2000, 0"
    })
    void minutes(long maxAgeMinutes, long maxAgeDivisor, long expectedSeconds) {
        final var cache = new Cache(maxAgeDivisor, false, 10);

        assertThat(cache.forEverybody(Duration.ofMinutes(maxAgeMinutes)).getHeaderValue())
                .isEqualTo("max-age=%d, public, stale-while-revalidate=10".formatted(expectedSeconds));
        assertThat(cache.inBrowser(Duration.ofMinutes(maxAgeMinutes)).getHeaderValue())
                .isEqualTo("max-age=%d, private".formatted(expectedSeconds));
    }

    @Test
    void zeroDuration() {
        final var cache = new Cache(1L, false, 10);
        assertThat(cache.forEverybody(Duration.ZERO).getHeaderValue()).isEqualTo("no-store");
        assertThat(cache.inBrowser(Duration.ZERO).getHeaderValue()).isEqualTo("no-store");
    }

    @Test
    void noCache() {
        final var cache = new Cache(1L, true, 10);
        assertThat(cache.forEverybody(Duration.ofSeconds(20)).getHeaderValue()).isEqualTo("no-store");
        assertThat(cache.inBrowser(Duration.ofMinutes(20)).getHeaderValue()).isEqualTo("no-store");
    }

    @Test
    void defaultStale() {
        final var cache = new Cache(1L, false, 40);
        assertThat(cache.forEverybody(Duration.ofSeconds(20)).getHeaderValue()).isEqualTo("max-age=20, public, stale-while-revalidate=20");
        assertThat(cache.forEverybody(Duration.ofSeconds(40)).getHeaderValue()).isEqualTo("max-age=40, public, stale-while-revalidate=40");
        assertThat(cache.forEverybody(Duration.ofSeconds(60)).getHeaderValue()).isEqualTo("max-age=60, public, stale-while-revalidate=40");
    }
}