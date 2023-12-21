package onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Auth0JwtClaims {
    @JsonProperty("nickname")
    String githubLogin;
    @JsonProperty("sub")
    String githubWithUserId;
    @JsonProperty("picture")
    String githubAvatarUrl;
    @JsonProperty("email")
    String email;
}
