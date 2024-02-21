package onlydust.com.marketplace.project.domain.model;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OldIndividualOldBillingProfileTest {

    @Test
    void mandate_is_accepted() {
        var individualBillingProfile = OldIndividualBillingProfile.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .status(OldVerificationStatus.NOT_STARTED)
                .build();
        assertThat(individualBillingProfile.isInvoiceMandateAccepted()).isFalse();

        individualBillingProfile = OldIndividualBillingProfile.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .status(OldVerificationStatus.NOT_STARTED)
                .invoiceMandateAcceptedAt(ZonedDateTime.now())
                .invoiceMandateLatestVersionDate(ZonedDateTime.now().minusDays(1))
                .build();
        assertThat(individualBillingProfile.isInvoiceMandateAccepted()).isTrue();

        individualBillingProfile = OldIndividualBillingProfile.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .status(OldVerificationStatus.NOT_STARTED)
                .invoiceMandateAcceptedAt(ZonedDateTime.now())
                .invoiceMandateLatestVersionDate(ZonedDateTime.now().plusDays(1))
                .build();
        assertThat(individualBillingProfile.isInvoiceMandateAccepted()).isFalse();
    }

}