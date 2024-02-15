package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.view.InvoicePreview;
import onlydust.com.marketplace.api.domain.model.CompanyBillingProfile;
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
@Table(name = "company_billing_profiles", schema = "public")
@EntityListeners(AuditingEntityListener.class)
@TypeDef(name = "verification_status", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "id_document_type", typeClass = PostgreSQLEnumType.class)
public class CompanyBillingProfileEntity {
    @Id
    UUID id;
    UUID userId;
    @Type(type = "billing_profile_type")
    @Enumerated(EnumType.STRING)
    VerificationStatusEntity verificationStatus;
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
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Date createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @EqualsAndHashCode.Exclude
    private Date updatedAt;

    public CompanyBillingProfile toDomain() {
        return CompanyBillingProfile.builder()
                .id(this.id)
                .status(this.verificationStatus.toDomain())
                .usEntity(this.usEntity)
                .address(this.address)
                .euVATNumber(this.euVATNumber)
                .subjectToEuropeVAT(this.subjectToEuVAT)
                .registrationDate(this.registrationDate)
                .name(this.name)
                .registrationNumber(this.registrationNumber)
                .country(this.country)
                .userId(this.userId)
                .reviewMessageForApplicant(this.reviewMessage)
                .build();
    }

    public static CompanyBillingProfileEntity fromDomain(final CompanyBillingProfile companyBillingProfile) {
        return CompanyBillingProfileEntity.builder()
                .id(companyBillingProfile.getId())
                .userId(companyBillingProfile.getUserId())
                .usEntity(companyBillingProfile.getUsEntity())
                .address(companyBillingProfile.getAddress())
                .country(companyBillingProfile.getCountry())
                .name(companyBillingProfile.getName())
                .verificationStatus(VerificationStatusEntity.fromDomain(companyBillingProfile.getStatus()))
                .subjectToEuVAT(companyBillingProfile.getSubjectToEuropeVAT())
                .euVATNumber(companyBillingProfile.getEuVATNumber())
                .registrationDate(companyBillingProfile.getRegistrationDate())
                .registrationNumber(companyBillingProfile.getRegistrationNumber())
                .reviewMessage(companyBillingProfile.getReviewMessageForApplicant())
                .build();
    }

    public InvoicePreview.CompanyInfo forInvoicePreview() {
        return new InvoicePreview.CompanyInfo(
                registrationNumber,
                name,
                address,
                subjectToEuVAT,
                euVATNumber
        );
    }
}
