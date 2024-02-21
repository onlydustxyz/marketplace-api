package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.project.domain.model.OldCountry;
import onlydust.com.marketplace.project.domain.model.OldIndividualBillingProfile;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

import static java.time.ZoneOffset.UTC;
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
    Date invoiceMandateAcceptedAt;
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

        public OldIndividualBillingProfile.OldIdDocumentTypeEnum toDomain() {
            return switch (this) {
                case ID_CARD -> OldIndividualBillingProfile.OldIdDocumentTypeEnum.ID_CARD;
                case PASSPORT -> OldIndividualBillingProfile.OldIdDocumentTypeEnum.PASSPORT;
                case RESIDENCE_PERMIT -> OldIndividualBillingProfile.OldIdDocumentTypeEnum.RESIDENCE_PERMIT;
                case DRIVER_LICENSE -> OldIndividualBillingProfile.OldIdDocumentTypeEnum.DRIVER_LICENSE;
            };
        }

        public static IdDocumentTypeEnumEntity fromDomain(OldIndividualBillingProfile.OldIdDocumentTypeEnum oldIdDocumentTypeEnum) {
            return isNull(oldIdDocumentTypeEnum) ? null : switch (oldIdDocumentTypeEnum) {
                case ID_CARD -> ID_CARD;
                case PASSPORT -> PASSPORT;
                case RESIDENCE_PERMIT -> RESIDENCE_PERMIT;
                case DRIVER_LICENSE -> DRIVER_LICENSE;
            };
        }
    }

    public OldIndividualBillingProfile toDomain(@NonNull Date invoiceMandateLatestVersionDate) {
        return OldIndividualBillingProfile.builder()
                .id(this.id)
                .status(this.verificationStatus.toDomain())
                .idDocumentType(isNull(this.idDocumentType) ? null : this.idDocumentType.toDomain())
                .address(this.address)
                .oldCountry(this.country == null ? null : OldCountry.fromIso3(this.country))
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
                .invoiceMandateAcceptedAt(this.invoiceMandateAcceptedAt != null ? this.invoiceMandateAcceptedAt.toInstant().atZone(UTC) : null)
                .invoiceMandateLatestVersionDate(invoiceMandateLatestVersionDate.toInstant().atZone(UTC))
                .build();
    }

    public static IndividualBillingProfileEntity fromDomain(final OldIndividualBillingProfile individualBillingProfile) {
        return IndividualBillingProfileEntity.builder()
                .id(individualBillingProfile.getId())
                .address(individualBillingProfile.getAddress())
                .birthdate(individualBillingProfile.getBirthdate())
                .country(individualBillingProfile.getOldCountry() == null ? null : individualBillingProfile.getOldCountry().iso3Code())
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
