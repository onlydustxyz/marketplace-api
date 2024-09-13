package com.onlydust.customer.io.adapter.dto;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.notification.RewardCanceled;

import static com.onlydust.customer.io.adapter.dto.UrlMapper.getMarketplaceMyRewardsUrlFromEnvironment;

public record RewardCanceledDTO(@NonNull String title,
                                @NonNull String description,
                                @NonNull String username,
                                @NonNull RewardDTO reward,
                                @NonNull ButtonDTO button) {

    private static final String DESCRIPTION = "We're very sorry but reward of project %s got canceled.<br />" +
                                              "Please reach out to project lead(s) for more explanation or contact us in case your need assistance.";

    public static RewardCanceledDTO fromEvent(@NonNull String recipientLogin, @NonNull RewardCanceled rewardCanceled, @NonNull String environment) {
        return new RewardCanceledDTO("Reward canceled", DESCRIPTION.formatted(rewardCanceled.shortReward().projectName()), recipientLogin,
                RewardDTO.from(rewardCanceled.shortReward()), new ButtonDTO("See my rewards", getMarketplaceMyRewardsUrlFromEnvironment(environment)));
    }
}
