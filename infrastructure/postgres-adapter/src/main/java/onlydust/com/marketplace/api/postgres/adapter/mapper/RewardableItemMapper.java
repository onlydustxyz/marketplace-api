package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.domain.model.ContributionType;
import onlydust.com.marketplace.api.domain.view.CodeReviewOutcome;
import onlydust.com.marketplace.api.domain.view.RewardableItemView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.RewardableItemViewEntity;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.postgres.adapter.mapper.RewardMapper.githubStatusToDomain;

public interface RewardableItemMapper {
    static RewardableItemView itemToDomain(RewardableItemViewEntity rewardableItemViewEntity) {
        return RewardableItemView.builder()
                .id(rewardableItemViewEntity.getId())
                .createdAt(rewardableItemViewEntity.getCreatedAt())
                .title(rewardableItemViewEntity.getTitle())
                .githubUrl(rewardableItemViewEntity.getGithubUrl())
                .commentsCount(rewardableItemViewEntity.getCommentsCount())
                .commitsCount(rewardableItemViewEntity.getCommitsCount())
                .userCommitsCount(rewardableItemViewEntity.getUserCommitsCount())
                .number(rewardableItemViewEntity.getNumber())
                .lastUpdateAt(rewardableItemViewEntity.getCompletedAt())
                .type(switch (rewardableItemViewEntity.getType()) {
                    case ISSUE -> ContributionType.ISSUE;
                    case PULL_REQUEST -> ContributionType.PULL_REQUEST;
                    case CODE_REVIEW -> ContributionType.CODE_REVIEW;
                })
                .repoName(rewardableItemViewEntity.getRepoName())
                .status(githubStatusToDomain(rewardableItemViewEntity.getDraft(), rewardableItemViewEntity.getStatus()))
                .outcome(isNull(rewardableItemViewEntity.getOutcome()) ? null :
                        switch (rewardableItemViewEntity.getOutcome()) {
                            case APPROVED -> CodeReviewOutcome.approved;
                            case CHANGE_REQUESTED -> CodeReviewOutcome.changeRequested;
                            default -> null;
                        })
                .build();
    }
}
