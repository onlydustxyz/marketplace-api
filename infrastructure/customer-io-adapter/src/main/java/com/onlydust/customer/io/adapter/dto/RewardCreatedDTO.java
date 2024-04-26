package com.onlydust.customer.io.adapter.dto;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.events.RewardCreated;

import java.math.RoundingMode;

public record RewardCreatedDTO(@NonNull String username, @NonNull String projectName,
                               @NonNull String currency, @NonNull String amount,
                               @NonNull String itemsNumber, @NonNull String sentBy) {

    public static RewardCreatedDTO fromEvent(RewardCreated rewardCreated) {
        return new RewardCreatedDTO(
                rewardCreated.recipientGithubLogin(), rewardCreated.shortReward().getProjectName(),
                rewardCreated.shortReward().getCurrencyCode(), rewardCreated.shortReward().getAmount().setScale(2, RoundingMode.HALF_UP).toString(),
                rewardCreated.contributionsNumber().toString(), rewardCreated.sentByGithubLogin()
        );
    }
}
