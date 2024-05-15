package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectContributorQueryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserProfileQueryEntity;
import onlydust.com.marketplace.project.domain.view.ProjectContributorsLinkView;
import onlydust.com.marketplace.project.domain.view.TotalsEarned;

import java.util.List;

import static java.util.Objects.isNull;

public interface ProjectContributorsMapper {

    static ProjectContributorsLinkView mapToDomainWithProjectLeadData(final ProjectContributorQueryEntity contributorViewEntity) {
        return ProjectContributorsLinkView.builder()
                .avatarUrl(contributorViewEntity.getAvatarUrl())
                .login(contributorViewEntity.getLogin())
                .earned(mapAmountsEntityToDomain(contributorViewEntity))
                .rewards(contributorViewEntity.getRewards())
                .totalToReward(contributorViewEntity.getTotalToReward())
                .issuesToRewardCount(contributorViewEntity.getIssuesToReward())
                .pullRequestsToRewardCount(contributorViewEntity.getPrsToReward())
                .codeReviewToRewardCount(contributorViewEntity.getCodeReviewsToReward())
                .contributionCount(contributorViewEntity.getContributionCount())
                .githubUserId(contributorViewEntity.getGithubUserId())
                .isRegistered(contributorViewEntity.isRegistered())
                .isHidden(contributorViewEntity.isHidden())
                .build();
    }

    static ProjectContributorsLinkView mapToDomainWithoutProjectLeadData(final ProjectContributorQueryEntity contributorViewEntity) {
        return ProjectContributorsLinkView.builder()
                .avatarUrl(contributorViewEntity.getAvatarUrl())
                .login(contributorViewEntity.getLogin())
                .earned(mapAmountsEntityToDomain(contributorViewEntity))
                .rewards(contributorViewEntity.getRewards())
                .contributionCount(contributorViewEntity.getContributionCount())
                .githubUserId(contributorViewEntity.getGithubUserId())
                .isRegistered(contributorViewEntity.isRegistered())
                .isHidden(false)
                .build();
    }

    private static TotalsEarned mapAmountsEntityToDomain(ProjectContributorQueryEntity contributorViewEntity) {
        return new TotalsEarned(isNull(contributorViewEntity.getTotalEarnedPerCurrencies())
                ? List.of()
                : contributorViewEntity.getTotalEarnedPerCurrencies().stream().map(UserProfileQueryEntity.TotalEarnedPerCurrency::toDomain).toList());
    }

}
