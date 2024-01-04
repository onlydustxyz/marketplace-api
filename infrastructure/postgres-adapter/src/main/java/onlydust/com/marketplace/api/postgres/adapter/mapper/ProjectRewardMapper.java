package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.domain.view.ProjectRewardView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectRewardViewEntity;

public interface ProjectRewardMapper {

  static ProjectRewardView mapEntityToDomain(final ProjectRewardViewEntity entity) {
    return ProjectRewardView.builder()
        .amount(ProjectRewardView.RewardAmountView.builder()
            .dollarsEquivalent(entity.getDollarsEquivalent())
            .currency(entity.getCurrency().toDomain())
            .total(entity.getAmount())
            .build())
        .rewardedUserAvatar(entity.getAvatarUrl())
        .rewardedUserLogin(entity.getLogin())
        .numberOfRewardedContributions(entity.getContributionCount())
        .id(entity.getId())
        .requestedAt(entity.getRequestedAt())
        .status(RewardMapper.mapStatusForProject(entity.getStatus()))
        .build();
  }
}
