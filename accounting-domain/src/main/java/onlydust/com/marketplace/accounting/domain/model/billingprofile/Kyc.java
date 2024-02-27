package onlydust.com.marketplace.accounting.domain.model.billingprofile;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Country;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;

import java.util.Date;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
public class Kyc {
    @NonNull
    UUID id;
    @NonNull
    UserId ownerId;
    @NonNull
    VerificationStatus status;
    BillingProfile.Id billingProfileId;
    String firstName;
    String lastName;
    Date birthdate;
    String address;
    Country country;
    Boolean usCitizen;
    IdDocumentTypeEnum idDocumentType;
    String idDocumentNumber;
    String idDocumentCountryCode;
    Date validUntil;
    String reviewMessageForApplicant;
    String externalApplicantId;

    public static Kyc initForUserAndBillingProfile(final UserId userId, final BillingProfile.Id billingProfileId) {
        return Kyc.builder()
                .id(UUID.randomUUID())
                .status(VerificationStatus.NOT_STARTED)
                .billingProfileId(billingProfileId)
                .ownerId(userId)
                .build();
    }

    public enum IdDocumentTypeEnum {
        PASSPORT,
        ID_CARD,
        RESIDENCE_PERMIT,
        DRIVER_LICENSE
    }

}
