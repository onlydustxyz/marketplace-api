package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.contract.model.MyBillingProfilesResponse;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

public class InvoicesApiIT extends AbstractMarketplaceApiIT {

    @Test
    void should_upload_and_list_invoices() {
        // Given
        final var antho = userAuthHelper.authenticateAnthony();

        final var myBillingProfiles = client.get()
                .uri(ME_BILLING_PROFILES)
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MyBillingProfilesResponse.class)
                .returnResult()
                .getResponseBody();

        final var billingProfileId = myBillingProfiles.getBillingProfiles().get(0).getId();

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILE_INVOICES_PREVIEW.formatted(billingProfileId), Map.of(
                        "pageIndex", "0",
                        "pageSize", "10"
                )))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
        ;

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILE_INVOICES.formatted(billingProfileId), Map.of(
                        "pageIndex", "0",
                        "pageSize", "10"
                )))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.invoices.length()").isEqualTo(0)
                .jsonPath("$.hasMore").isEqualTo(false)
                .jsonPath("$.totalPageNumber").isEqualTo(0)
                .jsonPath("$.totalItemNumber").isEqualTo(0)
                .jsonPath("$.nextPageIndex").isEqualTo(0)
        ;
    }
}
