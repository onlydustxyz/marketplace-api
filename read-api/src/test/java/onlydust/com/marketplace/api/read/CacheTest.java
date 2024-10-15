package onlydust.com.marketplace.api.read;

import onlydust.com.marketplace.api.read.properties.Cache;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
        assertThat(cache.forEverybody(maxAgeSeconds, TimeUnit.SECONDS).getHeaderValue())
                .isEqualTo("max-age=%d, public, stale-while-revalidate=10".formatted(expectedSeconds));
        assertThat(cache.whenAnonymous(Optional.empty(), maxAgeSeconds, TimeUnit.SECONDS).getHeaderValue())
                .isEqualTo("max-age=%d, public, stale-while-revalidate=10".formatted(expectedSeconds));
        assertThat(cache.whenAnonymous(Optional.of(AuthenticatedUser.builder().build()), maxAgeSeconds, TimeUnit.SECONDS).getHeaderValue())
                .isEqualTo("max-age=%d, private".formatted(expectedSeconds));

        assertThat(cache.forEverybody(Duration.ofSeconds(maxAgeSeconds)).getHeaderValue())
                .isEqualTo("max-age=%d, public, stale-while-revalidate=10".formatted(expectedSeconds));
        assertThat(cache.whenAnonymous(Optional.empty(), Duration.ofSeconds(maxAgeSeconds)).getHeaderValue())
                .isEqualTo("max-age=%d, public, stale-while-revalidate=10".formatted(expectedSeconds));
        assertThat(cache.whenAnonymous(Optional.of(AuthenticatedUser.builder().build()), Duration.ofSeconds(maxAgeSeconds)).getHeaderValue())
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
        assertThat(cache.forEverybody(maxAgeMinutes, TimeUnit.MINUTES).getHeaderValue())
                .isEqualTo("max-age=%d, public, stale-while-revalidate=10".formatted(expectedSeconds));
        assertThat(cache.whenAnonymous(Optional.empty(), maxAgeMinutes, TimeUnit.MINUTES).getHeaderValue())
                .isEqualTo("max-age=%d, public, stale-while-revalidate=10".formatted(expectedSeconds));
        assertThat(cache.whenAnonymous(Optional.of(AuthenticatedUser.builder().build()), maxAgeMinutes, TimeUnit.MINUTES).getHeaderValue())
                .isEqualTo("max-age=%d, private".formatted(expectedSeconds));

        assertThat(cache.forEverybody(Duration.ofMinutes(maxAgeMinutes)).getHeaderValue())
                .isEqualTo("max-age=%d, public, stale-while-revalidate=10".formatted(expectedSeconds));
        assertThat(cache.whenAnonymous(Optional.empty(), Duration.ofMinutes(maxAgeMinutes)).getHeaderValue())
                .isEqualTo("max-age=%d, public, stale-while-revalidate=10".formatted(expectedSeconds));
        assertThat(cache.whenAnonymous(Optional.of(AuthenticatedUser.builder().build()), Duration.ofMinutes(maxAgeMinutes)).getHeaderValue())
                .isEqualTo("max-age=%d, private".formatted(expectedSeconds));
    }

    @Test
    void noCache() {
        final var cache = new Cache(1L, true, 10);
        assertThat(cache.forEverybody(20, TimeUnit.SECONDS).getHeaderValue()).isEqualTo("no-store");
        assertThat(cache.whenAnonymous(Optional.empty(), 20, TimeUnit.MINUTES).getHeaderValue()).isEqualTo("no-store");
        assertThat(cache.whenAnonymous(Optional.of(AuthenticatedUser.builder().build()), 20, TimeUnit.MINUTES).getHeaderValue()).isEqualTo("no-store");
    }

    @Test
    void defaultStale() {
        final var cache = new Cache(1L, false, 40);
        assertThat(cache.forEverybody(20, TimeUnit.SECONDS).getHeaderValue()).isEqualTo("max-age=20, public, stale-while-revalidate=20");
        assertThat(cache.forEverybody(40, TimeUnit.SECONDS).getHeaderValue()).isEqualTo("max-age=40, public, stale-while-revalidate=40");
        assertThat(cache.forEverybody(60, TimeUnit.SECONDS).getHeaderValue()).isEqualTo("max-age=60, public, stale-while-revalidate=40");
    }
}