package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.model.GithubUserIdentity;
import onlydust.com.marketplace.api.domain.view.RewardView;
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
}
