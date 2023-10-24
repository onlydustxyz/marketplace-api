package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.domain.view.ProjectContributorsLinkView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectContributorViewEntity;

public interface ProjectContributorsMapper {

    static ProjectContributorsLinkView mapToDomainWithProjectLeadData(final ProjectContributorViewEntity contributorViewEntity) {
        return ProjectContributorsLinkView.builder()
                .avatarUrl(contributorViewEntity.getAvatarUrl())
                .login(contributorViewEntity.getLogin())
                .earned(contributorViewEntity.getEarned())
                .rewards(contributorViewEntity.getRewards())
                .totalToReward(contributorViewEntity.getTotalToReward())
                .issuesToRewardCount(contributorViewEntity.getIssuesToReward())
                .pullRequestsToRewardCount(contributorViewEntity.getPrsToReward())
                .codeReviewToRewardCount(contributorViewEntity.getCodeReviewsToReward())
                .contributionCount(contributorViewEntity.getContributionCount())
                .githubUserId(contributorViewEntity.getGithubUserId())
                .isRegistered(contributorViewEntity.isRegistered())
                .build();
    }

    static ProjectContributorsLinkView mapToDomainWithoutProjectLeadData(final ProjectContributorViewEntity contributorViewEntity) {
        return ProjectContributorsLinkView.builder()
                .avatarUrl(contributorViewEntity.getAvatarUrl())
                .login(contributorViewEntity.getLogin())
                .earned(contributorViewEntity.getEarned())
                .rewards(contributorViewEntity.getRewards())
                .contributionCount(contributorViewEntity.getContributionCount())
                .githubUserId(contributorViewEntity.getGithubUserId())
                .isRegistered(contributorViewEntity.isRegistered())
                .build();
    }

}
