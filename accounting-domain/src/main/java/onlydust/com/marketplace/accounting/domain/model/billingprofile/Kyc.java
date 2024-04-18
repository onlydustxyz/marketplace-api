package onlydust.com.marketplace.accounting.domain.model.billingprofile;

import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.Country;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

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
    @Getter(AccessLevel.NONE)
    Country country;
    Boolean consideredUsPersonQuestionnaire;
    IdDocumentTypeEnum idDocumentType;
    String idDocumentNumber;
    @Getter(AccessLevel.NONE)
    Country idDocumentCountry;
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

    public String sumsubUrl() {
        return externalApplicantId == null ? null :
                "https://cockpit.sumsub.com/checkus/#/applicant/%s/basicInfo?clientId=onlydust".formatted(externalApplicantId);
    }

    public String fullName() {
        return firstName == null ? lastName : firstName + " " + lastName;
    }

    public Boolean isUsCitizen() {
        if (TRUE.equals(consideredUsPersonQuestionnaire) ||
            TRUE.equals(getCountry().map(Country::isUsa).orElse(false)) ||
            TRUE.equals(getIdDocumentCountry().map(Country::isUsa).orElse(false)))
            return true;

        if (FALSE.equals(consideredUsPersonQuestionnaire) &&
            FALSE.equals(getCountry().map(Country::isUsa).orElse(true)) &&
            FALSE.equals(getIdDocumentCountry().map(Country::isUsa).orElse(true)))
            return false;

        return null;
    }

    public Optional<Country> getCountry() {
        return Optional.ofNullable(country);
    }

    public Optional<Country> getIdDocumentCountry() {
        return Optional.ofNullable(idDocumentCountry);
    }

    public enum IdDocumentTypeEnum {
        PASSPORT,
        ID_CARD,
        RESIDENCE_PERMIT,
        DRIVER_LICENSE
    }

}
