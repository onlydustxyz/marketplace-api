package onlydust.com.marketplace.accounting.domain.model.billingprofile;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Country;
import onlydust.com.marketplace.accounting.domain.model.UserId;

import java.util.Date;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
public class Kyb {
    @NonNull
    UUID id;
    @NonNull
    UserId ownerId;
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

    public static Kyb initForUser(final UserId userId) {
        return Kyb.builder()
                .id(UUID.randomUUID())
                .ownerId(userId)
                .status(VerificationStatus.NOT_STARTED)
                .build();
    }
}
