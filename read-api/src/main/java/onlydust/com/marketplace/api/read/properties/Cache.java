package onlydust.com.marketplace.api.read.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.CacheControl;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cache {
    private long timeDivisor = 1L;
    private boolean noCache = false;
    private long defaultStaleWhileRevalidateSeconds = 10L;

    public CacheControl maxAgeWithDefaultStale(long maxAge, TimeUnit unit) {
        return sanitize(CacheControl.maxAge(maxAge, unit)
                .staleWhileRevalidate(Duration.ofSeconds(defaultStaleWhileRevalidateSeconds)));
    }

    public CacheControl maxAge(long maxAge, TimeUnit unit) {
        return sanitize(CacheControl.maxAge(Duration.ofSeconds(unit.toSeconds(maxAge))));
    }

    private CacheControl sanitize(CacheControl cacheControl) {
        return noCache ? CacheControl.noCache() : cacheControl;
    }
}
