package onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HasuraJwtPayload {
    String sub;
    String iss;
    Date iat;
    Date exp;
    @JsonProperty("https://hasura.io/jwt/claims")
    HasuraClaims claims;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HasuraClaims {
        @JsonProperty("x-hasura-githubAccessToken")
        String githubAccessToken;
        @JsonProperty("x-hasura-allowed-roles")
        List<String> allowedRoles;
        @JsonProperty("x-hasura-githubUserId")
        Long githubUserId;
        @JsonProperty("x-hasura-odAdmin")
        Boolean isAnOnlydustAdmin;
        @JsonProperty("x-hasura-user-id")
        UUID userId;
        @JsonProperty("x-hasura-user-is-anonymous")
        Boolean isAnonymous;
        @JsonProperty("x-hasura-projectsLeaded")
        String projectsLeaded;
        @JsonProperty("x-hasura-default-role")
        String defaultRole;
        @JsonProperty("x-hasura-login")
        String login;
        @JsonProperty("x-hasura-avatarUrl")
        String avatarUrl;
    }

}
