package onlydust.com.marketplace.api.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Data
@Builder(toBuilder = true)
public class CompanyBillingProfile {
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
    String country;
    Boolean usEntity;
    Boolean subjectToEuropeVAT;
    String euVATNumber;
    String reviewMessageForApplicant;
    String externalApplicantId;

    public static CompanyBillingProfile initForUser(final UUID userId) {
        return CompanyBillingProfile.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .status(VerificationStatus.NOT_STARTED)
                .build();
    }

    public CompanyBillingProfile updateStatusFromNewChildrenStatuses(final List<VerificationStatus> childrenStatuses) {
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

    private CompanyBillingProfile updateStatus(final VerificationStatus newStatus) {
        return this.toBuilder().status(newStatus).build();
    }
}
