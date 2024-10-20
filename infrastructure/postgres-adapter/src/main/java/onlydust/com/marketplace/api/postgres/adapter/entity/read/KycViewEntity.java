package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Country;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Kyc;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.kernel.model.UserId;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Table(name = "kyc", schema = "accounting")
@EntityListeners(AuditingEntityListener.class)
@Immutable
public class KycViewEntity {
    @Id
    UUID id;
    UUID billingProfileId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billingProfileId", insertable = false, updatable = false)
    BillingProfileViewEntity billingProfile;

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
}
