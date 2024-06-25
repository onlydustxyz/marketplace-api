package onlydust.com.marketplace.api.auth0.api.client.adapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Auth0UserResponse {
    @JsonProperty("identities")
    List<Identity> identities;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Identity {
        @JsonProperty("access_token")
        String accessToken;
        @JsonProperty("provider")
        String provider;
    }
}
