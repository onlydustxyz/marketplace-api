package onlydust.com.marketplace.api.read.entities.billing_profile;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.backoffice.api.contract.model.KycIdDocumentType;
import onlydust.com.backoffice.api.contract.model.KycResponse;
import onlydust.com.backoffice.api.contract.model.UserSearchKyc;
import onlydust.com.backoffice.api.contract.model.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.model.Country;
import org.hibernate.annotations.Immutable;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Accessors(fluent = true)
@Table(name = "kyc", schema = "accounting")
@Immutable
public class KycReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID id;
    @NonNull
    UUID billingProfileId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billingProfileId", insertable = false, updatable = false)
    @NonNull
    BillingProfileReadEntity billingProfile;

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
    KycIdDocumentType idDocumentType;
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

    public UserSearchKyc toUserSearch() {
        return new UserSearchKyc()
                .firstName(firstName)
                .lastName(lastName)
                .countryCode(country)
                .idDocumentType(idDocumentType)
                .idDocumentNumber(idDocumentNumber)
                .usCitizen(usCitizen)
                .idDocumentCountryCode(idDocumentCountryCode)
                ;
    }
}
