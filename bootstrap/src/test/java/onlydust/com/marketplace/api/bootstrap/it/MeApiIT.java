package onlydust.com.marketplace.api.bootstrap.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import onlydust.com.marketplace.api.bootstrap.helper.HasuraJwtHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.AuthUserEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.AuthUserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.UserPayoutInfoRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura.HasuraJwtPayload;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt.JwtSecret;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

@ActiveProfiles({"hasura_auth"})
public class MeApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    AuthUserRepository authUserRepository;
    @Autowired
    JwtSecret jwtSecret;
    @Autowired
    UserPayoutInfoRepository userPayoutInfoRepository;

    @Test
    void should_get_user_payout_info() throws JsonProcessingException {
        // Given
        final AuthUserEntity anthony = authUserRepository.findByGithubUserId(43467246L).orElseThrow();
        final String jwt = HasuraJwtHelper.generateValidJwtFor(jwtSecret, HasuraJwtPayload.builder()
                .iss(jwtSecret.getIssuer())
                .claims(HasuraJwtPayload.HasuraClaims.builder()
                        .userId(anthony.getId())
                        .allowedRoles(List.of("me"))
                        .githubUserId(anthony.getGithubUserId())
                        .avatarUrl(anthony.getAvatarUrlAtSignup())
                        .login(anthony.getLoginAtSignup())
                        .build())
                .build());

        // When
        client.get()
                .uri(getApiURI(ME_GET_PAYOUT_INFO))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.isCompany").isEqualTo(false)
                .jsonPath("$.person.lastname").isEqualTo("BUISSET")
                .jsonPath("$.person.firstname").isEqualTo("Anthony")
                .jsonPath("$.location.address").isEqualTo("771 chemin de la sine")
                .jsonPath("$.location.city").isEqualTo("Vence")
                .jsonPath("$.location.country").isEqualTo("France")
                .jsonPath("$.location.postalCode").isEqualTo("06140")
                .jsonPath("$.payoutSettings.ethName").isEqualTo("abuisset.eth")
                .jsonPath("$.payoutSettings.usdPreferredMethod").isEqualTo("USDC");
    }
}
