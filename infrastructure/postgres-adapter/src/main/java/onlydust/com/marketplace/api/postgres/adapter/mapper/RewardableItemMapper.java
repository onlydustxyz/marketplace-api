package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.project.domain.model.ContributionType;
import onlydust.com.marketplace.project.domain.view.ContributorLinkView;
import onlydust.com.marketplace.project.domain.view.RewardableItemView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.RewardableItemViewEntity;

import static onlydust.com.marketplace.api.postgres.adapter.mapper.RewardMapper.githubStatusToDomain;

public interface RewardableItemMapper {
    static RewardableItemView itemToDomain(RewardableItemViewEntity rewardableItemViewEntity) {
        return RewardableItemView.builder()
                .id(rewardableItemViewEntity.getId())
                .contributionId(rewardableItemViewEntity.getContributionId())
                .createdAt(rewardableItemViewEntity.getCreatedAt())
                .title(rewardableItemViewEntity.getTitle())
                .githubUrl(rewardableItemViewEntity.getGithubUrl())
                .commentsCount(rewardableItemViewEntity.getCommentsCount())
                .commitsCount(rewardableItemViewEntity.getCommitsCount())
                .userCommitsCount(rewardableItemViewEntity.getUserCommitsCount())
                .number(rewardableItemViewEntity.getNumber())
                .ignored(rewardableItemViewEntity.getIgnored())
                .completedAt(rewardableItemViewEntity.getCompletedAt())
                .type(switch (rewardableItemViewEntity.getType()) {
                    case ISSUE -> ContributionType.ISSUE;
                    case PULL_REQUEST -> ContributionType.PULL_REQUEST;
                    case CODE_REVIEW -> ContributionType.CODE_REVIEW;
                })
                .repoName(rewardableItemViewEntity.getRepoName())
                .repoId(rewardableItemViewEntity.getRepoId())
                .status(githubStatusToDomain(rewardableItemViewEntity.getStatus()))
                .githubBody(rewardableItemViewEntity.getGithubBody())
                .githubAuthor(ContributorLinkView.builder()
                        .githubUserId(rewardableItemViewEntity.getGithubAuthorId())
                        .login(rewardableItemViewEntity.getGithubAuthorLogin())
                        .url(rewardableItemViewEntity.getGithubAuthorHtmlUrl())
                        .avatarUrl(rewardableItemViewEntity.getGithubAuthorAvatarUrl())
                        .build())
                .build();
    }
}
