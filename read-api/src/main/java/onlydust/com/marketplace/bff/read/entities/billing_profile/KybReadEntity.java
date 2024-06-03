package onlydust.com.marketplace.bff.read.entities.billing_profile;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.backoffice.api.contract.model.KybResponse;
import onlydust.com.backoffice.api.contract.model.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.model.Country;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Value
@Accessors(fluent = true)
@Table(name = "kyb", schema = "accounting")
public class KybReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull UUID id;

    @NonNull UUID billingProfileId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billingProfileId", insertable = false, updatable = false)
    BillingProfileReadEntity billingProfile;

    @Enumerated(EnumType.STRING)
    VerificationStatus verificationStatus;

    UUID ownerId;
    String name;
    String registrationNumber;
    ZonedDateTime registrationDate;
    String address;
    String country;
    Boolean usEntity;
    @Column(name = "subject_to_eu_vat")
    Boolean subjectToEuVAT;
    @Column(name = "eu_vat_number")
    String euVATNumber;
    String reviewMessage;
    String applicantId;

    public String subject() {
        return name;
    }

    public KybResponse toDto() {
        return new KybResponse()
                .name(name)
                .registrationNumber(registrationNumber)
                .registrationDate(registrationDate)
                .address(address)
                .country(Country.fromIso3(country).display().orElse(country))
                .countryCode(country)
                .usEntity(usEntity)
                .subjectToEuropeVAT(subjectToEuVAT)
                .euVATNumber(euVATNumber)
                .sumsubUrl(applicantId == null ? null : "https://cockpit.sumsub.com/checkus/#/applicant/%s/basicInfo?clientId=onlydust".formatted(applicantId))
                ;
    }
}
