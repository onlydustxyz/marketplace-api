package onlydust.com.marketplace.api.read.entities.billing_profile;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.backoffice.api.contract.model.KycResponse;
import onlydust.com.backoffice.api.contract.model.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.model.Country;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Value
@Accessors(fluent = true)
@Table(name = "kyc", schema = "accounting")
public class KycReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull UUID id;
    @NonNull UUID billingProfileId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billingProfileId", insertable = false, updatable = false)
    @NonNull BillingProfileReadEntity billingProfile;

    UUID ownerId;
    @Enumerated(EnumType.STRING)
    VerificationStatus verificationStatus;
    String firstName;
    String lastName;
    ZonedDateTime birthdate;
    String address;
    String country;
    Boolean consideredUsPersonQuestionnaire;
    @Enumerated(EnumType.STRING)
    KycResponse.IdDocumentTypeEnum idDocumentType;
    String idDocumentNumber;
    String idDocumentCountryCode;
    ZonedDateTime validUntil;
    String reviewMessage;
    String applicantId;
    Boolean usCitizen;

    public String subject() {
        return firstName == null ? lastName : firstName + " " + lastName;
    }

    public KycResponse toDto() {
        return new KycResponse()
                .firstName(firstName)
                .lastName(lastName)
                .birthdate(birthdate)
                .address(address)
                .country(country == null ? null : Country.fromIso3(country).display().orElse(country))
                .countryCode(country)
                .idDocumentType(idDocumentType)
                .idDocumentNumber(idDocumentNumber)
                .idDocumentCountryCode(idDocumentCountryCode)
                .validUntil(validUntil)
                .usCitizen(usCitizen)
                .sumsubUrl(applicantId == null ? null : "https://cockpit.sumsub.com/checkus/#/applicant/%s/basicInfo?clientId=onlydust".formatted(applicantId))
                ;
    }
}
