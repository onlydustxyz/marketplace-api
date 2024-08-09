package com.onlydust.customer.io.adapter.dto;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.notification.RewardCanceled;

public record RewardCanceledDTO(@NonNull String title,
                                @NonNull String description,
                                @NonNull String username,
                                @NonNull RewardDTO reward) {

    private static final String DESCRIPTION = "We're very sorry but reward of project %s got canceled.<br />" +
                                              "Please reach out to project lead(s) for more explanation or contact us in case your need assistance.";

    public static RewardCanceledDTO fromEvent(@NonNull String recipientLogin, @NonNull RewardCanceled rewardCanceled) {
        return new RewardCanceledDTO("Reward canceled", DESCRIPTION.formatted(rewardCanceled.shortReward().getProjectName()), recipientLogin,
                RewardDTO.from(rewardCanceled.shortReward()));
    }
}
