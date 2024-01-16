package onlydust.com.marketplace.api.rest.api.adapter.authentication;

import lombok.NonNull;

public interface ClaimsProvider<T> {
    T getClaimsFromAccessToken(final @NonNull String accessToken);
}
