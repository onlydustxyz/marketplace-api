package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.CurrencyView;
import onlydust.com.marketplace.kernel.model.ProjectId;
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

    Optional<UserProfile> findProfileById(UserId userId);

    void saveProfile(UserId userId, UserProfile userProfile);

    Optional<AuthenticatedUser> getRegisteredUserByGithubId(Long githubId);

    Optional<GithubUserIdentity> getIndexedUserByGithubId(Long githubId);

    Optional<AuthenticatedUser> getRegisteredUserById(UserId userId);

    void updateOnboardingCompletionDate(UserId userId, Date date);

    void updateTermsAndConditionsAcceptanceDate(UserId userId, Date date);

    void acceptProjectLeaderInvitation(Long githubUserId, ProjectId projectId);

    RewardDetailsView findRewardById(UUID rewardId);

    Page<RewardItemView> findRewardItemsPageById(UUID rewardId, int pageIndex, int pageSize);

    List<Contributor> searchContributorsByLogin(Set<Long> reposIds, String login, int maxContributorCountToReturn);

    void saveProjectLead(UserId userId, ProjectId projectId);

    List<CurrencyView> listRewardCurrencies(Long githubUserId, List<UUID> administratedBillingProfileIds);

    List<AuthenticatedUser> getUsersLastSeenSince(ZonedDateTime since);

    void saveUsers(List<GithubUserIdentity> users);

    void saveUser(GithubUserIdentity user);

    void refreshUserRanksAndStats();

    void historizeUserRanks();

    Optional<GithubUserWithTelegramView> findGithubUserWithTelegram(UserId userId);

    List<UserId> findUserIdsRegisteredOnNotifyOnNewGoodFirstIssuesOnProject(ProjectId projectId);
}
