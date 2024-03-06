package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.RewardItemViewEntity;
import onlydust.com.marketplace.project.domain.model.ContributionType;
import onlydust.com.marketplace.project.domain.view.ProjectRewardView;
import onlydust.com.marketplace.project.domain.view.RewardItemStatus;
import onlydust.com.marketplace.project.domain.view.RewardItemView;
import onlydust.com.marketplace.project.domain.view.UserRewardView;

public interface RewardMapper {
    static RewardItemView itemToDomain(RewardItemViewEntity rewardItemViewEntity) {
        return RewardItemView.builder()
                .id(rewardItemViewEntity.getId())
                .contributionId(rewardItemViewEntity.getContributionId())
                .recipientId(rewardItemViewEntity.getRecipientId())
                .authorAvatarUrl(rewardItemViewEntity.getAuthorAvatarUrl())
                .createdAt(rewardItemViewEntity.getCreatedAt())
                .title(rewardItemViewEntity.getTitle())
                .authorGithubUrl(rewardItemViewEntity.getAuthorAvatarUrl())
                .githubAuthorId(rewardItemViewEntity.getAuthorId())
                .githubUrl(rewardItemViewEntity.getGithubUrl())
                .commentsCount(rewardItemViewEntity.getCommentsCount())
                .commitsCount(rewardItemViewEntity.getCommitsCount())
                .userCommitsCount(rewardItemViewEntity.getUserCommitsCount())
                .number(rewardItemViewEntity.getNumber())
                .completedAt(rewardItemViewEntity.getCompletedAt())
                .type(switch (rewardItemViewEntity.getType()) {
                    case ISSUE -> ContributionType.ISSUE;
                    case PULL_REQUEST -> ContributionType.PULL_REQUEST;
                    case CODE_REVIEW -> ContributionType.CODE_REVIEW;
                })
                .repoName(rewardItemViewEntity.getRepoName())
                .authorLogin(rewardItemViewEntity.getAuthorLogin())
                .status(githubStatusToDomain(rewardItemViewEntity.getStatus()))
                .githubBody(rewardItemViewEntity.getGithubBody())
                .build();
    }

    static RewardItemStatus githubStatusToDomain(final String status) {
        return switch (status) {
            case "COMPLETED" -> RewardItemStatus.COMPLETED;
            case "CANCELLED" -> RewardItemStatus.CANCELLED;
            case "CLOSED" -> RewardItemStatus.CLOSED;
            case "MERGED" -> RewardItemStatus.MERGED;
            case "DRAFT" -> RewardItemStatus.DRAFT;
            case "PENDING" -> RewardItemStatus.PENDING;
            case "COMMENTED" -> RewardItemStatus.COMMENTED;
            case "APPROVED" -> RewardItemStatus.APPROVED;
            case "CHANGES_REQUESTED" -> RewardItemStatus.CHANGES_REQUESTED;
            case "DISMISSED" -> RewardItemStatus.DISMISSED;
            default -> RewardItemStatus.OPEN;
        };
    }

    static UserRewardView.Status mapStatusForUser(String status) {
        return switch (status) {
            case "PENDING_INVOICE" -> UserRewardView.Status.pendingInvoice;
            case "COMPLETE" -> UserRewardView.Status.complete;
            case "MISSING_PAYOUT_INFO" -> UserRewardView.Status.missingPayoutInfo;
            case "PENDING_VERIFICATION" -> UserRewardView.Status.pendingVerification;
//    TODO        case "PENDING_CONTRIBUTOR" -> UserRewardView.Status.pendingContributor;
//     TODO       case "PENDING_SIGNUP" -> UserRewardView.Status.pendingContributor;
            case "LOCKED" -> UserRewardView.Status.locked;
            default -> UserRewardView.Status.processing;
        };
    }

    static ProjectRewardView.Status mapStatusForProject(String status) {
        return switch (status) {
            case "PENDING_SIGNUP" -> ProjectRewardView.Status.pendingSignup;
            case "PENDING_CONTRIBUTOR" -> ProjectRewardView.Status.pendingContributor;
            case "COMPLETE" -> ProjectRewardView.Status.complete;
            case "LOCKED" -> ProjectRewardView.Status.locked;
            default -> ProjectRewardView.Status.processing;
        };
    }
}
