package onlydust.com.marketplace.project.domain.model;

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
    OldVerificationStatus status;
    String name;
    String registrationNumber;
    Date registrationDate;
    String address;
    OldCountry oldCountry;
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
                .status(OldVerificationStatus.NOT_STARTED)
                .build();
    }

    public OldCompanyBillingProfile updateStatusFromNewChildrenStatuses(final List<OldVerificationStatus> childrenStatuses) {
        if (isNull(childrenStatuses) || childrenStatuses.isEmpty()) {
            return this;
        }
        final List<OldVerificationStatus> childrenStatus =
                childrenStatuses.stream().sorted(Comparator.comparingInt(OldVerificationStatus::getPriority)).collect(Collectors.toList());
        Collections.reverse(childrenStatus);
        final OldVerificationStatus worstChildrenOldVerificationStatus = childrenStatus.get(0);
        if (this.status.getPriority() >= worstChildrenOldVerificationStatus.getPriority()) {
            return this;
        }
        return updateStatus(worstChildrenOldVerificationStatus);
    }

    private OldCompanyBillingProfile updateStatus(final OldVerificationStatus newStatus) {
        return this.toBuilder().status(newStatus).build();
    }
}
