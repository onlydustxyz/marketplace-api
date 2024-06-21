package onlydust.com.marketplace.project.domain.port.output;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.CurrencyView;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.view.RewardDetailsView;
import onlydust.com.marketplace.project.domain.view.RewardItemView;
import onlydust.com.marketplace.project.domain.view.UserProfileView;

import java.time.ZonedDateTime;
import java.util.*;

public interface UserStoragePort {
    UserProfileView getProfileById(UUID userId);

    void saveProfile(UUID userId, UserProfile userProfile);

    Optional<User> getUserByGithubId(Long githubId);

    Optional<User> getUserById(UUID userId);

    User createUser(User user);

    void updateUserLastSeenAt(UUID userId, Date lastSeenAt);

    void updateOnboardingWizardDisplayDate(UUID userId, Date date);

    void updateTermsAndConditionsAcceptanceDate(UUID userId, Date date);

    UUID acceptProjectLeaderInvitation(Long githubUserId, UUID projectId);

    void save(@NonNull Application... applications);

    RewardDetailsView findRewardById(UUID rewardId);

    Page<RewardItemView> findRewardItemsPageById(UUID rewardId, int pageIndex, int pageSize);

    List<Contributor> searchContributorsByLogin(Set<Long> reposIds, String login, int maxContributorCountToReturn);

    void saveProjectLead(UUID userId, UUID projectId);

    List<CurrencyView> listRewardCurrencies(Long githubUserId, List<UUID> administratedBillingProfileIds);

    List<User> getUsersLastSeenSince(ZonedDateTime since);

    void saveUsers(List<User> users);

    void saveUser(User user);

    void refreshUserRanksAndStats();

    void historizeUserRanks();

    Optional<Application> findApplication(Application.Id id);

    Optional<Application> findApplication(Long applicantId, UUID projectId, GithubIssue.Id issueId);

    List<Application> findApplications(Long applicantId, GithubIssue.Id issueId);

    List<Application> findApplications(GithubComment.Id commentId);

    void deleteApplications(Application.Id... applicationIds);

    void deleteApplicationsByIssueId(GithubIssue.Id issueId);

    void deleteObsoleteApplications();

    List<ScoredApplication> findScoredApplications(Long applicantId, GithubIssue.Id issueId);

    Optional<ScoredApplication> findScoredApplication(Application.Id id);
}
