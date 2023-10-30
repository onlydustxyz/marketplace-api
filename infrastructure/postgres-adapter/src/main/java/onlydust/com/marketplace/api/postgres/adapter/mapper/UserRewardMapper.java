package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.view.UserRewardTotalAmountsView;
import onlydust.com.marketplace.api.domain.view.UserRewardView;
import onlydust.com.marketplace.api.domain.view.UserTotalRewardView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserRewardTotalAmountEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserRewardViewEntity;

import java.util.List;

public interface UserRewardMapper {

    static UserRewardView mapEntityToDomain(final UserRewardViewEntity entity) {
        return UserRewardView.builder()
                .amount(UserRewardView.RewardAmountView.builder()
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
                .rewardedOnProjectLogoUrl(entity.getLogoUrl())
                .rewardedOnProjectName(entity.getName())
                .numberOfRewardedContributions(entity.getContributionCount())
                .id(entity.getId())
                .requestedAt(entity.getRequestedAt())
                .projectId(entity.getProjectId())
                .status(switch (entity.getStatus()) {
                    case "PENDING_INVOICE" -> UserRewardView.RewardStatusView.pendingInvoice;
                    case "COMPLETE" -> UserRewardView.RewardStatusView.complete;
                    case "MISSING_PAYOUT_INFO" -> UserRewardView.RewardStatusView.missingPayoutInfo;
                    default -> UserRewardView.RewardStatusView.processing;
                })
                .build();
    }

        static UserRewardTotalAmountsView mapTotalAmountEntitiesToDomain(final List<UserRewardTotalAmountEntity> entities) {
        final UserRewardTotalAmountsView userRewardTotalAmountsView = UserRewardTotalAmountsView.builder().build();
        for (UserRewardTotalAmountEntity entity : entities) {
            userRewardTotalAmountsView.addUserTotalReward(UserTotalRewardView.builder()
                    .totalAmount(entity.getTotal())
                    .totalDollarsEquivalent(entity.getDollarsEquivalent())
                    .currency(switch (entity.getCurrency()) {
                        case op -> Currency.Op;
                        case apt -> Currency.Apt;
                        case usd -> Currency.Usd;
                        case eth -> Currency.Eth;
                        case stark -> Currency.Stark;
                    })
                    .build());
        }
        return userRewardTotalAmountsView;
    }
}
