package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.accounting.domain.service.BillingProfileService;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.contract.model.BillingProfileResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.UUID;

public class BillingProfileEnableApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    BillingProfileService billingProfileService;

    @Test
    void should_disable_billing_profile() {
        // Given
        final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.newFakeUser(UUID.randomUUID(),
                faker.number().randomNumber() + faker.number().randomNumber(), faker.name().name(),
                faker.internet().url(), false);
        final String jwt =
                authenticatedUser.jwt();


        // When
        final BillingProfileResponse billingProfileResponse = client.post()
                .uri(getApiURI(BILLING_PROFILES_POST))
                .header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "company",
                          "type": "COMPANY"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(BillingProfileResponse.class).returnResult().getResponseBody();
        Assertions.assertTrue(billingProfileResponse.getEnabled());

        // Then
        client.put()
                .uri(getApiURI(BILLING_PROFILES_ENABLE_BY_ID.formatted(billingProfileResponse.getId())))
                .header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "enable": false
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // When
        client.get()
                .uri(ME_BILLING_PROFILES)
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.billingProfiles[0].enabled").isEqualTo(false);

        // When
        client.get()
                .uri(BILLING_PROFILES_GET_BY_ID.formatted(billingProfileResponse.getId()))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.enabled").isEqualTo(false);
    }
}
