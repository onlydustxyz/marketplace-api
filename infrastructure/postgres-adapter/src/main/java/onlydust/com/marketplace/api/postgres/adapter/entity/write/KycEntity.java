package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.Country;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Kyc;
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
@Table(name = "kyc", schema = "accounting")
@EntityListeners(AuditingEntityListener.class)
@TypeDef(name = "verification_status", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "id_document_type", typeClass = PostgreSQLEnumType.class)
public class KycEntity {
    @Id
    UUID id;
    UUID billingProfileId;
    UUID ownerId;
    @Type(type = "verification_status")
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
    @Column(name = "tech_created_at", nullable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Date createdAt;
    @UpdateTimestamp
    @Column(name = "tech_updated_at", nullable = false)
    @EqualsAndHashCode.Exclude
    private Date updatedAt;

    public enum IdDocumentTypeEnumEntity {
        PASSPORT,
        ID_CARD,
        RESIDENCE_PERMIT,
        DRIVER_LICENSE;

        public Kyc.IdDocumentTypeEnum toDomain() {
            return switch (this) {
                case ID_CARD -> Kyc.IdDocumentTypeEnum.ID_CARD;
                case PASSPORT -> Kyc.IdDocumentTypeEnum.PASSPORT;
                case RESIDENCE_PERMIT -> Kyc.IdDocumentTypeEnum.RESIDENCE_PERMIT;
                case DRIVER_LICENSE -> Kyc.IdDocumentTypeEnum.DRIVER_LICENSE;
            };
        }

        public static IdDocumentTypeEnumEntity fromDomain(Kyc.IdDocumentTypeEnum oldIdDocumentTypeEnum) {
            return isNull(oldIdDocumentTypeEnum) ? null : switch (oldIdDocumentTypeEnum) {
                case ID_CARD -> ID_CARD;
                case PASSPORT -> PASSPORT;
                case RESIDENCE_PERMIT -> RESIDENCE_PERMIT;
                case DRIVER_LICENSE -> DRIVER_LICENSE;
            };
        }
    }

    public Kyc toDomain() {
        return Kyc.builder()
                .id(this.id)
                .billingProfileId(BillingProfile.Id.of(this.billingProfileId))
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
                .country(kyc.getCountry() == null ? null : kyc.getCountry().iso3Code())
                .firstName(kyc.getFirstName())
                .lastName(kyc.getLastName())
                .validUntil(kyc.getValidUntil())
                .idDocumentNumber(kyc.getIdDocumentNumber())
                .idDocumentType(IdDocumentTypeEnumEntity.fromDomain(kyc.getIdDocumentType()))
                .ownerId(kyc.getOwnerId().value())
                .usCitizen(kyc.getUsCitizen())
                .verificationStatus(VerificationStatusEntity.fromDomain(kyc.getStatus()))
                .idDocumentCountryCode(kyc.getIdDocumentCountryCode())
                .reviewMessage(kyc.getReviewMessageForApplicant())
                .applicantId(kyc.getExternalApplicantId())
                .build();
    }
}
