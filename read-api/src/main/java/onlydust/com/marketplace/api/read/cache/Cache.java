package onlydust.com.marketplace.api.read.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.CacheControl;

import java.time.Duration;

import static java.lang.Math.min;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cache {
    public static final Duration ZERO = Duration.ZERO;
    public static final Duration XS = Duration.ofSeconds(1);
    public static final Duration S = Duration.ofSeconds(10);
    public static final Duration M = Duration.ofMinutes(5);
    public static final Duration L = Duration.ofMinutes(20);
    public static final Duration XL = Duration.ofHours(1);
    private long timeDivisor = 1L;
    private boolean noCache = false;
    private long defaultStaleWhileRevalidateSeconds = 10L;

    /**
     * Add 'max-age' and 'private' headers.
     * The 'private' header indicates that all or part of the response message is intended for a single user
     * and MUST NOT be cached by a shared cache. Hence, it will be cached by the browser only.
     *
     * @param duration max-age duration for the browser cache
     * @return CacheControl
     */
    public CacheControl inBrowser(Duration duration) {
        if (duration.isZero()) {
            return CacheControl.noStore();
        }
        return sanitize(maxAge(duration).cachePrivate());
    }

    /**
     * Add 'max-age' and 'stale-while-revalidate' headers.
     *
     * @param duration max-age duration
     * @return CacheControl
     */
    public CacheControl forEverybody(Duration duration) {
        if (duration.isZero()) {
            return CacheControl.noStore();
        }
        return sanitize(maxAge(duration)
                .cachePublic()
                .staleWhileRevalidate(Duration.ofSeconds(min(defaultStaleWhileRevalidateSeconds, duration.getSeconds()))));
    }

    private CacheControl maxAge(Duration duration) {
        return CacheControl.maxAge(duration.dividedBy(timeDivisor));
    }

    private CacheControl sanitize(CacheControl cacheControl) {
        return noCache ? CacheControl.noStore() : cacheControl;
    }
}
