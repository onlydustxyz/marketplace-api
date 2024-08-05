package com.onlydust.customer.io.adapter.dto;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.notification.RewardsPaid;

public record RewardsPaidDTO(@NonNull String rewardsDetails, @NonNull String username) {

    public static RewardsPaidDTO fromEvent(String recipientLogin, RewardsPaid rewardsPaid) {
        return new RewardsPaidDTO(MailDTO.getRewardNames(rewardsPaid.shortRewards()), recipientLogin);
    }
}
