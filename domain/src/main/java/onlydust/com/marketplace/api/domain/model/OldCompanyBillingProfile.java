package onlydust.com.marketplace.api.domain.model;

import lombok.*;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Data
@Builder(toBuilder = true)
public class OldCompanyBillingProfile {
    @NonNull
    UUID id;
    @NonNull
    UUID userId;
    @NonNull
    VerificationStatus status;
    String name;
    String registrationNumber;
    Date registrationDate;
    String address;
    Country country;
    Boolean usEntity;
    Boolean subjectToEuropeVAT;
    String euVATNumber;
    String reviewMessageForApplicant;
    String externalApplicantId;

    @Getter(AccessLevel.NONE)
    ZonedDateTime invoiceMandateAcceptedAt;
    @Getter(AccessLevel.NONE)
    ZonedDateTime invoiceMandateLatestVersionDate;

    public boolean isInvoiceMandateAccepted() {
        return invoiceMandateAcceptedAt != null &&
               invoiceMandateLatestVersionDate != null &&
               invoiceMandateAcceptedAt.isAfter(invoiceMandateLatestVersionDate);
    }

    public static OldCompanyBillingProfile initForUser(final UUID userId) {
        return OldCompanyBillingProfile.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .status(VerificationStatus.NOT_STARTED)
                .build();
    }

    public OldCompanyBillingProfile updateStatusFromNewChildrenStatuses(final List<VerificationStatus> childrenStatuses) {
        if (isNull(childrenStatuses) || childrenStatuses.isEmpty()) {
            return this;
        }
        final List<VerificationStatus> childrenStatus =
                childrenStatuses.stream().sorted(Comparator.comparingInt(VerificationStatus::getPriority)).collect(Collectors.toList());
        Collections.reverse(childrenStatus);
        final VerificationStatus worstChildrenVerificationStatus = childrenStatus.get(0);
        if (this.status.getPriority() >= worstChildrenVerificationStatus.getPriority()) {
            return this;
        }
        return updateStatus(worstChildrenVerificationStatus);
    }

    private OldCompanyBillingProfile updateStatus(final VerificationStatus newStatus) {
        return this.toBuilder().status(newStatus).build();
    }
}
