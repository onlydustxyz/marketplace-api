package onlydust.com.marketplace.api.read.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import org.springframework.http.CacheControl;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cache {
    private long timeDivisor = 1L;
    private boolean noCache = false;
    private long defaultStaleWhileRevalidateSeconds = 10L;

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
        return sanitize(CacheControl.maxAge(maxAge, unit).cachePrivate());
    }

    /**
     * Add 'max-age' and 'stale-while-revalidate' headers.
     *
     * @param maxAge max-age value
     * @param unit   time unit
     * @return CacheControl
     */
    public CacheControl forEverybody(long maxAge, TimeUnit unit) {
        return sanitize(CacheControl.maxAge(maxAge, unit)
                .staleWhileRevalidate(Duration.ofSeconds(defaultStaleWhileRevalidateSeconds)));
    }

    /**
     * Add 'max-age' header.
     *
     * @param maxAge max-age value
     * @param unit   time unit
     * @return CacheControl
     */
    public CacheControl maxAge(long maxAge, TimeUnit unit) {
        return sanitize(CacheControl.maxAge(maxAge, unit));
    }

    private CacheControl sanitize(CacheControl cacheControl) {
        return noCache ? CacheControl.noStore() : cacheControl;
    }
}
