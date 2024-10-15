package onlydust.com.marketplace.api.read.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import org.springframework.http.CacheControl;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.min;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cache {
    private long timeDivisor = 1L;
    private boolean noCache = false;
    private long defaultStaleWhileRevalidateSeconds = 10L;

    public static final Duration XS = Duration.ofSeconds(5);
    public static final Duration S = Duration.ofSeconds(30);
    public static final Duration M = Duration.ofMinutes(5);
    public static final Duration L = Duration.ofMinutes(20);
    public static final Duration XL = Duration.ofHours(1);

    /**
     * Add 'max-age' and 'stale-while-revalidate' headers when the user is anonymous, otherwise add 'max-age' and 'private' headers.
     * The 'private' header indicates that all or part of the response message is intended for a single user
     * and MUST NOT be cached by a shared cache.
     *
     * @param user   authenticated user (empty if anonymous)
     * @param maxAge max-age value
     * @param unit   time unit
     * @return CacheControl
     */
    public CacheControl whenAnonymous(Optional<AuthenticatedUser> user, long maxAge, TimeUnit unit) {
        if (user.isEmpty()) {
            return forEverybody(maxAge, unit);
        }
        return sanitize(maxAge(maxAge, unit).cachePrivate());
    }

    /**
     * Add 'max-age' and 'stale-while-revalidate' headers when the user is anonymous, otherwise add 'max-age' and 'private' headers.
     * The 'private' header indicates that all or part of the response message is intended for a single user
     * and MUST NOT be cached by a shared cache.
     *
     * @param user     authenticated user (empty if anonymous)
     * @param duration max-age duration
     * @return CacheControl
     */
    public CacheControl whenAnonymous(Optional<AuthenticatedUser> user, Duration duration) {
        if (user.isEmpty()) {
            return forEverybody(duration);
        }
        return sanitize(maxAge(duration).cachePrivate());
    }

    /**
     * Add 'max-age' and 'stale-while-revalidate' headers.
     *
     * @param maxAge max-age value
     * @param unit   time unit
     * @return CacheControl
     */
    public CacheControl forEverybody(long maxAge, TimeUnit unit) {
        return sanitize(maxAge(maxAge, unit)
                .cachePublic()
                .staleWhileRevalidate(Duration.ofSeconds(min(defaultStaleWhileRevalidateSeconds, unit.toSeconds(maxAge)))));
    }

    /**
     * Add 'max-age' and 'stale-while-revalidate' headers.
     *
     * @param duration max-age duration
     * @return CacheControl
     */
    public CacheControl forEverybody(Duration duration) {
        return sanitize(maxAge(duration)
                .cachePublic()
                .staleWhileRevalidate(Duration.ofSeconds(min(defaultStaleWhileRevalidateSeconds, duration.getSeconds()))));
    }

    private CacheControl maxAge(long maxAge, TimeUnit unit) {
        return maxAge(Duration.ofSeconds(unit.toSeconds(maxAge)));
    }

    private CacheControl maxAge(Duration duration) {
        return CacheControl.maxAge(duration.dividedBy(timeDivisor));
    }

    private CacheControl sanitize(CacheControl cacheControl) {
        return noCache ? CacheControl.noStore() : cacheControl;
    }
}
