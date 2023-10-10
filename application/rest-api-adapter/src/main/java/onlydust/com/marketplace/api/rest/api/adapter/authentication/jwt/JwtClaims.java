package onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtClaims {
    String githubLogin;
    Long githubUserId;
    
    Boolean isAnOnlydustAdmin;
    UUID userId;
    String projectsLeaded;
}
