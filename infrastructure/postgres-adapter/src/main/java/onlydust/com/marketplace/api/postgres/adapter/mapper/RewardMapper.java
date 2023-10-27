package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.domain.model.ContributionStatus;
import onlydust.com.marketplace.api.domain.model.ContributionType;
import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.model.GithubUserIdentity;
import onlydust.com.marketplace.api.domain.view.RewardItemView;
import onlydust.com.marketplace.api.domain.view.RewardView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.RewardItemViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.RewardViewEntity;

public interface RewardMapper {
    static RewardView projectRewardToDomain(RewardViewEntity rewardViewEntityByd) {
        return RewardView.builder()
                .id(rewardViewEntityByd.getId())
                .to(GithubUserIdentity.builder()
                        .githubAvatarUrl(rewardViewEntityByd.getRecipientAvatarUrl())
                        .githubLogin(rewardViewEntityByd.getRecipientLogin())
                        .githubUserId(rewardViewEntityByd.getRecipientId())
                        .build())
                .amount(rewardViewEntityByd.getAmount())
                .createdAt(rewardViewEntityByd.getRequestedAt())
                .processedAt(rewardViewEntityByd.getProcessedAt())
                .currency(switch (rewardViewEntityByd.getCurrency()) {
                    case op -> Currency.Op;
                    case apt -> Currency.Apt;
                    case eth -> Currency.Eth;
                    case usd -> Currency.Usd;
                    case stark -> Currency.Stark;
                })
                .dollarsEquivalent(rewardViewEntityByd.getDollarsEquivalent())
                .status(switch (rewardViewEntityByd.getStatus()) {
                    case "PENDING_SIGNUP" -> RewardView.Status.pendingSignup;
                    case "COMPLETE" -> RewardView.Status.complete;
                    default -> RewardView.Status.processing;
                })
                .from(GithubUserIdentity.builder()
                        .githubUserId(rewardViewEntityByd.getRequestorId())
                        .githubLogin(rewardViewEntityByd.getRequestorLogin())
                        .githubAvatarUrl(rewardViewEntityByd.getRequestorAvatarUrl())
                        .build())
                .build();
    }

    static RewardItemView itemToDomain(RewardItemViewEntity rewardItemViewEntity) {
        return RewardItemView.builder()
                .id(rewardItemViewEntity.getId())
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
                .number(rewardItemViewEntity.getUserCommitsCount())
                .lastUpdateAt(rewardItemViewEntity.getCompletedAt())
                .type(switch (rewardItemViewEntity.getType()) {
                    case issue -> ContributionType.ISSUE;
                    case pull_request -> ContributionType.PULL_REQUEST;
                    case code_review -> ContributionType.CODE_REVIEW;
                })
                .repoName(rewardItemViewEntity.getRepoName())
                .authorLogin(rewardItemViewEntity.getAuthorLogin())
                .status(switch (rewardItemViewEntity.getStatus()) {
                    case canceled -> ContributionStatus.CANCELLED;
                    case complete -> ContributionStatus.COMPLETED;
                    case in_progress -> ContributionStatus.IN_PROGRESS;
                })
                .build();
    }
}
