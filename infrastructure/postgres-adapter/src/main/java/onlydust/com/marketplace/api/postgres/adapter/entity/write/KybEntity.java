package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.Country;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Kyb;
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
@Accessors(chain = true, fluent = true)
@Builder(toBuilder = true)
@Table(name = "kyb", schema = "accounting")
@EntityListeners(AuditingEntityListener.class)
public class KybEntity {
    @Id
    UUID id;
    UUID billingProfileId;
    @OneToOne
    @JoinColumn(name = "billingProfileId", insertable = false, updatable = false)
    BillingProfileEntity billingProfile;
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "verification_status")
    VerificationStatus verificationStatus;
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
                .status(this.verificationStatus)
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
                .verificationStatus(kyb.getStatus())
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
