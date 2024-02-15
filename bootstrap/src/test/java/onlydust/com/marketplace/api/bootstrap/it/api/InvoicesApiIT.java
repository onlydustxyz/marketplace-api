package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.contract.model.BillingProfileType;
import onlydust.com.marketplace.api.contract.model.CurrencyContract;
import onlydust.com.marketplace.api.contract.model.MyBillingProfilesResponse;
import onlydust.com.marketplace.api.contract.model.NewInvoiceResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Map;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

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
        final var response = client.get()
                .uri(getApiURI(BILLING_PROFILE_INVOICE_PREVIEW.formatted(billingProfileId), Map.of(
                        "pageIndex", "0",
                        "pageSize", "10",
                        "rewardIds", "ee28315c-7a84-4052-9308-c2236eeafda1,79209029-c488-4284-aa3f-bce8870d3a66,966cd55c-7de8-45c4-8bba-b388c38ca15d"
                )))
                .header("Authorization", BEARER_PREFIX + antho.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(NewInvoiceResponse.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("OD-BUISSET-ANTHONY-001");
        assertThat(response.getCreatedAt()).isBefore(ZonedDateTime.now());
        assertThat(response.getDueAt()).isAfter(ZonedDateTime.now());
        assertThat(response.getBillingProfileType()).isEqualTo(BillingProfileType.INDIVIDUAL);
        assertThat(response.getIndividualBillingProfile().getFirstName()).isEqualTo("Anthony");
        assertThat(response.getIndividualBillingProfile().getLastName()).isEqualTo("BUISSET");
        assertThat(response.getIndividualBillingProfile().getAddress()).isEqualTo("771 chemin de la sine, 06140, Vence, France");
        assertThat(response.getCompanyBillingProfile()).isNull();
        assertThat(response.getDestinationAccounts()).isNull();
        assertThat(response.getRewards()).hasSize(3);
        assertThat(response.getRewards()).allMatch(r -> r.getId() != null);
        assertThat(response.getRewards()).allMatch(r -> r.getDate() != null);
        assertThat(response.getRewards()).allMatch(r -> r.getProjectName() != null);
        assertThat(response.getRewards()).allMatch(r -> r.getAmount().getAmount().compareTo(BigDecimal.ZERO) > 0);
        assertThat(response.getRewards()).allMatch(r -> r.getAmount().getCurrency() != null);
        assertThat(response.getRewards()).allMatch(r -> r.getAmount().getBase().getAmount().compareTo(BigDecimal.ZERO) > 0);
        assertThat(response.getRewards()).allMatch(r -> r.getAmount().getBase().getCurrency() == CurrencyContract.USD);
        assertThat(response.getRewards()).allMatch(r -> r.getAmount().getBase().getConversionRate().compareTo(BigDecimal.ZERO) >= 0);
        assertThat(response.getTotalBeforeTax().getAmount().equals(BigDecimal.valueOf(1784757.5)));
        assertThat(response.getTotalBeforeTax().getCurrency()).isEqualTo(CurrencyContract.USD);
        assertThat(response.getTaxRate()).isEqualTo(BigDecimal.ZERO);
        assertThat(response.getTotalTax().getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getTotalTax().getCurrency()).isEqualTo(CurrencyContract.USD);
        assertThat(response.getTotalAfterTax().getAmount()).isEqualTo(response.getTotalBeforeTax().getAmount());
        assertThat(response.getTotalAfterTax().getCurrency()).isEqualTo(CurrencyContract.USD);

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
