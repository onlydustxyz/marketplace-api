package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.accounting.domain.view.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.BillingProfileUserRightsViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.BillingProfileUserViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.RewardViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ShortBillingProfileViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.kernel.pagination.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

import static java.time.ZonedDateTime.now;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class PostgresBillingProfileAdapter implements BillingProfileStoragePort {
    private final @NonNull GlobalSettingsRepository globalSettingsRepository;
    private final @NonNull BillingProfileRepository billingProfileRepository;
    private final @NonNull KybRepository kybRepository;
    private final @NonNull KycRepository kycRepository;
    private final @NonNull PayoutInfoRepository payoutInfoRepository;
    private final @NonNull PayoutInfoViewRepository payoutInfoViewRepository;
    private final @NonNull WalletRepository walletRepository;
    private final @NonNull BillingProfileUserRepository billingProfileUserRepository;
    private final @NonNull BillingProfileUserViewRepository billingProfileUserViewRepository;
    private final @NonNull ChildrenKycRepository childrenKycRepository;
    private final @NonNull BillingProfileUserInvitationRepository billingProfileUserInvitationRepository;
    private final @NonNull PayoutPreferenceRepository payoutPreferenceRepository;
    private final @NonNull BankAccountRepository bankAccountRepository;
    private final @NonNull ShortBillingProfileViewRepository shortBillingProfileViewRepository;
    private final @NonNull BillingProfileUserRightsViewRepository billingProfileUserRightsViewRepository;
    private final @NonNull RewardViewRepository rewardViewRepository;
    private final @NonNull RewardRepository rewardRepository;


    @Override
    @Transactional
    public void updateInvoiceMandateAcceptanceDate(@NonNull final BillingProfile.Id billingProfileId, @NonNull final ZonedDateTime acceptanceDate) {
        final var billingProfile = billingProfileRepository.findById(billingProfileId.value())
                .orElseThrow(() -> notFound("Billing profile %s not found".formatted(billingProfileId)));
        billingProfile.setInvoiceMandateAcceptedAt(Date.from(acceptanceDate.toInstant()));
        billingProfileRepository.saveAndFlush(billingProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShortBillingProfileView> findIndividualBillingProfileForUser(UserId ownerId) {
        return shortBillingProfileViewRepository.findBillingProfilesForUserId(ownerId.value(), List.of(BillingProfile.Type.INDIVIDUAL.name()))
                .stream().map(ShortBillingProfileViewEntity::toView).findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShortBillingProfileView> findAllBillingProfilesForUser(UserId userId) {
        final var invoiceMandateLatestVersionDate = globalSettingsRepository.get().getInvoiceMandateLatestVersionDate();
        final var billingProfiles = shortBillingProfileViewRepository.findBillingProfilesForUserId(userId.value(), List.of());
        final var billingProfilesInvitedOn = shortBillingProfileViewRepository.findBillingProfilesForUserIdInvited(userId.value());
        return Stream.concat(billingProfiles.stream(), billingProfilesInvitedOn.stream())
                .map(ShortBillingProfileViewEntity::toView)
                .peek(bp -> bp.setInvoiceMandateLatestVersionDate(invoiceMandateLatestVersionDate))
                .toList();
    }

    @Override
    @Transactional
    public void save(IndividualBillingProfile billingProfile) {
        billingProfileRepository.saveAndFlush(BillingProfileEntity.fromDomain(billingProfile, billingProfile.owner().id(), now()));
        final Optional<KycEntity> optionalKycEntity = kycRepository.findByBillingProfileId(billingProfile.id().value());
        if (optionalKycEntity.isEmpty()) {
            kycRepository.saveAndFlush(KycEntity.fromDomain(billingProfile.kyc()));
        }
    }

    @Override
    @Transactional
    public void save(SelfEmployedBillingProfile billingProfile) {
        billingProfileRepository.saveAndFlush(BillingProfileEntity.fromDomain(billingProfile, billingProfile.owner().id(), now()));
        final Optional<KybEntity> optionalKybEntity = kybRepository.findByBillingProfileId(billingProfile.id().value());
        if (optionalKybEntity.isEmpty()) {
            kybRepository.saveAndFlush(KybEntity.fromDomain(billingProfile.kyb()));
        }
    }

    @Override
    @Transactional
    public void save(CompanyBillingProfile billingProfile) {
        billingProfileRepository.saveAndFlush(BillingProfileEntity.fromDomain(billingProfile,
                billingProfile.members().stream().map(BillingProfile.User::id).toList().get(0), now()));
        final Optional<KybEntity> optionalKybEntity = kybRepository.findByBillingProfileId(billingProfile.id().value());
        if (optionalKybEntity.isEmpty()) {
            kybRepository.saveAndFlush(KybEntity.fromDomain(billingProfile.kyb()));
        }
    }

    @Override
    public void savePayoutPreference(BillingProfile.Id billingProfileId, UserId userId, ProjectId projectId) {
        payoutPreferenceRepository.saveAndFlush(PayoutPreferenceEntity.builder()
                .billingProfileId(billingProfileId.value())
                .id(PayoutPreferenceEntity.PrimaryKey.builder()
                        .userId(userId.value())
                        .projectId(projectId.value())
                        .build())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserMemberOf(BillingProfile.Id billingProfileId, UserId userId) {
        return billingProfileUserRepository.existsByBillingProfileIdAndUserId(billingProfileId.value(), userId.value());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAdmin(BillingProfile.Id billingProfileId, UserId userId) {
        return billingProfileUserRepository.findByBillingProfileIdAndUserId(billingProfileId.value(), userId.value())
                .map(billingProfileUserEntity -> billingProfileUserEntity.getRole().equals(BillingProfileUserEntity.Role.ADMIN))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BillingProfileView> findById(BillingProfile.Id billingProfileId) {
        final var invoiceMandateLatestVersionDate = globalSettingsRepository.get().getInvoiceMandateLatestVersionDate();

        return billingProfileRepository.findById(billingProfileId.value()).map(billingProfileEntity -> {
            final var billingProfileCustomData = shortBillingProfileViewRepository.findById(billingProfileId.value())
                    .orElseThrow(() -> notFound("Billing profile %s not found".formatted(billingProfileId)));
            return switch (billingProfileEntity.getType()) {
                case INDIVIDUAL -> {
                    final var individualBillingProfile = (IndividualBillingProfile) billingProfileEntity.toDomain();
                    BillingProfileView billingProfileView = BillingProfileView.builder()
                            .enabled(billingProfileEntity.getEnabled())
                            .type(BillingProfile.Type.INDIVIDUAL)
                            .id(billingProfileId)
                            .name(billingProfileEntity.getName())
                            .payoutInfo(isNull(billingProfileEntity.getPayoutInfo()) ? null : billingProfileEntity.getPayoutInfo().toDomain())
                            .verificationStatus(billingProfileEntity.getVerificationStatus().toDomain())
                            .missingPayoutInfo(billingProfileCustomData.getStats().missingPayoutInfo())
                            .missingVerification(billingProfileCustomData.getStats().missingVerification())
                            .rewardCount(billingProfileCustomData.getStats().rewardCount())
                            .invoiceableRewardCount(billingProfileCustomData.getStats().invoiceableRewardCount())
                            .invoiceMandateAcceptedAt(billingProfileEntity.getInvoiceMandateAcceptedAt())
                            .invoiceMandateLatestVersionDate(invoiceMandateLatestVersionDate)
                            .currentYearPaymentLimit(individualBillingProfile.currentYearPaymentLimit())
                            .currentYearPaymentAmount(PositiveAmount.of(billingProfileCustomData.getStats().currentYearPaymentAmount()))
                            .currentMonthRewardedAmounts(billingProfileCustomData.getCurrentMonthRewards().stream()
                                    .map(r -> new TotalMoneyView(r.amount(), r.currency().toDomain().toView(), r.statusData().amountUsdEquivalent()))
                                    .collect(groupingBy(TotalMoneyView::currency, reducing(null, TotalMoneyView::add)))
                                    .values().stream().toList())
                            .admins(billingProfileEntity.getUsers().stream().map(BillingProfileUserEntity::toView).toList())
                            .build();
                    final Optional<KycEntity> optionalKycEntity = kycRepository.findByBillingProfileId(billingProfileId.value());
                    if (optionalKycEntity.isPresent()) {
                        billingProfileView = billingProfileView.toBuilder()
                                .kyc(optionalKycEntity.get().toDomain())
                                .build();
                    }
                    yield billingProfileView;
                }
                case COMPANY -> {
                    BillingProfileView billingProfileView = BillingProfileView.builder().type(BillingProfile.Type.COMPANY)
                            .id(billingProfileId)
                            .name(billingProfileEntity.getName())
                            .enabled(billingProfileEntity.getEnabled())
                            .payoutInfo(isNull(billingProfileEntity.getPayoutInfo()) ? null : billingProfileEntity.getPayoutInfo().toDomain())
                            .verificationStatus(billingProfileEntity.getVerificationStatus().toDomain())
                            .missingPayoutInfo(billingProfileCustomData.getStats().missingPayoutInfo())
                            .missingVerification(billingProfileCustomData.getStats().missingVerification())
                            .rewardCount(billingProfileCustomData.getStats().rewardCount())
                            .invoiceableRewardCount(billingProfileCustomData.getStats().invoiceableRewardCount())
                            .invoiceMandateAcceptedAt(billingProfileEntity.getInvoiceMandateAcceptedAt())
                            .invoiceMandateLatestVersionDate(invoiceMandateLatestVersionDate)
                            .admins(billingProfileEntity.getUsers().stream().map(BillingProfileUserEntity::toView).toList())
                            .currentMonthRewardedAmounts(billingProfileCustomData.getCurrentMonthRewards().stream()
                                    .map(r -> new TotalMoneyView(r.amount(), r.currency().toDomain().toView(), r.statusData().amountUsdEquivalent()))
                                    .collect(groupingBy(TotalMoneyView::currency, reducing(null, TotalMoneyView::add)))
                                    .values().stream().toList())
                            .build();
                    final Optional<KybEntity> optionalKybEntity = kybRepository.findByBillingProfileId(billingProfileId.value());
                    if (optionalKybEntity.isPresent()) {
                        billingProfileView = billingProfileView.toBuilder()
                                .kyb(optionalKybEntity.get().toDomain())
                                .build();
                    }
                    yield billingProfileView;
                }
                case SELF_EMPLOYED -> {
                    BillingProfileView billingProfileView = BillingProfileView.builder().type(BillingProfile.Type.SELF_EMPLOYED)
                            .id(billingProfileId)
                            .name(billingProfileEntity.getName())
                            .enabled(billingProfileEntity.getEnabled())
                            .invoiceMandateAcceptedAt(billingProfileEntity.getInvoiceMandateAcceptedAt())
                            .invoiceMandateLatestVersionDate(invoiceMandateLatestVersionDate)
                            .payoutInfo(isNull(billingProfileEntity.getPayoutInfo()) ? null : billingProfileEntity.getPayoutInfo().toDomain())
                            .verificationStatus(billingProfileEntity.getVerificationStatus().toDomain())
                            .missingPayoutInfo(billingProfileCustomData.getStats().missingPayoutInfo())
                            .missingVerification(billingProfileCustomData.getStats().missingVerification())
                            .rewardCount(billingProfileCustomData.getStats().rewardCount())
                            .invoiceableRewardCount(billingProfileCustomData.getStats().invoiceableRewardCount())
                            .admins(billingProfileEntity.getUsers().stream().map(BillingProfileUserEntity::toView).toList())
                            .currentMonthRewardedAmounts(billingProfileCustomData.getCurrentMonthRewards().stream()
                                    .map(r -> new TotalMoneyView(r.amount(), r.currency().toDomain().toView(), r.statusData().amountUsdEquivalent()))
                                    .collect(groupingBy(TotalMoneyView::currency, reducing(null, TotalMoneyView::add)))
                                    .values().stream().toList())
                            .build();
                    final Optional<KybEntity> optionalKybEntity = kybRepository.findByBillingProfileId(billingProfileId.value());
                    if (optionalKybEntity.isPresent()) {
                        billingProfileView = billingProfileView.toBuilder()
                                .kyb(optionalKybEntity.get().toDomain())
                                .build();
                    }
                    yield billingProfileView;
                }
            };
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PayoutInfoView> findPayoutInfoByBillingProfile(BillingProfile.Id billingProfileId) {
        return payoutInfoViewRepository.findByBillingProfileId(billingProfileId.value()).map(PayoutInfoViewEntity::toDomain);
    }

    @Override
    @Transactional
    public void savePayoutInfoForBillingProfile(PayoutInfo payoutInfo, BillingProfile.Id billingProfileId) {
        payoutInfoRepository.findById(billingProfileId.value())
                .ifPresent(payoutInfoEntity -> walletRepository.deleteByBillingProfileId(billingProfileId.value()));
        payoutInfoRepository.save(PayoutInfoEntity.toEntity(billingProfileId, payoutInfo));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BillingProfileCoworkerView> findCoworkersByBillingProfile(@NonNull BillingProfile.Id billingProfileId,
                                                                          @NonNull Set<BillingProfile.User.Role> roles,
                                                                          int pageIndex, int pageSize) {
        final var page = billingProfileUserViewRepository.findByBillingProfileId(billingProfileId.value(),
                roles.stream().map(BillingProfileUserEntity.Role::fromDomain).map(Enum::toString).toList(),
                PageRequest.of(pageIndex, pageSize, Sort.by("user_id")));
        return Page.<BillingProfileCoworkerView>builder()
                .content(page.getContent().stream().map(BillingProfileUserViewEntity::toView).toList())
                .totalItemNumber(page.getNumberOfElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Kyc> findKycById(UUID id) {
        return kycRepository.findById(id).map(KycEntity::toDomain);
    }

    @Override
    @Transactional
    public void saveKyc(Kyc kyc) {
        kycRepository.saveAndFlush(KycEntity.fromDomain(kyc));
    }

    @Override
    @Transactional
    public void updateBillingProfileStatus(BillingProfile.Id billingProfileId, VerificationStatus status) {
        billingProfileRepository.updateBillingProfileVerificationStatus(billingProfileId.value(), VerificationStatusEntity.fromDomain(status).name());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Kyb> findKybById(UUID id) {
        return kybRepository.findById(id).map(KybEntity::toDomain);
    }

    @Override
    @Transactional
    public void saveKyb(Kyb kyb) {
        kybRepository.saveAndFlush(KybEntity.fromDomain(kyb));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VerificationStatus> findAllChildrenKycStatuesFromParentKyb(Kyb parentKyb) {
        return childrenKycRepository.findAllByParentApplicantId(parentKyb.getExternalApplicantId()).stream()
                .map(ChildrenKycEntity::getVerificationStatus)
                .map(VerificationStatusEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Kyb> findKybByParentExternalId(String parentExternalApplicantId) {
        return kybRepository.findByApplicantId(parentExternalApplicantId).map(KybEntity::toDomain);
    }

    @Override
    @Transactional
    public void saveChildrenKyc(String externalApplicantId, String parentExternalApplicantId, VerificationStatus verificationStatus) {
        childrenKycRepository.saveAndFlush(ChildrenKycEntity.builder()
                .applicantId(externalApplicantId)
                .parentApplicantId(parentExternalApplicantId)
                .verificationStatus(VerificationStatusEntity.fromDomain(verificationStatus))
                .build());
    }

    @Override
    @Transactional
    public void saveCoworkerInvitation(BillingProfile.Id billingProfileId, UserId invitedBy, GithubUserId invitedUser, BillingProfile.User.Role role,
                                       ZonedDateTime invitedAt) {
        billingProfileUserInvitationRepository.saveAndFlush(BillingProfileUserInvitationEntity.builder()
                .billingProfileId(billingProfileId.value())
                .invitedBy(invitedBy.value())
                .githubUserId(invitedUser.value())
                .invitedAt(Date.from(invitedAt.toInstant()))
                .role(BillingProfileUserEntity.Role.fromDomain(role))
                .accepted(false)
                .build());
    }

    @Override
    public void deleteCoworkerInvitation(BillingProfile.Id billingProfileId, GithubUserId invitedUser) {
        billingProfileUserInvitationRepository.deleteById(new BillingProfileUserInvitationEntity.PrimaryKey(billingProfileId.value(), invitedUser.value()));
    }

    @Override
    @Transactional
    public void acceptCoworkerInvitation(BillingProfile.Id billingProfileId, GithubUserId invitedGithubUserId) {
        billingProfileUserInvitationRepository.acceptInvitation(billingProfileId.value(), invitedGithubUserId.value());
    }

    @Override
    @Transactional
    public void saveCoworker(BillingProfile.Id billingProfileId, UserId invitedUser, BillingProfile.User.Role role, ZonedDateTime acceptedAt) {
        billingProfileUserRepository.saveAndFlush(BillingProfileUserEntity.builder()
                .billingProfileId(billingProfileId.value())
                .userId(invitedUser.value())
                .role(BillingProfileUserEntity.Role.fromDomain(role))
                .joinedAt(Date.from(acceptedAt.toInstant()))
                .build());
    }

    @Override
    @Transactional
    public void updateCoworkerRole(BillingProfile.Id billingProfileId, UserId userId, BillingProfile.User.Role role) {
        final var user = billingProfileUserRepository.findByBillingProfileIdAndUserId(billingProfileId.value(), userId.value())
                .orElseThrow(() -> notFound("User %s is not a member of billing profile %s".formatted(userId, billingProfileId)));
        billingProfileUserRepository.saveAndFlush(user.toBuilder().role(BillingProfileUserEntity.Role.fromDomain(role)).build());
    }

    @Override
    public void updateCoworkerInvitationRole(BillingProfile.Id billingProfileId, GithubUserId invitedUser, BillingProfile.User.Role role) {
        final var invitation = billingProfileUserInvitationRepository.findById(
                        new BillingProfileUserInvitationEntity.PrimaryKey(billingProfileId.value(), invitedUser.value()))
                .orElseThrow(() -> notFound("User %s is not invited to billing profile %s".formatted(invitedUser, billingProfileId)));

        billingProfileUserInvitationRepository.saveAndFlush(invitation.toBuilder().role(BillingProfileUserEntity.Role.fromDomain(role)).build());
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<BillingProfileCoworkerView> getInvitedCoworker(BillingProfile.Id billingProfileId, GithubUserId invitedGithubUserId) {
        return billingProfileUserViewRepository.findInvitedUserByBillingProfileIdAndGithubId(billingProfileId.value(), invitedGithubUserId.value())
                .map(BillingProfileUserViewEntity::toView);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BillingProfileCoworkerView> getCoworker(BillingProfile.Id billingProfileId, GithubUserId invitedGithubUserId) {
        return billingProfileUserViewRepository.findUserByBillingProfileIdAndGithubId(billingProfileId.value(), invitedGithubUserId.value())
                .map(BillingProfileUserViewEntity::toView);
    }

    @Override
    @Transactional
    public void deleteCoworker(BillingProfile.Id billingProfileId, UserId userId) {
        payoutPreferenceRepository.deleteAllByBillingProfileIdAndUserId(billingProfileId.value(), userId.value());
        final var rewardIds = rewardRepository.getRewardIdsToBeRemovedFromBillingProfileForUser(billingProfileId.value(), userId.value())
                .stream().map(RewardEntity::id).toList();
        rewardRepository.removeBillingProfileIdOf(rewardIds);
        billingProfileUserRepository.deleteById(new BillingProfileUserEntity.PrimaryKey(userId.value(), billingProfileId.value()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BillingProfileCoworkerView> findBillingProfileAdmin(UserId userId, BillingProfile.Id billingProfileId) {
        return billingProfileUserViewRepository.findBillingProfileAdminById(userId.value(), billingProfileId.value())
                .map(BillingProfileUserViewEntity::toView);
    }

    @Override
    @Transactional
    public void deleteBillingProfile(BillingProfile.Id billingProfileId) {
        kybRepository.findByBillingProfileId(billingProfileId.value())
                .ifPresent(kybEntity -> childrenKycRepository.deleteAllByParentApplicantId(kybEntity.applicantId()));
        kybRepository.deleteByBillingProfileId(billingProfileId.value());
        kycRepository.deleteByBillingProfileId(billingProfileId.value());
        billingProfileUserInvitationRepository.deleteAllByBillingProfileId(billingProfileId.value());
        payoutPreferenceRepository.deleteAllByBillingProfileId(billingProfileId.value());
        walletRepository.deleteByBillingProfileId(billingProfileId.value());
        bankAccountRepository.deleteByBillingProfileId(billingProfileId.value());
        billingProfileRepository.deleteById(billingProfileId.value());
    }

    @Override
    @Transactional
    public void updateEnableBillingProfile(BillingProfile.Id billingProfileId, Boolean enabled) {
        payoutPreferenceRepository.deleteAllByBillingProfileId(billingProfileId.value());
        billingProfileRepository.updateEnabled(billingProfileId.value(), enabled);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEnabled(BillingProfile.Id billingProfileId) {
        return billingProfileRepository.isBillingProfileEnabled(billingProfileId.value());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BillingProfileUserRightsView> getUserRightsForBillingProfile(BillingProfile.Id billingProfileId, UserId userId) {
        return billingProfileUserRightsViewRepository.findForUserIdAndBillingProfileId(userId.value(), billingProfileId.value())
                .map(BillingProfileUserRightsViewEntity::toDomain);
    }

    @Override
    @Transactional
    public void updateBillingProfileType(BillingProfile.Id billingProfileId, BillingProfile.Type type) {
        billingProfileRepository.updateBillingProfileType(billingProfileId.value(), type.name());
    }

    @Override
    @Transactional
    public List<BillingProfileRewardView> findInvoiceableRewardsForBillingProfile(BillingProfile.Id billingProfileId) {
        return rewardViewRepository.findByBillingProfileIdAndStatusStatus(billingProfileId.value(), RewardStatusEntity.Status.PENDING_REQUEST)
                .stream().map(RewardViewEntity::toBillingProfileReward).toList();
    }

    @Override
    public boolean isUserInvitedTo(BillingProfile.Id billingProfileId, GithubUserId githubUserId) {
        return billingProfileUserInvitationRepository.existsByBillingProfileIdAndGithubUserIdAndAcceptedIsFalse(billingProfileId.value(), githubUserId.value());
    }
}
