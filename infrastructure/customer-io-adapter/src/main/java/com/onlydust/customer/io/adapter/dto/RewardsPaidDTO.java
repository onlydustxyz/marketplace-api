package com.onlydust.customer.io.adapter.dto;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.events.RewardsPaid;

public record RewardsPaidDTO(@NonNull String rewardsDetails, @NonNull String username) {

    public static RewardsPaidDTO fromEvent(RewardsPaid rewardsPaid) {
        return new RewardsPaidDTO(MailDTO.getRewardNames(rewardsPaid.shortRewards()), rewardsPaid.recipientGithubLogin());
    }
}
