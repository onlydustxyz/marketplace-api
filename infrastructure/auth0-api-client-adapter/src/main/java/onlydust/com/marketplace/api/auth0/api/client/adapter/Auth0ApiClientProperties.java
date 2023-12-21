package onlydust.com.marketplace.api.auth0.api.client.adapter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auth0ApiClientProperties {
    String domainBaseUri;
    String clientId;
    String clientSecret;
    Integer patCacheTtlInSeconds = 3600;
}
