package onlydust.com.marketplace.api.bootstrap.it.api;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;


public class SponsorsGetApiIT extends AbstractMarketplaceApiIT {
    private final static SponsorId COCA_COLAX = SponsorId.of("44c6807c-48d1-4987-a0a6-ac63f958bdae");
    private UserAuthHelper.AuthenticatedUser user;

    @BeforeEach
    void setup() {
        user = userAuthHelper.authenticateAnthony();
    }

    @Test
    void should_return_forbidden_if_not_admin() {
        getSponsor(COCA_COLAX)
                .expectStatus()
                .isForbidden();
    }

    @Test
    void should_return_sponsor_by_id() {
        // Given
        addSponsorFor(user, COCA_COLAX);

        // When
        getSponsor(COCA_COLAX)
                .expectStatus()
                .isOk();
    }

    @NonNull
    private WebTestClient.ResponseSpec getSponsor(SponsorId id) {
        return client.get()
                .uri(SPONSOR.formatted(id))
                .header("Authorization", "Bearer " + user.jwt())
                .exchange();
    }
}
