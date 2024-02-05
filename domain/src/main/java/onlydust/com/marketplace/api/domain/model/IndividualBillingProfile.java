package onlydust.com.marketplace.api.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.Date;
import java.util.UUID;

@Data
@Builder
public class IndividualBillingProfile {
    @NonNull
    UUID id;
    @NonNull
    VerificationStatus status;
    String firstName;
    String lastName;
    Date birthdate;
    String address;
    String country;
    Boolean usCitizen;
    IdDocumentTypeEnum idDocumentType;
    String idDocumentNumber;
    Date validUntil;

    public static IndividualBillingProfile init() {
        return IndividualBillingProfile.builder()
                .id(UUID.randomUUID())
                .status(VerificationStatus.NOT_STARTED)
                .build();
    }

    public enum IdDocumentTypeEnum {
        PASSPORT,
        ID_CARD,
        RESIDENCE_PERMIT,
        DRIVER_LICENSE
    }

}
