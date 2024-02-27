package onlydust.com.marketplace.project.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.Date;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
public class OldIndividualBillingProfile {
    @NonNull
    UUID id;
    @NonNull
    UUID userId;
    @NonNull
    OldVerificationStatus status;
    String firstName;
    String lastName;
    Date birthdate;
    String address;
    OldCountry oldCountry;
    Boolean usCitizen;
    OldIdDocumentTypeEnum idDocumentType;
    String idDocumentNumber;
    String idDocumentCountryCode;
    Date validUntil;
    String reviewMessageForApplicant;
    String externalApplicantId;

    public static OldIndividualBillingProfile initForUser(final UUID userId) {
        return OldIndividualBillingProfile.builder()
                .id(UUID.randomUUID())
                .status(OldVerificationStatus.NOT_STARTED)
                .userId(userId)
                .build();
    }

    public enum OldIdDocumentTypeEnum {
        PASSPORT,
        ID_CARD,
        RESIDENCE_PERMIT,
        DRIVER_LICENSE
    }
}
