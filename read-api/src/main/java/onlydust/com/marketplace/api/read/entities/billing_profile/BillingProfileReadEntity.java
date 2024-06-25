package onlydust.com.marketplace.api.read.entities.billing_profile;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.backoffice.api.contract.model.BillingProfileShortResponse;
import onlydust.com.backoffice.api.contract.model.BillingProfileType;
import onlydust.com.backoffice.api.contract.model.VerificationStatus;
import org.hibernate.annotations.Immutable;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Accessors(fluent = true)
@Table(name = "billing_profiles", schema = "accounting")
@Immutable
public class BillingProfileReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID id;

    @NonNull
    String name;
    @Enumerated(EnumType.STRING)
    @NonNull
    BillingProfileType type;

    ZonedDateTime invoiceMandateAcceptedAt;

    @Enumerated(EnumType.STRING)
    @NonNull
    VerificationStatus verificationStatus;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "billingProfile")
    KycReadEntity kyc;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "billingProfile")
    KybReadEntity kyb;

    Boolean enabled;

    @OneToMany(mappedBy = "billingProfile")
    Set<AllBillingProfileUserReadEntity> users;

    @OneToMany(mappedBy = "billingProfile")
    Set<BillingProfileUserInvitationReadEntity> invitations;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "billingProfile")
    BillingProfileStatsReadEntity stats;

    public BillingProfileShortResponse toBoShortResponse() {
        return new BillingProfileShortResponse()
                .id(id)
                .subject(kyc != null ? kyc.subject() : kyb != null ? kyb.subject() : null)
                .type(type)
                .name(name)
                .verificationStatus(verificationStatus)
                .kyb(kyb == null ? null : kyb.toDto())
                .kyc(kyc == null ? null : kyc.toDto())
                ;
    }
}
