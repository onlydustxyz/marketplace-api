package onlydust.com.marketplace.api.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.Date;
import java.util.UUID;

@Data
@Builder
public class CompanyBillingProfile {
    @NonNull
    UUID id;
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

    public static CompanyBillingProfile init() {
        return CompanyBillingProfile.builder()
                .id(UUID.randomUUID())
                .status(VerificationStatus.NOT_STARTED)
                .build();
    }
}
