package onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Auth0Properties {
    String jwksUrl;
    Long expiresAtLeeway;
    Long accessTokenCacheTtlInSeconds;
}
