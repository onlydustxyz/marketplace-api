package com.onlydust.customer.io.adapter.dto;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.events.RewardCanceled;

import java.math.RoundingMode;

public record RewardCanceledDTO(@NonNull String totalUsdAmount, @NonNull String rewardName, @NonNull String userName) {

    public static RewardCanceledDTO fromEvent(@NonNull RewardCanceled rewardCanceled) {
        return new RewardCanceledDTO(rewardCanceled.shortReward().getDollarsEquivalent().setScale(1, RoundingMode.HALF_UP).toString(),
                "%s - %s".formatted(rewardCanceled.shortReward().getId().pretty(), rewardCanceled.shortReward().getProjectName()),
                rewardCanceled.recipientGithubLogin());
    }
}
