package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.Country;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Kyb;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Builder(toBuilder = true)
@Table(name = "kyb", schema = "accounting")
@EntityListeners(AuditingEntityListener.class)
@TypeDef(name = "verification_status", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "id_document_type", typeClass = PostgreSQLEnumType.class)
public class KybEntity {
    @Id
    UUID id;
    UUID billingProfileId;
    @Type(type = "verification_status")
    @Enumerated(EnumType.STRING)
    VerificationStatusEntity verificationStatus;
    UUID ownerId;
    String name;
    String registrationNumber;
    Date registrationDate;
    String address;
    String country;
    Boolean usEntity;
    @Column(name = "subject_to_eu_vat")
    Boolean subjectToEuVAT;
    @Column(name = "eu_vat_number")
    String euVATNumber;
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

    public Kyb toDomain() {
        return Kyb.builder()
                .id(this.id)
                .billingProfileId(BillingProfile.Id.of(this.billingProfileId))
                .status(this.verificationStatus.toDomain())
                .usEntity(this.usEntity)
                .address(this.address)
                .euVATNumber(this.euVATNumber)
                .subjectToEuropeVAT(this.subjectToEuVAT)
                .registrationDate(this.registrationDate)
                .name(this.name)
                .registrationNumber(this.registrationNumber)
                .country(this.country == null ? null : Country.fromIso3(this.country))
                .ownerId(UserId.of(this.ownerId))
                .reviewMessageForApplicant(this.reviewMessage)
                .externalApplicantId(this.applicantId)
                .build();
    }

    public static KybEntity fromDomain(final Kyb kyb) {
        return KybEntity.builder()
                .id(kyb.getId())
                .billingProfileId(kyb.getBillingProfileId().value())
                .usEntity(kyb.getUsEntity())
                .address(kyb.getAddress())
                .country(kyb.getCountry() == null ? null : kyb.getCountry().iso3Code())
                .name(kyb.getName())
                .verificationStatus(VerificationStatusEntity.fromDomain(kyb.getStatus()))
                .subjectToEuVAT(kyb.getSubjectToEuropeVAT())
                .euVATNumber(kyb.getEuVATNumber())
                .registrationDate(kyb.getRegistrationDate())
                .registrationNumber(kyb.getRegistrationNumber())
                .reviewMessage(kyb.getReviewMessageForApplicant())
                .applicantId(kyb.getExternalApplicantId())
                .ownerId(kyb.getOwnerId().value())
                .build();
    }
}
