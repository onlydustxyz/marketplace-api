package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.accounting.domain.service.BillingProfileService;
import onlydust.com.marketplace.api.contract.model.BillingProfileCreateResponse;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagAccounting;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

@TagAccounting
public class BillingProfileEnableApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    BillingProfileService billingProfileService;

    @Test
    void should_disable_billing_profile() {
        // Given
        final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.signUpUser(
                faker.number().randomNumber() + faker.number().randomNumber(), faker.name().name(),
                faker.internet().url(), false);
        final String jwt =
                authenticatedUser.jwt();


        // When
        final BillingProfileCreateResponse billingProfileResponse = client.post()
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
                .expectBody(BillingProfileCreateResponse.class).returnResult().getResponseBody();

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
