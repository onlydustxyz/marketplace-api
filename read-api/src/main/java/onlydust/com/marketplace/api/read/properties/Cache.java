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

    public CacheControl whenAnonymous(Optional<AuthenticatedUser> user, long maxAge, TimeUnit unit) {
        if (user.isEmpty()) {
            return forEverybody(maxAge, unit);
        }
        return CacheControl.empty().cachePrivate();
    }

    public CacheControl forEverybody(long maxAge, TimeUnit unit) {
        return sanitize(CacheControl.maxAge(maxAge, unit)
                .staleWhileRevalidate(Duration.ofSeconds(defaultStaleWhileRevalidateSeconds)));
    }

    public CacheControl maxAge(long maxAge, TimeUnit unit) {
        return sanitize(CacheControl.maxAge(maxAge, unit));
    }

    private CacheControl sanitize(CacheControl cacheControl) {
        return noCache ? CacheControl.noStore() : cacheControl;
    }
}
