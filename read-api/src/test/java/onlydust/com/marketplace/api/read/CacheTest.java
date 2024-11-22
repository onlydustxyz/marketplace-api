package onlydust.com.marketplace.api.read;

import onlydust.com.marketplace.api.read.cache.Cache;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CacheTest {

    @ParameterizedTest
    @CsvSource({
            "10, 15, 1, 10, 15",
            "10, 15, 2, 5, 7",
            "10, 15, 3, 3, 5",
            "10, 15, 4, 2, 3",
            "10, 15, 20, 0,  0"
    })
    void seconds(long maxAgeSeconds, long privateMaxAgeSeconds, long maxAgeDivisor, long expectedSeconds, long expectedSecondsPrivate) {
        final var cache = new Cache(maxAgeDivisor, false, 10);

        assertThat(cache.forEverybody(Duration.ofSeconds(maxAgeSeconds)).getHeaderValue())
                .isEqualTo("max-age=%d, public, stale-while-revalidate=10".formatted(expectedSeconds));
        assertThat(cache.whenAnonymous(Optional.empty(), Duration.ofSeconds(maxAgeSeconds), Duration.ofSeconds(privateMaxAgeSeconds)).getHeaderValue())
                .isEqualTo("max-age=%d, stale-while-revalidate=10".formatted(expectedSeconds));
        assertThat(cache.whenAnonymous(Optional.of(AuthenticatedUser.builder().build()), Duration.ofSeconds(maxAgeSeconds),
                Duration.ofSeconds(privateMaxAgeSeconds)).getHeaderValue())
                .isEqualTo("max-age=%d, private".formatted(expectedSecondsPrivate));
    }

    @ParameterizedTest
    @CsvSource({
            "10, 15, 1, 600, 900",
            "10, 15, 2, 300, 450",
            "10, 15, 3, 200, 300",
            "10, 15, 4, 150, 225",
            "10, 15, 2000, 0, 0"
    })
    void minutes(long maxAgeMinutes, long privateMaxAgeMinutes, long maxAgeDivisor, long expectedSeconds, long expectedSecondsPrivate) {
        final var cache = new Cache(maxAgeDivisor, false, 10);

        assertThat(cache.forEverybody(Duration.ofMinutes(maxAgeMinutes)).getHeaderValue())
                .isEqualTo("max-age=%d, public, stale-while-revalidate=10".formatted(expectedSeconds));
        assertThat(cache.whenAnonymous(Optional.empty(), Duration.ofMinutes(maxAgeMinutes), Duration.ofMinutes(privateMaxAgeMinutes)).getHeaderValue())
                .isEqualTo("max-age=%d, stale-while-revalidate=10".formatted(expectedSeconds));
        assertThat(cache.whenAnonymous(Optional.of(AuthenticatedUser.builder().build()), Duration.ofMinutes(maxAgeMinutes),
                Duration.ofMinutes(privateMaxAgeMinutes)).getHeaderValue())
                .isEqualTo("max-age=%d, private".formatted(expectedSecondsPrivate));
    }

    @Test
    void zeroDuration() {
        final var cache = new Cache(1L, false, 10);
        assertThat(cache.forEverybody(Duration.ZERO).getHeaderValue()).isEqualTo("no-store");
        assertThat(cache.whenAnonymous(Optional.empty(), Duration.ZERO, Duration.ofMinutes(20)).getHeaderValue()).isEqualTo("no-store");
        assertThat(cache.whenAnonymous(Optional.of(AuthenticatedUser.builder().build()), Duration.ofMinutes(20), Duration.ZERO).getHeaderValue()).isEqualTo(
                "no-store");
    }

    @Test
    void noCache() {
        final var cache = new Cache(1L, true, 10);
        assertThat(cache.forEverybody(Duration.ofSeconds(20)).getHeaderValue()).isEqualTo("no-store");
        assertThat(cache.whenAnonymous(Optional.empty(), Duration.ofMinutes(20), Duration.ofMinutes(20)).getHeaderValue()).isEqualTo("no-store");
        assertThat(cache.whenAnonymous(Optional.of(AuthenticatedUser.builder().build()), Duration.ofMinutes(20), Duration.ofMinutes(20)).getHeaderValue()).isEqualTo("no-store");
    }

    @Test
    void defaultStale() {
        final var cache = new Cache(1L, false, 40);
        assertThat(cache.forEverybody(Duration.ofSeconds(20)).getHeaderValue()).isEqualTo("max-age=20, public, stale-while-revalidate=20");
        assertThat(cache.forEverybody(Duration.ofSeconds(40)).getHeaderValue()).isEqualTo("max-age=40, public, stale-while-revalidate=40");
        assertThat(cache.forEverybody(Duration.ofSeconds(60)).getHeaderValue()).isEqualTo("max-age=60, public, stale-while-revalidate=40");
    }
}