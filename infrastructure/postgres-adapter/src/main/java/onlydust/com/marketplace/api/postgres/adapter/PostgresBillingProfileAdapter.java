package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileCoworkerView;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileRewardView;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileUserRightsView;
import onlydust.com.marketplace.accounting.domain.view.ShortContributorView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.BillingProfileUserQueryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.BillingProfileUserRightsQueryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.RewardViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.kernel.pagination.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.*;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class PostgresBillingProfileAdapter implements BillingProfileStoragePort {
    private final @NonNull BillingProfileRepository billingProfileRepository;
    private final @NonNull KybRepository kybRepository;
    private final @NonNull KycRepository kycRepository;
    private final @NonNull PayoutInfoRepository payoutInfoRepository;
    private final @NonNull WalletRepository walletRepository;
    private final @NonNull BillingProfileUserRepository billingProfileUserRepository;
    private final @NonNull BillingProfileUserViewRepository billingProfileUserViewRepository;
    private final @NonNull ChildrenKycRepository childrenKycRepository;
    private final @NonNull BillingProfileUserInvitationRepository billingProfileUserInvitationRepository;
    private final @NonNull PayoutPreferenceRepository payoutPreferenceRepository;
    private final @NonNull BankAccountRepository bankAccountRepository;
    private final @NonNull BillingProfileUserRightsViewRepository billingProfileUserRightsViewRepository;
    private final @NonNull RewardViewRepository rewardViewRepository;
    private final @NonNull RewardRepository rewardRepository;
    private final @NonNull UserRepository userRepository;
    private final @NonNull SumsubRejectionReasonRepository sumsubRejectionReasonRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean individualBillingProfileExistsByUserId(UserId ownerId) {
        return billingProfileRepository.individualBillingProfileExistsByUserId(ownerId.value());
    }

    @Override
    @Transactional
    public void save(BillingProfile billingProfile) {
        if (billingProfile instanceof IndividualBillingProfile individualBillingProfile)
            save(individualBillingProfile);
        else if (billingProfile instanceof SelfEmployedBillingProfile selfEmployedBillingProfile)
            save(selfEmployedBillingProfile);
        else if (billingProfile instanceof CompanyBillingProfile companyBillingProfile)
            save(companyBillingProfile);
        else
            throw new IllegalArgumentException("Unknown billing profile type: %s".formatted(billingProfile.getClass().getSimpleName()));

    }

    @Override
    @Transactional
    public void save(IndividualBillingProfile billingProfile) {
        // TODO cascade merge the KYC/KYB and remove flush
        billingProfileRepository.saveAndFlush(BillingProfileEntity.fromDomain(billingProfile));
        final Optional<KycEntity> optionalKycEntity = kycRepository.findByBillingProfileId(billingProfile.id().value());
        if (optionalKycEntity.isEmpty()) {
            kycRepository.saveAndFlush(KycEntity.fromDomain(billingProfile.kyc()));
        }
    }

    @Override
    @Transactional
    public void save(SelfEmployedBillingProfile billingProfile) {
        billingProfileRepository.saveAndFlush(BillingProfileEntity.fromDomain(billingProfile));
        final Optional<KybEntity> optionalKybEntity = kybRepository.findByBillingProfileId(billingProfile.id().value());
        if (optionalKybEntity.isEmpty()) {
            kybRepository.saveAndFlush(KybEntity.fromDomain(billingProfile.kyb()));
        }
    }

    @Override
    @Transactional
    public void save(CompanyBillingProfile billingProfile) {
        billingProfileRepository.saveAndFlush(BillingProfileEntity.fromDomain(billingProfile));
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
    public Optional<BillingProfile> findById(BillingProfile.Id billingProfileId) {
        return billingProfileRepository.findById(billingProfileId.value()).map(BillingProfileEntity::toDomain);
    }

    @Override
    @Transactional
    public void savePayoutInfoForBillingProfile(PayoutInfo payoutInfo, BillingProfile.Id billingProfileId) {
        payoutInfoRepository.findById(billingProfileId.value())
                .ifPresent(payoutInfoEntity -> walletRepository.deleteByBillingProfileId(billingProfileId.value()));
        payoutInfoRepository.save(PayoutInfoEntity.fromDomain(billingProfileId, payoutInfo));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BillingProfileCoworkerView> findCoworkersByBillingProfile(@NonNull BillingProfile.Id billingProfileId,
                                                                          @NonNull Set<BillingProfile.User.Role> roles,
                                                                          int pageIndex, int pageSize) {
        final var page = billingProfileUserViewRepository.findByBillingProfileId(billingProfileId.value(),
                roles.stream().map(Enum::toString).toList(),
                PageRequest.of(pageIndex, pageSize, Sort.by("user_id")));
        return Page.<BillingProfileCoworkerView>builder()
                .content(page.getContent().stream().map(BillingProfileUserQueryEntity::toView).toList())
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
        billingProfileRepository.updateBillingProfileVerificationStatus(billingProfileId.value(), status.name());
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
                .verificationStatus(verificationStatus)
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
                .role(role)
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
                .role(role)
                .joinedAt(acceptedAt)
                .build());
    }

    @Override
    @Transactional
    public void updateCoworkerRole(BillingProfile.Id billingProfileId, UserId userId, BillingProfile.User.Role role) {
        final var user = billingProfileUserRepository.findByBillingProfileIdAndUserId(billingProfileId.value(), userId.value())
                .orElseThrow(() -> notFound("User %s is not a member of billing profile %s".formatted(userId, billingProfileId)));
        billingProfileUserRepository.saveAndFlush(user.toBuilder().role(role).build());
    }

    @Override
    public void updateCoworkerInvitationRole(BillingProfile.Id billingProfileId, GithubUserId invitedUser, BillingProfile.User.Role role) {
        final var invitation = billingProfileUserInvitationRepository.findById(
                        new BillingProfileUserInvitationEntity.PrimaryKey(billingProfileId.value(), invitedUser.value()))
                .orElseThrow(() -> notFound("User %s is not invited to billing profile %s".formatted(invitedUser, billingProfileId)));

        billingProfileUserInvitationRepository.saveAndFlush(invitation.toBuilder().role(role).build());
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<BillingProfileCoworkerView> getInvitedCoworker(BillingProfile.Id billingProfileId, GithubUserId invitedGithubUserId) {
        return billingProfileUserViewRepository.findInvitedUserByBillingProfileIdAndGithubId(billingProfileId.value(), invitedGithubUserId.value())
                .map(BillingProfileUserQueryEntity::toView);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BillingProfileCoworkerView> getCoworker(BillingProfile.Id billingProfileId, GithubUserId invitedGithubUserId) {
        return billingProfileUserViewRepository.findUserByBillingProfileIdAndGithubId(billingProfileId.value(), invitedGithubUserId.value())
                .map(BillingProfileUserQueryEntity::toView);
    }

    @Override
    @Transactional
    public void deleteCoworker(BillingProfile.Id billingProfileId, UserId userId) {
        payoutPreferenceRepository.deleteAllByBillingProfileIdAndUserId(billingProfileId.value(), userId.value());
        final var rewardIds = rewardRepository.getRewardIdsToBeRemovedFromBillingProfileForUser(billingProfileId.value(), userId.value())
                .stream().map(RewardEntity::id).toList();
        rewardRepository.removeBillingProfileIdOf(rewardIds);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BillingProfileCoworkerView> findBillingProfileAdmin(UserId userId, BillingProfile.Id billingProfileId) {
        return billingProfileUserViewRepository.findBillingProfileAdminById(userId.value(), billingProfileId.value())
                .map(BillingProfileUserQueryEntity::toView);
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
    public Optional<BillingProfileUserRightsView> getUserRightsForBillingProfile(BillingProfile.Id billingProfileId, UserId userId) {
        return billingProfileUserRightsViewRepository.findForUserIdAndBillingProfileId(userId.value(), billingProfileId.value())
                .map(BillingProfileUserRightsQueryEntity::toDomain);
    }

    @Override
    @Transactional
    public void updateBillingProfileType(BillingProfile.Id billingProfileId, BillingProfile.Type type) {
        billingProfileRepository.updateBillingProfileType(billingProfileId.value(), type.name());
    }

    @Override
    @Transactional
    public List<BillingProfileRewardView> findInvoiceableRewardsForBillingProfile(BillingProfile.Id billingProfileId) {
        return rewardViewRepository.findByBillingProfileIdAndStatusStatus(billingProfileId.value(), RewardStatus.Input.PENDING_REQUEST)
                .stream().map(RewardViewEntity::toBillingProfileReward).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShortContributorView> getBillingProfileOwnerById(UserId ownerId) {
        return userRepository.findById(ownerId.value()).map(UserEntity::toShortContributorView);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PayoutInfo> getPayoutInfo(BillingProfile.Id billingProfileId) {
        return payoutInfoRepository.findById(billingProfileId.value()).map(PayoutInfoEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillingProfile> findAllByCreationDate(ZonedDateTime creationDate) {
        return billingProfileRepository.findAllByCreationDate(creationDate).stream()
                .map(BillingProfileEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> findExternalRejectionReason(String groupId, String buttonId, String label) {
        return sumsubRejectionReasonRepository
                .findByGroupIdAndButtonIdAndAssociatedRejectionLabel(groupId, buttonId, label)
                .map(SumsubRejectionReasonEntity::getDescription);
    }
}
