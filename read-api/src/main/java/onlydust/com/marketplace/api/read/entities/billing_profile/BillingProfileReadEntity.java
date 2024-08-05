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
import onlydust.com.marketplace.api.read.entities.reward.RewardReadEntity;
import onlydust.com.marketplace.api.read.entities.user.AllUserReadEntity;
import onlydust.com.marketplace.api.read.utils.Arithmetic;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "billingProfileId")
    PayoutInfoReadEntity payoutInfo;

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
        return kyc != null ? kyc.subject() : kyb != null ? kyb.subject() : null;
    }

    public BillingProfileResponse toBoResponse() {
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

    /*
    final var response = new BillingProfileResponse();
        response.setId(view.getId().value());
        response.setName(view.getName());
        response.setType(map(view.getType()));
        response.setKyb(isNull(view.getKyb()) ? null : kybToResponse(view.getKyb()));
        response.setKyc(isNull(view.getKyc()) ? null : kycToResponse(view.getKyc()));
        response.setStatus(verificationStatusToResponse(view.getVerificationStatus()));
        response.setEnabled(view.getEnabled());
        response.setCurrentYearPaymentLimit(isNull(view.getCurrentYearPaymentLimit()) ? null : view.getCurrentYearPaymentLimit().getValue());
        response.setCurrentYearPaymentAmount(isNull(view.getCurrentYearPaymentAmount()) ? null : view.getCurrentYearPaymentAmount().getValue());
        response.setInvoiceMandateAccepted(view.isInvoiceMandateAccepted());
        response.setRewardCount(view.getRewardCount());
        response.setInvoiceableRewardCount(view.getInvoiceableRewardCount());
        response.setMissingPayoutInfo(view.getMissingPayoutInfo());
        response.setMissingVerification(view.getMissingVerification());
        response.setVerificationBlocked(view.isVerificationBlocked());
        response.setIndividualLimitReached(view.getIndividualLimitReached());
        response.setMe(isNull(view.getMe()) ? null :
                new BillingProfileResponseMe()
                        .canLeave(view.getMe().canLeave())
                        .canDelete(view.getMe().canDelete())
                        .role(mapRole(view.getMe().role()))
                        .invitation(isNull(view.getMe().invitation()) ? null :
                                new BillingProfileCoworkerInvitation()
                                        .invitedBy(new ContributorResponse()
                                                .avatarUrl(view.getMe().invitation().githubAvatarUrl())
                                                .login(view.getMe().invitation().githubLogin())
                                                .githubUserId(view.getMe().invitation().githubUserId().value()))
                                        .role(mapRole(view.getMe().invitation().role()))
                                        .invitedAt(view.getMe().invitation().invitedAt())
                        )
        );
        response.setIsSwitchableToSelfEmployed(view.isSwitchableToSelfEmployed());
        return response;
     */
}
