package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.CurrencyView;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.model.github.GithubUserIdentity;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Contributor;
import onlydust.com.marketplace.project.domain.model.UserProfile;
import onlydust.com.marketplace.project.domain.view.GithubUserWithTelegramView;
import onlydust.com.marketplace.project.domain.view.RewardDetailsView;
import onlydust.com.marketplace.project.domain.view.RewardItemView;

import java.time.ZonedDateTime;
import java.util.*;

public interface UserStoragePort {

    Optional<UserProfile> findProfileById(UUID userId);

    void saveProfile(UUID userId, UserProfile userProfile);

    Optional<AuthenticatedUser> getRegisteredUserByGithubId(Long githubId);

    Optional<GithubUserIdentity> getIndexedUserByGithubId(Long githubId);

    Optional<AuthenticatedUser> getRegisteredUserById(UUID userId);

    void updateOnboardingCompletionDate(UUID userId, Date date);

    void updateTermsAndConditionsAcceptanceDate(UUID userId, Date date);

    UUID acceptProjectLeaderInvitation(Long githubUserId, UUID projectId);

    RewardDetailsView findRewardById(UUID rewardId);

    Page<RewardItemView> findRewardItemsPageById(UUID rewardId, int pageIndex, int pageSize);

    List<Contributor> searchContributorsByLogin(Set<Long> reposIds, String login, int maxContributorCountToReturn);

    void saveProjectLead(UUID userId, UUID projectId);

    List<CurrencyView> listRewardCurrencies(Long githubUserId, List<UUID> administratedBillingProfileIds);

    List<AuthenticatedUser> getUsersLastSeenSince(ZonedDateTime since);

    void saveUsers(List<GithubUserIdentity> users);

    void saveUser(GithubUserIdentity user);

    void refreshUserRanksAndStats();

    void historizeUserRanks();

    Optional<GithubUserWithTelegramView> findGithubUserWithTelegram(UUID userId);

    List<UserId> findUserIdsRegisteredOnNotifyOnNewGoodFirstIssuesOnProject(UUID projectId);
}
