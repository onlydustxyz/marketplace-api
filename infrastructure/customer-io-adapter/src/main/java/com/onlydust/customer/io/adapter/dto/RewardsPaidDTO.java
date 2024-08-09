package com.onlydust.customer.io.adapter.dto;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.notification.RewardsPaid;

import java.util.List;

import static com.onlydust.customer.io.adapter.dto.UrlMapper.getMarketplaceFrontendUrlFromEnvironment;

public record RewardsPaidDTO(@NonNull String title,
                             @NonNull String description,
                             @NonNull String username,
                             @NonNull List<RewardDTO> rewards,
                             @NonNull ButtonDTO button) {

    private static final String DESCRIPTION = "Good news! We just processed all your pending rewards on OnlyDust. Please find details below";

    public static RewardsPaidDTO fromEvent(String recipientLogin, RewardsPaid rewardsPaid, String environment) {
        return new RewardsPaidDTO("Reward(s) processed", DESCRIPTION, recipientLogin, rewardsPaid.shortRewards().stream().map(RewardDTO::from).toList(),
                new ButtonDTO("See details", getMarketplaceFrontendUrlFromEnvironment(environment) + "rewards"));
    }
}
