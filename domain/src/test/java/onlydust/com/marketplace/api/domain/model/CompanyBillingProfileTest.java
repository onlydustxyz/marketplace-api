package onlydust.com.marketplace.api.domain.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CompanyBillingProfileTest {

    @Test
    void mandate_is_accepted() {
        var companyBillingProfile = CompanyBillingProfile.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .status(VerificationStatus.NOT_STARTED)
                .build();
        assertThat(companyBillingProfile.isInvoiceMandateAccepted()).isFalse();

        companyBillingProfile = CompanyBillingProfile.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .status(VerificationStatus.NOT_STARTED)
                .invoiceMandateAcceptedAt(ZonedDateTime.now())
                .invoiceMandateLatestVersionDate(ZonedDateTime.now().minusDays(1))
                .build();
        assertThat(companyBillingProfile.isInvoiceMandateAccepted()).isTrue();

        companyBillingProfile = CompanyBillingProfile.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .status(VerificationStatus.NOT_STARTED)
                .invoiceMandateAcceptedAt(ZonedDateTime.now())
                .invoiceMandateLatestVersionDate(ZonedDateTime.now().plusDays(1))
                .build();
        assertThat(companyBillingProfile.isInvoiceMandateAccepted()).isFalse();
    }

    @Test
    void should_update_status_given_no_children() {
        assertUpdatedStatusIsEqualsTo(VerificationStatus.STARTED, List.of(), VerificationStatus.STARTED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.UNDER_REVIEW, List.of(), VerificationStatus.UNDER_REVIEW);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.REJECTED, List.of(), VerificationStatus.REJECTED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.CLOSED, List.of(), VerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.VERIFIED, List.of(), VerificationStatus.VERIFIED);
    }

    @Test
    void should_update_status_from_children_statuses_given_one_children() {
        assertUpdatedStatusIsEqualsTo(VerificationStatus.STARTED, List.of(VerificationStatus.STARTED), VerificationStatus.STARTED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.STARTED, List.of(VerificationStatus.UNDER_REVIEW), VerificationStatus.STARTED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.STARTED, List.of(VerificationStatus.REJECTED), VerificationStatus.REJECTED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.STARTED, List.of(VerificationStatus.CLOSED), VerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.STARTED, List.of(VerificationStatus.VERIFIED), VerificationStatus.STARTED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.UNDER_REVIEW, List.of(VerificationStatus.STARTED), VerificationStatus.STARTED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.UNDER_REVIEW, List.of(VerificationStatus.UNDER_REVIEW), VerificationStatus.UNDER_REVIEW);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.UNDER_REVIEW, List.of(VerificationStatus.REJECTED), VerificationStatus.REJECTED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.UNDER_REVIEW, List.of(VerificationStatus.CLOSED), VerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.UNDER_REVIEW, List.of(VerificationStatus.VERIFIED), VerificationStatus.UNDER_REVIEW);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.REJECTED, List.of(VerificationStatus.STARTED), VerificationStatus.REJECTED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.REJECTED, List.of(VerificationStatus.UNDER_REVIEW), VerificationStatus.REJECTED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.REJECTED, List.of(VerificationStatus.REJECTED), VerificationStatus.REJECTED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.REJECTED, List.of(VerificationStatus.CLOSED), VerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.REJECTED, List.of(VerificationStatus.VERIFIED), VerificationStatus.REJECTED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.CLOSED, List.of(VerificationStatus.STARTED), VerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.CLOSED, List.of(VerificationStatus.UNDER_REVIEW), VerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.CLOSED, List.of(VerificationStatus.REJECTED), VerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.CLOSED, List.of(VerificationStatus.CLOSED), VerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.CLOSED, List.of(VerificationStatus.VERIFIED), VerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.VERIFIED, List.of(VerificationStatus.STARTED), VerificationStatus.STARTED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.VERIFIED, List.of(VerificationStatus.UNDER_REVIEW), VerificationStatus.UNDER_REVIEW);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.VERIFIED, List.of(VerificationStatus.REJECTED), VerificationStatus.REJECTED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.VERIFIED, List.of(VerificationStatus.CLOSED), VerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.VERIFIED, List.of(VerificationStatus.VERIFIED), VerificationStatus.VERIFIED);
    }

    @Test
    void should_update_status_from_children_statuses_given_two_children() {
        assertUpdatedStatusIsEqualsTo(VerificationStatus.STARTED, List.of(VerificationStatus.NOT_STARTED, VerificationStatus.STARTED),
                VerificationStatus.NOT_STARTED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.STARTED, List.of(VerificationStatus.UNDER_REVIEW, VerificationStatus.STARTED),
                VerificationStatus.STARTED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.STARTED, List.of(VerificationStatus.REJECTED, VerificationStatus.UNDER_REVIEW),
                VerificationStatus.REJECTED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.STARTED, List.of(VerificationStatus.CLOSED, VerificationStatus.REJECTED), VerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.STARTED, List.of(VerificationStatus.VERIFIED, VerificationStatus.UNDER_REVIEW),
                VerificationStatus.STARTED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.UNDER_REVIEW, List.of(VerificationStatus.STARTED, VerificationStatus.STARTED),
                VerificationStatus.STARTED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.UNDER_REVIEW, List.of(VerificationStatus.NOT_STARTED, VerificationStatus.STARTED),
                VerificationStatus.NOT_STARTED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.UNDER_REVIEW, List.of(VerificationStatus.REJECTED, VerificationStatus.UNDER_REVIEW),
                VerificationStatus.REJECTED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.UNDER_REVIEW, List.of(VerificationStatus.CLOSED, VerificationStatus.UNDER_REVIEW),
                VerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.UNDER_REVIEW, List.of(VerificationStatus.VERIFIED, VerificationStatus.UNDER_REVIEW),
                VerificationStatus.UNDER_REVIEW);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.REJECTED, List.of(VerificationStatus.STARTED, VerificationStatus.UNDER_REVIEW),
                VerificationStatus.REJECTED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.REJECTED, List.of(VerificationStatus.UNDER_REVIEW, VerificationStatus.UNDER_REVIEW),
                VerificationStatus.REJECTED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.REJECTED, List.of(VerificationStatus.REJECTED, VerificationStatus.UNDER_REVIEW),
                VerificationStatus.REJECTED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.REJECTED, List.of(VerificationStatus.CLOSED, VerificationStatus.STARTED), VerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.REJECTED, List.of(VerificationStatus.VERIFIED, VerificationStatus.UNDER_REVIEW),
                VerificationStatus.REJECTED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.CLOSED, List.of(VerificationStatus.STARTED, VerificationStatus.UNDER_REVIEW),
                VerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.CLOSED, List.of(VerificationStatus.UNDER_REVIEW, VerificationStatus.REJECTED),
                VerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.CLOSED, List.of(VerificationStatus.REJECTED, VerificationStatus.STARTED), VerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.CLOSED, List.of(VerificationStatus.CLOSED, VerificationStatus.VERIFIED), VerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.CLOSED, List.of(VerificationStatus.VERIFIED, VerificationStatus.STARTED), VerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.VERIFIED, List.of(VerificationStatus.STARTED, VerificationStatus.VERIFIED),
                VerificationStatus.STARTED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.VERIFIED, List.of(VerificationStatus.UNDER_REVIEW, VerificationStatus.VERIFIED),
                VerificationStatus.UNDER_REVIEW);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.VERIFIED, List.of(VerificationStatus.REJECTED, VerificationStatus.STARTED),
                VerificationStatus.REJECTED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.VERIFIED, List.of(VerificationStatus.CLOSED, VerificationStatus.STARTED), VerificationStatus.CLOSED);
        assertUpdatedStatusIsEqualsTo(VerificationStatus.VERIFIED, List.of(VerificationStatus.VERIFIED, VerificationStatus.VERIFIED),
                VerificationStatus.VERIFIED);
    }


    public void assertUpdatedStatusIsEqualsTo(final VerificationStatus parentStatus, final List<VerificationStatus> childrenStatuses,
                                              final VerificationStatus expectedVerificationStatus) {
        Assertions.assertEquals(expectedVerificationStatus, CompanyBillingProfile.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .status(parentStatus)
                .build().updateStatusFromNewChildrenStatuses(childrenStatuses).getStatus());
    }
}
