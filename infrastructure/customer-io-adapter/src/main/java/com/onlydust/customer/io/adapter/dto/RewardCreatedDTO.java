package com.onlydust.customer.io.adapter.dto;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.notification.RewardReceived;

import static com.onlydust.customer.io.adapter.dto.UrlMapper.getMarketplaceMyRewardsUrlFromEnvironment;

public record RewardCreatedDTO(@NonNull String title,
                               @NonNull String description,
                               @NonNull String username,
                               @NonNull RewardDTO reward,
                               @NonNull ButtonDTO button) {

    private static final String DESCRIPTION = "Good news! You just received a new reward for your contribution on <b>%s</b>:";

    public static RewardCreatedDTO fromEvent(String recipientLogin, RewardReceived rewardReceived, String environment) {
        return new RewardCreatedDTO("Reward received", DESCRIPTION.formatted(rewardReceived.shortReward().projectName()), recipientLogin,
                RewardDTO.from(rewardReceived.shortReward()),
                new ButtonDTO("See details", getMarketplaceMyRewardsUrlFromEnvironment(environment)));
    }
}
