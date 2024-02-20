package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.api.domain.model.Country;
import onlydust.com.marketplace.api.domain.model.IndividualBillingProfile;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

import static java.util.Objects.isNull;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Builder(toBuilder = true)
@Table(name = "individual_billing_profiles", schema = "public")
@EntityListeners(AuditingEntityListener.class)
@TypeDef(name = "verification_status", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "id_document_type", typeClass = PostgreSQLEnumType.class)
public class IndividualBillingProfileEntity {
    @Id
    UUID id;
    UUID userId;
    @Type(type = "billing_profile_type")
    @Enumerated(EnumType.STRING)
    VerificationStatusEntity verificationStatus;
    String firstName;
    String lastName;
    Date birthdate;
    String address;
    String country;
    Boolean usCitizen;
    @Type(type = "id_document_type")
    @Enumerated(EnumType.STRING)
    IdDocumentTypeEnumEntity idDocumentType;
    String idDocumentNumber;
    String idDocumentCountryCode;
    Date validUntil;
    String reviewMessage;
    String applicantId;
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Date createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @EqualsAndHashCode.Exclude
    private Date updatedAt;

    public Invoice.PersonalInfo forInvoice() {
        return new Invoice.PersonalInfo(firstName, lastName, address);
    }

    public enum IdDocumentTypeEnumEntity {
        PASSPORT,
        ID_CARD,
        RESIDENCE_PERMIT,
        DRIVER_LICENSE;

        public IndividualBillingProfile.IdDocumentTypeEnum toDomain() {
            return switch (this) {
                case ID_CARD -> IndividualBillingProfile.IdDocumentTypeEnum.ID_CARD;
                case PASSPORT -> IndividualBillingProfile.IdDocumentTypeEnum.PASSPORT;
                case RESIDENCE_PERMIT -> IndividualBillingProfile.IdDocumentTypeEnum.RESIDENCE_PERMIT;
                case DRIVER_LICENSE -> IndividualBillingProfile.IdDocumentTypeEnum.DRIVER_LICENSE;
            };
        }

        public static IdDocumentTypeEnumEntity fromDomain(IndividualBillingProfile.IdDocumentTypeEnum idDocumentTypeEnum) {
            return isNull(idDocumentTypeEnum) ? null : switch (idDocumentTypeEnum) {
                case ID_CARD -> ID_CARD;
                case PASSPORT -> PASSPORT;
                case RESIDENCE_PERMIT -> RESIDENCE_PERMIT;
                case DRIVER_LICENSE -> DRIVER_LICENSE;
            };
        }
    }

    public IndividualBillingProfile toDomain() {
        return IndividualBillingProfile.builder()
                .id(this.id)
                .status(this.verificationStatus.toDomain())
                .idDocumentType(isNull(this.idDocumentType) ? null : this.idDocumentType.toDomain())
                .address(this.address)
                .country(this.country == null ? null : Country.fromIso3(this.country))
                .firstName(this.firstName)
                .lastName(this.lastName)
                .validUntil(this.validUntil)
                .idDocumentNumber(this.idDocumentNumber)
                .idDocumentCountryCode(this.idDocumentCountryCode)
                .usCitizen(this.usCitizen)
                .birthdate(this.birthdate)
                .userId(this.userId)
                .reviewMessageForApplicant(this.reviewMessage)
                .externalApplicantId(this.applicantId)
                .build();
    }

    public static IndividualBillingProfileEntity fromDomain(final IndividualBillingProfile individualBillingProfile) {
        return IndividualBillingProfileEntity.builder()
                .id(individualBillingProfile.getId())
                .address(individualBillingProfile.getAddress())
                .birthdate(individualBillingProfile.getBirthdate())
                .country(individualBillingProfile.getCountry() == null ? null : individualBillingProfile.getCountry().iso3Code())
                .firstName(individualBillingProfile.getFirstName())
                .lastName(individualBillingProfile.getLastName())
                .validUntil(individualBillingProfile.getValidUntil())
                .idDocumentNumber(individualBillingProfile.getIdDocumentNumber())
                .idDocumentType(IdDocumentTypeEnumEntity.fromDomain(individualBillingProfile.getIdDocumentType()))
                .userId(individualBillingProfile.getUserId())
                .usCitizen(individualBillingProfile.getUsCitizen())
                .verificationStatus(VerificationStatusEntity.fromDomain(individualBillingProfile.getStatus()))
                .idDocumentCountryCode(individualBillingProfile.getIdDocumentCountryCode())
                .reviewMessage(individualBillingProfile.getReviewMessageForApplicant())
                .applicantId(individualBillingProfile.getExternalApplicantId())
                .build();
    }
}
