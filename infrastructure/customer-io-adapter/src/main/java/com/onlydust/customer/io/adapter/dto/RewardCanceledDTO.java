package com.onlydust.customer.io.adapter.dto;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.events.RewardCanceled;

import java.math.RoundingMode;

public record RewardCanceledDTO(@NonNull String amount, @NonNull String currency, @NonNull String rewardName, @NonNull String username) {

    public static RewardCanceledDTO fromEvent(@NonNull RewardCanceled rewardCanceled) {
        return new RewardCanceledDTO(rewardCanceled.shortReward().getAmount().setScale(1, RoundingMode.HALF_UP).toString(),
                rewardCanceled.shortReward().getCurrencyCode(), "%s - %s".formatted(rewardCanceled.shortReward().getId().pretty(),
                rewardCanceled.shortReward().getProjectName()), rewardCanceled.recipientGithubLogin());
    }
}
