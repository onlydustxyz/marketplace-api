package onlydust.com.marketplace.project.domain.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class OldCompanyOldBillingProfileTest {

    @Test
    void mandate_is_accepted() {
        var companyBillingProfile = OldCompanyBillingProfile.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .status(OldVerificationStatus.NOT_STARTED)
                .build();
        assertThat(companyBillingProfile.isInvoiceMandateAccepted()).isFalse();

        companyBillingProfile = OldCompanyBillingProfile.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .status(OldVerificationStatus.NOT_STARTED)
                .invoiceMandateAcceptedAt(ZonedDateTime.now())
                .invoiceMandateLatestVersionDate(ZonedDateTime.now().minusDays(1))
                .build();
        assertThat(companyBillingProfile.isInvoiceMandateAccepted()).isTrue();

        companyBillingProfile = OldCompanyBillingProfile.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .status(OldVerificationStatus.NOT_STARTED)
                .invoiceMandateAcceptedAt(ZonedDateTime.now())
                .invoiceMandateLatestVersionDate(ZonedDateTime.now().plusDays(1))
                .build();
        assertThat(companyBillingProfile.isInvoiceMandateAccepted()).isFalse();
    }

    @Test
    void should_update_status_given_no_children() {
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.STARTED, List.of(), OldVerificationStatus.STARTED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.UNDER_REVIEW, List.of(), OldVerificationStatus.UNDER_REVIEW);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.REJECTED, List.of(), OldVerificationStatus.REJECTED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.CLOSED, List.of(), OldVerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.VERIFIED, List.of(), OldVerificationStatus.VERIFIED);
    }

    @Test
    void should_update_status_from_children_statuses_given_one_children() {
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.STARTED, List.of(OldVerificationStatus.STARTED), OldVerificationStatus.STARTED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.STARTED, List.of(OldVerificationStatus.UNDER_REVIEW), OldVerificationStatus.STARTED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.STARTED, List.of(OldVerificationStatus.REJECTED), OldVerificationStatus.REJECTED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.STARTED, List.of(OldVerificationStatus.CLOSED), OldVerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.STARTED, List.of(OldVerificationStatus.VERIFIED), OldVerificationStatus.STARTED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.UNDER_REVIEW, List.of(OldVerificationStatus.STARTED), OldVerificationStatus.STARTED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.UNDER_REVIEW, List.of(OldVerificationStatus.UNDER_REVIEW), OldVerificationStatus.UNDER_REVIEW);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.UNDER_REVIEW, List.of(OldVerificationStatus.REJECTED), OldVerificationStatus.REJECTED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.UNDER_REVIEW, List.of(OldVerificationStatus.CLOSED), OldVerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.UNDER_REVIEW, List.of(OldVerificationStatus.VERIFIED), OldVerificationStatus.UNDER_REVIEW);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.REJECTED, List.of(OldVerificationStatus.STARTED), OldVerificationStatus.REJECTED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.REJECTED, List.of(OldVerificationStatus.UNDER_REVIEW), OldVerificationStatus.REJECTED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.REJECTED, List.of(OldVerificationStatus.REJECTED), OldVerificationStatus.REJECTED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.REJECTED, List.of(OldVerificationStatus.CLOSED), OldVerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.REJECTED, List.of(OldVerificationStatus.VERIFIED), OldVerificationStatus.REJECTED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.CLOSED, List.of(OldVerificationStatus.STARTED), OldVerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.CLOSED, List.of(OldVerificationStatus.UNDER_REVIEW), OldVerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.CLOSED, List.of(OldVerificationStatus.REJECTED), OldVerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.CLOSED, List.of(OldVerificationStatus.CLOSED), OldVerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.CLOSED, List.of(OldVerificationStatus.VERIFIED), OldVerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.VERIFIED, List.of(OldVerificationStatus.STARTED), OldVerificationStatus.STARTED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.VERIFIED, List.of(OldVerificationStatus.UNDER_REVIEW), OldVerificationStatus.UNDER_REVIEW);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.VERIFIED, List.of(OldVerificationStatus.REJECTED), OldVerificationStatus.REJECTED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.VERIFIED, List.of(OldVerificationStatus.CLOSED), OldVerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.VERIFIED, List.of(OldVerificationStatus.VERIFIED), OldVerificationStatus.VERIFIED);
    }

    @Test
    void should_update_status_from_children_statuses_given_two_children() {
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.STARTED, List.of(OldVerificationStatus.NOT_STARTED, OldVerificationStatus.STARTED),
                OldVerificationStatus.NOT_STARTED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.STARTED, List.of(OldVerificationStatus.UNDER_REVIEW, OldVerificationStatus.STARTED),
                OldVerificationStatus.STARTED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.STARTED, List.of(OldVerificationStatus.REJECTED, OldVerificationStatus.UNDER_REVIEW),
                OldVerificationStatus.REJECTED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.STARTED, List.of(OldVerificationStatus.CLOSED, OldVerificationStatus.REJECTED), OldVerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.STARTED, List.of(OldVerificationStatus.VERIFIED, OldVerificationStatus.UNDER_REVIEW),
                OldVerificationStatus.STARTED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.UNDER_REVIEW, List.of(OldVerificationStatus.STARTED, OldVerificationStatus.STARTED),
                OldVerificationStatus.STARTED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.UNDER_REVIEW, List.of(OldVerificationStatus.NOT_STARTED, OldVerificationStatus.STARTED),
                OldVerificationStatus.NOT_STARTED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.UNDER_REVIEW, List.of(OldVerificationStatus.REJECTED, OldVerificationStatus.UNDER_REVIEW),
                OldVerificationStatus.REJECTED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.UNDER_REVIEW, List.of(OldVerificationStatus.CLOSED, OldVerificationStatus.UNDER_REVIEW),
                OldVerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.UNDER_REVIEW, List.of(OldVerificationStatus.VERIFIED, OldVerificationStatus.UNDER_REVIEW),
                OldVerificationStatus.UNDER_REVIEW);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.REJECTED, List.of(OldVerificationStatus.STARTED, OldVerificationStatus.UNDER_REVIEW),
                OldVerificationStatus.REJECTED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.REJECTED, List.of(OldVerificationStatus.UNDER_REVIEW, OldVerificationStatus.UNDER_REVIEW),
                OldVerificationStatus.REJECTED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.REJECTED, List.of(OldVerificationStatus.REJECTED, OldVerificationStatus.UNDER_REVIEW),
                OldVerificationStatus.REJECTED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.REJECTED, List.of(OldVerificationStatus.CLOSED, OldVerificationStatus.STARTED), OldVerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.REJECTED, List.of(OldVerificationStatus.VERIFIED, OldVerificationStatus.UNDER_REVIEW),
                OldVerificationStatus.REJECTED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.CLOSED, List.of(OldVerificationStatus.STARTED, OldVerificationStatus.UNDER_REVIEW),
                OldVerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.CLOSED, List.of(OldVerificationStatus.UNDER_REVIEW, OldVerificationStatus.REJECTED),
                OldVerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.CLOSED, List.of(OldVerificationStatus.REJECTED, OldVerificationStatus.STARTED), OldVerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.CLOSED, List.of(OldVerificationStatus.CLOSED, OldVerificationStatus.VERIFIED), OldVerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.CLOSED, List.of(OldVerificationStatus.VERIFIED, OldVerificationStatus.STARTED), OldVerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.VERIFIED, List.of(OldVerificationStatus.STARTED, OldVerificationStatus.VERIFIED),
                OldVerificationStatus.STARTED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.VERIFIED, List.of(OldVerificationStatus.UNDER_REVIEW, OldVerificationStatus.VERIFIED),
                OldVerificationStatus.UNDER_REVIEW);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.VERIFIED, List.of(OldVerificationStatus.REJECTED, OldVerificationStatus.STARTED),
                OldVerificationStatus.REJECTED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.VERIFIED, List.of(OldVerificationStatus.CLOSED, OldVerificationStatus.STARTED), OldVerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(OldVerificationStatus.VERIFIED, List.of(OldVerificationStatus.VERIFIED, OldVerificationStatus.VERIFIED),
                OldVerificationStatus.VERIFIED);
    }


    public void assertUpdatedStatusIsEqualsTo(final OldVerificationStatus parentStatus, final List<OldVerificationStatus> childrenStatuses,
                                              final OldVerificationStatus expectedOldVerificationStatus) {
        Assertions.assertEquals(expectedOldVerificationStatus, OldCompanyBillingProfile.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .status(parentStatus)
                .build().updateStatusFromNewChildrenStatuses(childrenStatuses).getStatus());
    }
}
