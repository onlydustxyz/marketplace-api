package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.RewardableItemQueryEntity;
import onlydust.com.marketplace.project.domain.model.ContributionType;
import onlydust.com.marketplace.project.domain.view.ContributorLinkView;
import onlydust.com.marketplace.project.domain.view.RewardableItemView;

import static onlydust.com.marketplace.api.postgres.adapter.mapper.RewardMapper.githubStatusToDomain;

public interface RewardableItemMapper {
    static RewardableItemView itemToDomain(RewardableItemQueryEntity rewardableItemQueryEntity) {
        return RewardableItemView.builder()
                .id(rewardableItemQueryEntity.getId())
                .contributionId(rewardableItemQueryEntity.getContributionId())
                .createdAt(rewardableItemQueryEntity.getCreatedAt())
                .title(rewardableItemQueryEntity.getTitle())
                .githubUrl(rewardableItemQueryEntity.getGithubUrl())
                .commentsCount(rewardableItemQueryEntity.getCommentsCount())
                .commitsCount(rewardableItemQueryEntity.getCommitsCount())
                .userCommitsCount(rewardableItemQueryEntity.getUserCommitsCount())
                .number(rewardableItemQueryEntity.getNumber())
                .ignored(rewardableItemQueryEntity.getIgnored())
                .completedAt(rewardableItemQueryEntity.getCompletedAt())
                .type(switch (rewardableItemQueryEntity.getType()) {
                    case ISSUE -> ContributionType.ISSUE;
                    case PULL_REQUEST -> ContributionType.PULL_REQUEST;
                    case CODE_REVIEW -> ContributionType.CODE_REVIEW;
                })
                .repoName(rewardableItemQueryEntity.getRepoName())
                .repoId(rewardableItemQueryEntity.getRepoId())
                .status(githubStatusToDomain(rewardableItemQueryEntity.getStatus()))
                .githubBody(rewardableItemQueryEntity.getGithubBody())
                .githubAuthor(ContributorLinkView.builder()
                        .githubUserId(rewardableItemQueryEntity.getGithubAuthorId())
                        .login(rewardableItemQueryEntity.getGithubAuthorLogin())
                        .avatarUrl(rewardableItemQueryEntity.getGithubAuthorAvatarUrl())
                        .build())
                .build();
    }
}
