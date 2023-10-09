package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.exception.OnlydustException;
import onlydust.com.marketplace.api.domain.port.output.UserStoragePort;
import onlydust.com.marketplace.api.domain.view.UserProfileView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectIdsForUserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectStatsEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomUserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@AllArgsConstructor
public class PostgresUserAdapter implements UserStoragePort {

    private final CustomUserRepository customUserRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileView getProfileById(UUID userId) {
        return customUserRepository.findProfileById(userId)
                .map(userProfileView -> {
                    for (ProjectIdsForUserEntity projectIdsForUserEntity :
                            customUserRepository.getProjectIdsForUserId(userId)) {
                        final ProjectStatsEntity projectStatsForProjectIdAndUserId =
                                customUserRepository.getProjectStatsForProjectIdAndUserId(projectIdsForUserEntity.getId(), userProfileView.getGithubId());
                        userProfileView.addProjectStats(UserProfileView.ProjectStats.builder()
                                .contributorCount(projectStatsForProjectIdAndUserId.getContributorsCount())
                                .isProjectLead(projectIdsForUserEntity.getIsLead())
                                .totalGranted(projectStatsForProjectIdAndUserId.getTotalGranted())
                                .userContributionCount(projectStatsForProjectIdAndUserId.getUserContributionsCount())
                                .userLastContributedAt(projectStatsForProjectIdAndUserId.getLastContributionDate())
                                .id(projectIdsForUserEntity.getId())
                                .logoUrl(projectIdsForUserEntity.getLogoUrl())
                                .name(projectIdsForUserEntity.getName())
                                .build());
                    }
                    return userProfileView;
                })
                .orElseThrow(() -> OnlydustException.builder()
                        .status(404)
                        .message(String.format("User profile %s not found", userId))
                        .build());

    }
}
