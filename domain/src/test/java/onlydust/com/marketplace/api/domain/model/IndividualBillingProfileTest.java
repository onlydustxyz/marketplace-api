package onlydust.com.marketplace.api.domain.model;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class IndividualBillingProfileTest {

    @Test
    void mandate_is_accepted() {
        var individualBillingProfile = IndividualBillingProfile.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .status(VerificationStatus.NOT_STARTED)
                .build();
        assertThat(individualBillingProfile.isInvoiceMandateAccepted()).isFalse();

        individualBillingProfile = IndividualBillingProfile.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .status(VerificationStatus.NOT_STARTED)
                .invoiceMandateAcceptedAt(ZonedDateTime.now())
                .invoiceMandateLatestVersionDate(ZonedDateTime.now().minusDays(1))
                .build();
        assertThat(individualBillingProfile.isInvoiceMandateAccepted()).isTrue();

        individualBillingProfile = IndividualBillingProfile.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .status(VerificationStatus.NOT_STARTED)
                .invoiceMandateAcceptedAt(ZonedDateTime.now())
                .invoiceMandateLatestVersionDate(ZonedDateTime.now().plusDays(1))
                .build();
        assertThat(individualBillingProfile.isInvoiceMandateAccepted()).isFalse();
    }

}