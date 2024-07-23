package onlydust.com.marketplace.api.read.entities.billing_profile;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.api.contract.model.BillingProfileCoworkerRole;
import onlydust.com.marketplace.api.contract.model.ShortBillingProfileResponse;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Accessors(fluent = true, chain = true)
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
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "accounting.billing_profile_type")
    @NonNull
    BillingProfileType type;

    ZonedDateTime invoiceMandateAcceptedAt;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "accounting.verification_status")
    @NonNull
    VerificationStatus verificationStatus;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "billingProfile")
    KycReadEntity kyc;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "billingProfile")
    KybReadEntity kyb;

    Boolean enabled;

    @OneToMany(mappedBy = "billingProfile")
    @NonNull
    Set<AllBillingProfileUserReadEntity> users;

    @OneToMany(mappedBy = "billingProfile")
    @NonNull
    Set<BillingProfileUserInvitationReadEntity> invitations;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "billingProfile")
    @NonNull
    BillingProfileStatsReadEntity stats;

    @Formula("(select gs.invoice_mandate_latest_version_date from global_settings gs where gs.id=1)")
    ZonedDateTime invoiceMandateLatestVersionDate;

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

    public UserSearchBillingProfile toUserSearch() {
        return new UserSearchBillingProfile()
                .id(id)
                .name(name)
                .type(type)
                .status(verificationStatus)
                .kyb(kyb == null ? null : kyb.toUserSearch())
                .kyc(kyc == null ? null : kyc.toUserSearch())
                ;
    }

    public ShortBillingProfileResponse toShortResponse(Long callerGithubUserId) {
        final var caller = users.stream().filter(u -> u.githubUserId().equals(callerGithubUserId)).findFirst();
        return new ShortBillingProfileResponse()
                .id(id)
                .type(switch (type) {
                    case INDIVIDUAL -> onlydust.com.marketplace.api.contract.model.BillingProfileType.INDIVIDUAL;
                    case COMPANY -> onlydust.com.marketplace.api.contract.model.BillingProfileType.COMPANY;
                    case SELF_EMPLOYED -> onlydust.com.marketplace.api.contract.model.BillingProfileType.SELF_EMPLOYED;
                })
                .name(name)
                .enabled(enabled)
                .role(caller.map(AllBillingProfileUserReadEntity::role).orElse(null))
                .invoiceMandateAccepted(isInvoiceMandateAccepted())
                .rewardCount(stats.rewardCount())
                .invoiceableRewardCount(stats.invoiceableRewardCount())
                .currentYearPaymentLimit(stats.currentYearPaymentLimit())
                .currentYearPaymentAmount(stats.currentYearPaymentAmount())
                .individualLimitReached(stats.individualLimitReached())
                .missingPayoutInfo(stats.missingPayoutInfo())
                .missingVerification(stats.missingVerification())
                .pendingInvitationResponse(caller.map(u -> !u.invitationAccepted()).orElse(null))
                .requestableRewardCount(requestableRewardCount(caller))
                .verificationBlocked(isVerificationBlocked());
    }

    private Integer requestableRewardCount(Optional<AllBillingProfileUserReadEntity> user) {
        return user.map(u -> u.role() == BillingProfileCoworkerRole.ADMIN && u.invitationAccepted() ? stats.invoiceableRewardCount() : 0).orElse(null);
    }

    private boolean isInvoiceMandateAccepted() {
        return invoiceMandateAcceptedAt != null && invoiceMandateAcceptedAt.isAfter(invoiceMandateLatestVersionDate);
    }

    private boolean isVerificationBlocked() {
        return verificationStatus == VerificationStatus.REJECTED || verificationStatus == VerificationStatus.CLOSED;
    }

    public BillingProfileLinkResponse toBoLinkResponse() {
        return new BillingProfileLinkResponse()
                .id(id)
                .type(type)
                .subject(kyc != null ? kyc.subject() : kyb != null ? kyb.subject() : null)
                ;
    }
}
