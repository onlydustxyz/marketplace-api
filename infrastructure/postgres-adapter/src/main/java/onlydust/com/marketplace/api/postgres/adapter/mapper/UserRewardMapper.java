package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserRewardTotalAmountEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserRewardViewEntity;
import onlydust.com.marketplace.project.domain.view.UserRewardTotalAmountsView;
import onlydust.com.marketplace.project.domain.view.UserRewardView;
import onlydust.com.marketplace.project.domain.view.UserTotalRewardView;

import java.util.List;

public interface UserRewardMapper {

    static UserRewardView mapEntityToDomain(final UserRewardViewEntity entity) {
        return UserRewardView.builder()
                .amount(UserRewardView.RewardAmountView.builder()
                        .dollarsEquivalent(entity.getStatusData().getAmountUsdEquivalent())
                        .currency(entity.getCurrency().toOldDomain()) // TODO: replace with correct currency
                        .total(entity.getAmount())
                        .build())
                .rewardedOnProjectLogoUrl(entity.getProject().getLogoUrl())
                .rewardedOnProjectName(entity.getProject().getName())
                .numberOfRewardedContributions(entity.getContributionCount())
                .id(entity.getId())
                .requestedAt(entity.getRequestedAt())
                .processedAt(entity.getStatusData().getPaidAt())
                .projectId(entity.getProject().getId())
                .status(entity.getStatus().forUser())
                .build();
    }

    static UserRewardTotalAmountsView mapTotalAmountEntitiesToDomain(final List<UserRewardTotalAmountEntity> entities) {
        final UserRewardTotalAmountsView userRewardTotalAmountsView = UserRewardTotalAmountsView.builder().build();
        for (UserRewardTotalAmountEntity entity : entities) {
            userRewardTotalAmountsView.addUserTotalReward(UserTotalRewardView.builder()
                    .totalAmount(entity.getTotal())
                    .totalDollarsEquivalent(entity.getDollarsEquivalent())
                    .currency(entity.getCurrency().toDomain())
                    .build());
        }
        return userRewardTotalAmountsView;
    }
}
