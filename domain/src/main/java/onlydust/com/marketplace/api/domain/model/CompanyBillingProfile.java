package onlydust.com.marketplace.api.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.Date;
import java.util.UUID;

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

    public static CompanyBillingProfile initForUser(final UUID userId) {
        return CompanyBillingProfile.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .status(VerificationStatus.NOT_STARTED)
                .build();
    }
}
