package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.view.ProjectRewardView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectRewardViewEntity;

public interface ProjectRewardMapper {

    static ProjectRewardView mapEntityToDomain(final ProjectRewardViewEntity entity) {
        return ProjectRewardView.builder()
                .amount(ProjectRewardView.RewardAmountView.builder()
                        .dollarsEquivalent(entity.getDollarsEquivalent())
                        .currency(switch (entity.getCurrency()) {
                            case op -> Currency.Op;
                            case apt -> Currency.Apt;
                            case usd -> Currency.Usd;
                            case eth -> Currency.Eth;
                            case stark -> Currency.Stark;
                        })
                        .total(entity.getAmount())
                        .build())
                .rewardedUserAvatar(entity.getAvatarUrl())
                .rewardedUserLogin(entity.getLogin())
                .numberOfRewardedContributions(entity.getContributionCount())
                .id(entity.getId())
                .requestedAt(entity.getRequestedAt())
                .status(switch (entity.getStatus()) {
                    case "PENDING_SIGNUP" -> ProjectRewardView.RewardStatusView.pendingSignup;
                    case "COMPLETE" -> ProjectRewardView.RewardStatusView.complete;
                    default -> ProjectRewardView.RewardStatusView.processing;
                })
                .build();
    }
}
