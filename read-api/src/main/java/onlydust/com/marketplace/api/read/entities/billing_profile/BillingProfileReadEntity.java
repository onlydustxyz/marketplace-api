package onlydust.com.marketplace.api.read.entities.billing_profile;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.api.contract.model.BillingProfileCoworkerRole;
import onlydust.com.marketplace.api.contract.model.ShortBillingProfileResponse;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
import onlydust.com.marketplace.api.read.entities.reward.RewardReadEntity;
import onlydust.com.marketplace.api.read.entities.user.AllUserReadEntity;
import onlydust.com.marketplace.api.read.utils.Arithmetic;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.ZonedDateTime;
import java.util.*;

import static java.util.stream.Collectors.*;

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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "billingProfile")
    Set<KycReadEntity> kyc;

    public KycReadEntity kyc() {
        return kyc.stream().findFirst().orElse(null);
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "billingProfile")
    Set<KybReadEntity> kyb;

    public KybReadEntity kyb() {
        return kyb.stream().findFirst().orElse(null);
    }

    Boolean enabled;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "billingProfile")
    @NonNull
    Set<AllBillingProfileUserReadEntity> users;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "billingProfile")
    @NonNull
    Set<BillingProfileUserInvitationReadEntity> invitations;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "billingProfile")
    @NonNull
    Set<BillingProfileStatsReadEntity> stats;

    public BillingProfileStatsReadEntity stats() {
        return stats.stream().findFirst().orElse(null);
    }

    @Formula("(select gs.invoice_mandate_latest_version_date from global_settings gs where gs.id=1)")
    ZonedDateTime invoiceMandateLatestVersionDate;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "billingProfileId")
    @SQLRestriction("""
            id in (
                select rsd.reward_id
                from accounting.reward_status_data rsd
                where date_trunc('month', rsd.paid_at)::date = date_trunc('month', CURRENT_DATE)::date
            )
            """)
    @NonNull
    List<RewardReadEntity> currentMonthRewards;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "billingProfileId")
    @SQLRestriction("""
            id in (
                select rs.reward_id
                from accounting.reward_statuses rs
                where rs.status = 'PAYOUT_INFO_MISSING'
            )
            """)
    @NonNull
    List<RewardReadEntity> missingPayoutInfoRewards;

    List<NetworkEnumEntity> missingPayoutInfoRewardsNetworks() {
        return missingPayoutInfoRewards.stream().flatMap(r -> Arrays.stream(r.statusData().networks())).toList();
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "billingProfile")
    @Getter(AccessLevel.NONE)
    Set<PayoutInfoReadEntity> payoutInfo;

    public PayoutInfoReadEntity payoutInfo() {
        return payoutInfo.stream().findFirst().orElseGet(() -> new PayoutInfoReadEntity(this));
    }

    public BillingProfileShortResponse toBoShortResponse() {
        final var kyc = kyc();
        final var kyb = kyb();
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
        final var kyc = kyc();
        final var kyb = kyb();
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
        final var stats = stats();
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

    public ShortBillingProfileResponse toShortResponse(BillingProfileCoworkerRole role, boolean invitationAccepted) {
        final var stats = stats();
        return new ShortBillingProfileResponse()
                .id(id)
                .type(switch (type) {
                    case INDIVIDUAL -> onlydust.com.marketplace.api.contract.model.BillingProfileType.INDIVIDUAL;
                    case COMPANY -> onlydust.com.marketplace.api.contract.model.BillingProfileType.COMPANY;
                    case SELF_EMPLOYED -> onlydust.com.marketplace.api.contract.model.BillingProfileType.SELF_EMPLOYED;
                })
                .name(name)
                .enabled(enabled)
                .role(role)
                .invoiceMandateAccepted(isInvoiceMandateAccepted())
                .rewardCount(stats.rewardCount())
                .invoiceableRewardCount(stats.invoiceableRewardCount())
                .currentYearPaymentLimit(stats.currentYearPaymentLimit())
                .currentYearPaymentAmount(stats.currentYearPaymentAmount())
                .individualLimitReached(stats.individualLimitReached())
                .missingPayoutInfo(stats.missingPayoutInfo())
                .missingVerification(stats.missingVerification())
                .pendingInvitationResponse(!invitationAccepted)
                .requestableRewardCount(requestableRewardCount(role, invitationAccepted))
                .verificationBlocked(isVerificationBlocked());
    }

    private Integer requestableRewardCount(BillingProfileCoworkerRole role, boolean invitationAccepted) {
        return role == BillingProfileCoworkerRole.ADMIN && invitationAccepted ? stats().invoiceableRewardCount() : 0;
    }

    private Integer requestableRewardCount(Optional<AllBillingProfileUserReadEntity> user) {
        return user.map(u -> u.role() == BillingProfileCoworkerRole.ADMIN && u.invitationAccepted() ? stats().invoiceableRewardCount() : 0).orElse(null);
    }

    private boolean isInvoiceMandateAccepted() {
        return invoiceMandateAcceptedAt != null && invoiceMandateAcceptedAt.isAfter(invoiceMandateLatestVersionDate);
    }

    private boolean isVerificationBlocked() {
        return verificationStatus == VerificationStatus.REJECTED || verificationStatus == VerificationStatus.CLOSED;
    }

    private boolean isSwitchableToSelfEmployed() {
        return type == BillingProfileType.COMPANY && this.users.size() == 1;
    }

    public BillingProfileLinkResponse toBoLinkResponse() {
        return new BillingProfileLinkResponse()
                .id(id)
                .type(type)
                .subject(subject())
                ;
    }

    public String subject() {
        final var kyc = kyc();
        final var kyb = kyb();
        return kyc != null ? kyc.subject() : kyb != null ? kyb.subject() : null;
    }

    public BillingProfileResponse toBoResponse() {
        final var kyc = kyc();
        final var kyb = kyb();
        final var payoutInfo = payoutInfo();
        return new BillingProfileResponse()
                .id(id)
                .subject(subject())
                .type(type)
                .name(name)
                .verificationStatus(verificationStatus)
                .kyb(kyb == null ? null : kyb.toDto())
                .kyc(kyc == null ? null : kyc.toDto())
                .admins(users.stream().filter(u -> u.role() == BillingProfileCoworkerRole.ADMIN).map(AllBillingProfileUserReadEntity::user).map(AllUserReadEntity::toBoResponse).toList())
                .currentMonthRewardedAmounts(currentMonthRewards.stream()
                        .collect(groupingBy(r -> r.currency().id(),
                                mapping(RewardReadEntity::toTotalMoneyWithUsdEquivalentResponse,
                                        reducing(null, Arithmetic::sum))))
                        .values().stream().toList())
                .payoutInfos(payoutInfo == null ? null : payoutInfo.toBoResponse())
                ;
    }

    public onlydust.com.marketplace.api.contract.model.BillingProfileResponse toResponse() {
        final var kyc = kyc();
        final var kyb = kyb();
        final var stats = stats();
        return new onlydust.com.marketplace.api.contract.model.BillingProfileResponse()
                .id(id)
                .name(name)
                .type(map(type))
                .kyb(kyb == null ? null : kyb.toResponse())
                .kyc(kyc == null ? null : kyc.toResponse())
                .status(map(verificationStatus))
                .enabled(enabled)
                .currentYearPaymentLimit(stats.currentYearPaymentLimit)
                .currentYearPaymentAmount(stats.currentYearPaymentAmount)
                .invoiceMandateAccepted(isInvoiceMandateAccepted())
                .rewardCount(stats.rewardCount())
                .invoiceableRewardCount(stats.invoiceableRewardCount())
                .missingPayoutInfo(stats.missingPayoutInfo())
                .missingVerification(stats.missingVerification())
                .verificationBlocked(isVerificationBlocked())
                .individualLimitReached(stats.individualLimitReached())
                .isSwitchableToSelfEmployed(isSwitchableToSelfEmployed());
    }

    private onlydust.com.marketplace.api.contract.model.VerificationStatus map(VerificationStatus verificationStatus) {
        return switch (verificationStatus) {
            case NOT_STARTED -> onlydust.com.marketplace.api.contract.model.VerificationStatus.NOT_STARTED;
            case STARTED -> onlydust.com.marketplace.api.contract.model.VerificationStatus.STARTED;
            case UNDER_REVIEW -> onlydust.com.marketplace.api.contract.model.VerificationStatus.UNDER_REVIEW;
            case VERIFIED -> onlydust.com.marketplace.api.contract.model.VerificationStatus.VERIFIED;
            case REJECTED -> onlydust.com.marketplace.api.contract.model.VerificationStatus.REJECTED;
            case CLOSED -> onlydust.com.marketplace.api.contract.model.VerificationStatus.CLOSED;
        };
    }

    private onlydust.com.marketplace.api.contract.model.BillingProfileType map(BillingProfileType type) {
        return switch (type) {
            case INDIVIDUAL -> onlydust.com.marketplace.api.contract.model.BillingProfileType.INDIVIDUAL;
            case COMPANY -> onlydust.com.marketplace.api.contract.model.BillingProfileType.COMPANY;
            case SELF_EMPLOYED -> onlydust.com.marketplace.api.contract.model.BillingProfileType.SELF_EMPLOYED;
        };
    }


}
