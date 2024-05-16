package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.Country;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Kyb;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
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
@Table(name = "kyb", schema = "accounting")
@EntityListeners(AuditingEntityListener.class)
@Immutable
public class KybViewEntity {
    @Id
    UUID id;
    UUID billingProfileId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billingProfileId", insertable = false, updatable = false)
    BillingProfileViewEntity billingProfile;

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
}
