package onlydust.com.marketplace.api.rest.api.adapter.authentication;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.NonNull;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0.Auth0Properties;

import java.util.concurrent.TimeUnit;

public class CachedClaimsProvider<T> implements ClaimsProvider<T> {
    private final @NonNull ClaimsProvider<T> claimsProvider;
    private final @NonNull Cache<String, T> cache;

    public CachedClaimsProvider(final @NonNull ClaimsProvider<T> claimsProvider,
                                final @NonNull Auth0Properties properties) {
        this.claimsProvider = claimsProvider;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(properties.getAccessTokenCacheTtlInSeconds(), TimeUnit.SECONDS)
                .build();
    }

    @Override
    public T getClaimsFromAccessToken(@NonNull String accessToken) {
        return cache.get(accessToken, claimsProvider::getClaimsFromAccessToken);
    }
}
