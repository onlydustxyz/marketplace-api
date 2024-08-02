package com.onlydust.customer.io.adapter.dto;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.notification.RewardReceived;

import java.math.RoundingMode;

public record RewardCreatedDTO(@NonNull String username, @NonNull String projectName,
                               @NonNull String currency, @NonNull String amount,
                               @NonNull String itemsNumber, @NonNull String sentBy) {

    public static RewardCreatedDTO fromEvent(String recipientLogin, RewardReceived rewardReceived) {
        return new RewardCreatedDTO(
                recipientLogin, rewardReceived.shortReward().getProjectName(),
                rewardReceived.shortReward().getCurrencyCode(), rewardReceived.shortReward().getAmount().setScale(3, RoundingMode.HALF_UP).toString(),
                rewardReceived.contributionCount().toString(), rewardReceived.sentByGithubLogin()
        );
    }
}
