package onlydust.com.marketplace.api.auth0.api.client.adapter.authentication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Auth0ManagementApiAccessTokenResponse {

  @JsonProperty("access_token")
  String accessToken;
  @JsonProperty("scope")
  String scope;
  @JsonProperty("expires_in")
  Integer expiresIn;
  @JsonProperty("token_type")
  String tokenType;
}
