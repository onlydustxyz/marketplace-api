package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.gateway.DateProvider;
import onlydust.com.marketplace.api.domain.model.*;
import onlydust.com.marketplace.api.domain.port.input.UserFacadePort;
import onlydust.com.marketplace.api.domain.port.output.UserStoragePort;
import onlydust.com.marketplace.api.domain.view.*;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class UserService implements UserFacadePort {

    private final UserStoragePort userStoragePort;
    private final DateProvider dateProvider;

    @Override
    public User getUserByGithubIdentity(GithubUserIdentity githubUserIdentity) {
        return userStoragePort
                .getUserByGithubId(githubUserIdentity.getGithubUserId())
                .map(user -> {
                    final UserPayoutInformation payoutInformationById =
                            userStoragePort.getPayoutInformationById(user.getId());
                    user.setHasValidPayoutInfos(payoutInformationById.isValid());
                    return user;
                })
                .orElseGet(() -> {
                    final var user = User.builder()
                            .id(UUID.randomUUID())
                            .roles(List.of(UserRole.USER))
                            .githubUserId(githubUserIdentity.getGithubUserId())
                            .avatarUrl(githubUserIdentity.getGithubAvatarUrl())
                            .login(githubUserIdentity.getGithubLogin())
                            .build();
                    userStoragePort.createUser(user);
                    return user;
                });
    }

    @Override
    public UserProfileView getProfileById(UUID userId) {
        return userStoragePort.getProfileById(userId);
    }

    @Override
    public UserProfileView getProfileById(Long githubUserId) {
        return userStoragePort.getProfileById(githubUserId);
    }

    @Override
    public UserProfileView getProfileByLogin(String githubLogin) {
        return userStoragePort.getProfileByLogin(githubLogin);
    }

    @Override
    public UserProfileView updateProfile(UUID userId, UserProfile userProfile) {
        userStoragePort.saveProfile(userId, userProfile);
        return userStoragePort.getProfileById(userId);
    }

    @Override
    public UserPayoutInformation getPayoutInformationForUserId(UUID userId) {
        return userStoragePort.getPayoutInformationById(userId);
    }

    @Override
    public UserPayoutInformation updatePayoutInformation(UUID userId, UserPayoutInformation userPayoutInformation) {
        userPayoutInformation.validate();
        return userStoragePort.savePayoutInformationForUserId(userId, userPayoutInformation);
    }

    @Override
    public void markUserAsOnboarded(UUID userId) {
        userStoragePort.updateOnboardingWizardDisplayDate(userId, dateProvider.now());
    }

    @Override
    public void updateTermsAndConditionsAcceptanceDate(UUID userId) {
        userStoragePort.updateTermsAndConditionsAcceptanceDate(userId, dateProvider.now());
    }

    @Override
    public void acceptInvitationToLeadProject(Long githubUserId, UUID projectId) {
        userStoragePort.acceptProjectLeaderInvitation(githubUserId, projectId);
    }

    @Override
    public void applyOnProject(UUID userId, UUID projectId) {
        userStoragePort.createApplicationOnProject(userId, projectId);
    }

    @Override
    public Page<UserRewardView> getRewardsForUserId(UUID userId, int pageIndex, int pageSize,
                                                    UserRewardView.SortBy sortBy, SortDirection sortDirection) {
        return userStoragePort.findRewardsForUserId(userId, pageIndex, pageSize, sortBy, sortDirection);
    }

    @Override
    public UserRewardTotalAmountsView getRewardTotalAmountsForUserId(UUID userId) {
        return userStoragePort.findRewardTotalAmountsForUserId(userId);
    }

    @Override
    public RewardView getRewardByIdForRecipientId(UUID rewardId, Long recipientId) {
        final RewardView reward = userStoragePort.findRewardById(rewardId);
        if (!reward.getTo().getGithubUserId().equals(recipientId)) {
            throw OnlyDustException.forbidden("Only recipient user can read it's own reward");
        }
        return reward;
    }

    @Override
    public Page<RewardItemView> getRewardItemsPageByIdForRecipientId(UUID rewardId, Long recipientId, int pageIndex,
                                                                     int pageSize) {
        final Page<RewardItemView> page = userStoragePort.findRewardItemsPageById(rewardId, pageIndex, pageSize);
        if (page.getContent().stream().anyMatch(rewardItemView -> !rewardItemView.getRecipientId().equals(recipientId))) {
            throw OnlyDustException.forbidden("Only recipient user can read it's own reward");
        }
        return page;
    }

    @Override
    public List<UserRewardView> getPendingInvoiceRewardsForRecipientId(Long githubUserId) {
        return userStoragePort.findPendingInvoiceRewardsForRecipientId(githubUserId);
    }
}
