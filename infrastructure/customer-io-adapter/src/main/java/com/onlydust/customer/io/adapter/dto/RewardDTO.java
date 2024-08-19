package com.onlydust.customer.io.adapter.dto;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.notification.dto.ShortReward;

import java.math.RoundingMode;

import static java.util.Objects.isNull;

public record RewardDTO(
        @NonNull String id,
        @NonNull String projectName,
        @NonNull String currency,
        @NonNull String amount,
        @NonNull String contributionsNumber,
        @NonNull String sentBy
) {

    public static RewardDTO from(@NonNull ShortReward reward) {
        return new RewardDTO(reward.getId().pretty(), reward.getProjectName(), reward.getCurrencyCode(),
                reward.getAmount().setScale(3, RoundingMode.HALF_UP).toString(), isNull(reward.getContributionsCount()) ? "0" :
                reward.getContributionsCount().toString(), reward.getSentByGithubLogin());
    }


}
