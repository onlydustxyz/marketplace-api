package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.Country;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Kyc;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.kernel.model.UserId;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Builder(toBuilder = true)
@Table(name = "kyc", schema = "accounting")
@Accessors(chain = true, fluent = true)
@EntityListeners(AuditingEntityListener.class)
public class KycEntity {
    @Id
    UUID id;
    UUID billingProfileId;
    @OneToOne
    @JoinColumn(name = "billingProfileId", insertable = false, updatable = false)
    BillingProfileEntity billingProfile;

    UUID ownerId;
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "verification_status")
    VerificationStatus verificationStatus;
    String firstName;
    String lastName;
    Date birthdate;
    String address;
    String country;
    Boolean consideredUsPersonQuestionnaire;
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "id_document_type")
    Kyc.IdDocumentTypeEnum idDocumentType;
    String idDocumentNumber;
    String idDocumentCountryCode;
    Date validUntil;
    String reviewMessage;
    String applicantId;
    Boolean usCitizen;

    @CreationTimestamp
    @Column(name = "tech_created_at", nullable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Date createdAt;
    @UpdateTimestamp
    @Column(name = "tech_updated_at", nullable = false)
    @EqualsAndHashCode.Exclude
    private Date updatedAt;


    public Kyc toDomain() {
        return Kyc.builder()
                .id(this.id)
                .billingProfileId(BillingProfile.Id.of(this.billingProfileId))
                .status(this.verificationStatus)
                .idDocumentType(this.idDocumentType)
                .address(this.address)
                .country(this.country == null ? null : Country.fromIso3(this.country))
                .firstName(this.firstName)
                .lastName(this.lastName)
                .validUntil(this.validUntil)
                .idDocumentNumber(this.idDocumentNumber)
                .idDocumentCountry(idDocumentCountryCode == null ? null : Country.fromIso3(idDocumentCountryCode))
                .consideredUsPersonQuestionnaire(this.consideredUsPersonQuestionnaire)
                .birthdate(this.birthdate)
                .ownerId(UserId.of(this.ownerId))
                .reviewMessageForApplicant(this.reviewMessage)
                .externalApplicantId(this.applicantId)
                .build();
    }

    public static KycEntity fromDomain(final Kyc kyc) {
        return KycEntity.builder()
                .id(kyc.getId())
                .billingProfileId(kyc.getBillingProfileId().value())
                .address(kyc.getAddress())
                .birthdate(kyc.getBirthdate())
                .country(kyc.getCountry().map(Country::iso3Code).orElse(null))
                .firstName(kyc.getFirstName())
                .lastName(kyc.getLastName())
                .validUntil(kyc.getValidUntil())
                .idDocumentNumber(kyc.getIdDocumentNumber())
                .idDocumentType(kyc.getIdDocumentType())
                .ownerId(kyc.getOwnerId().value())
                .consideredUsPersonQuestionnaire(kyc.getConsideredUsPersonQuestionnaire())
                .verificationStatus(kyc.getStatus())
                .idDocumentCountryCode(kyc.getIdDocumentCountry().map(Country::iso3Code).orElse(null))
                .reviewMessage(kyc.getReviewMessageForApplicant())
                .applicantId(kyc.getExternalApplicantId())
                .usCitizen(kyc.isUsCitizen())
                .build();
    }
}
