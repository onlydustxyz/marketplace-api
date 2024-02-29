package onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Auth0JwtClaims {
    @JsonProperty("nickname")
    String nickname;
    @JsonProperty("sub")
    String sub;
    @JsonProperty("picture")
    String picture;
    @JsonProperty("email")
    String email;
    @JsonProperty("name")
    String name;

    public enum Connection {
        GITHUB, GOOGLE
    }

    public Connection connection() {
        if (sub.startsWith("github")) {
            return Connection.GITHUB;
        } else if (sub.startsWith("google-oauth2")) {
            return Connection.GOOGLE;
        }
        throw OnlyDustException.unauthorized("Unknown connection type: " + sub);
    }
}
