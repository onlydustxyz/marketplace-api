package onlydust.com.marketplace.api.auth0.api.client.adapter.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Auth0ManagementApiAccessTokenRequest {

  @JsonProperty("grant_type")
  String grantType;
  @JsonProperty("client_id")
  String clientId;
  @JsonProperty("client_secret")
  String clientSecret;
  @JsonProperty("audience")
  String audience;
}
