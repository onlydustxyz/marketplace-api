package onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtSecret {

  String type;
  String key;
  String issuer;
}
