package onlydust.com.marketplace.project.domain.model;

import lombok.*;

import java.time.ZonedDateTime;
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

    @Getter(AccessLevel.NONE)
    ZonedDateTime invoiceMandateAcceptedAt;
    @Getter(AccessLevel.NONE)
    ZonedDateTime invoiceMandateLatestVersionDate;

    public boolean isInvoiceMandateAccepted() {
        return invoiceMandateAcceptedAt != null &&
               invoiceMandateLatestVersionDate != null &&
               invoiceMandateAcceptedAt.isAfter(invoiceMandateLatestVersionDate);
    }

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
