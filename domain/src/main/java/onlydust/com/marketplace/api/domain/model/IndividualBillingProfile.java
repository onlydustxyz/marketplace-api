package onlydust.com.marketplace.api.domain.model;

import lombok.*;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
public class IndividualBillingProfile {
    @NonNull
    UUID id;
    @NonNull
    UUID userId;
    @NonNull
    VerificationStatus status;
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
    
    @Getter(AccessLevel.NONE)
    ZonedDateTime invoiceMandateAcceptedAt;
    @Getter(AccessLevel.NONE)
    ZonedDateTime invoiceMandateLatestVersionDate;

    public boolean isInvoiceMandateAccepted() {
        return invoiceMandateAcceptedAt != null &&
               invoiceMandateLatestVersionDate != null &&
               invoiceMandateAcceptedAt.isAfter(invoiceMandateLatestVersionDate);
    }

    public static IndividualBillingProfile initForUser(final UUID userId) {
        return IndividualBillingProfile.builder()
                .id(UUID.randomUUID())
                .status(VerificationStatus.NOT_STARTED)
                .userId(userId)
                .build();
    }

    public enum IdDocumentTypeEnum {
        PASSPORT,
        ID_CARD,
        RESIDENCE_PERMIT,
        DRIVER_LICENSE
    }

}
