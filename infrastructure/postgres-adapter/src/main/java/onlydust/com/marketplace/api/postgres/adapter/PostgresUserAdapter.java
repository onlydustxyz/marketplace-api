package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.exception.OnlydustException;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation;
import onlydust.com.marketplace.api.domain.port.output.UserStoragePort;
import onlydust.com.marketplace.api.domain.view.UserProfileView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectIdsForUserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserPayoutInfoEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.UserMapper;
import onlydust.com.marketplace.api.postgres.adapter.mapper.UserPayoutInfoMapper;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomUserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.UserPayoutInfoRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class PostgresUserAdapter implements UserStoragePort {

    private final CustomUserRepository customUserRepository;
    private final UserRepository userRepository;
    private final UserPayoutInfoRepository userPayoutInfoRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserByGithubId(Long githubId) {
        final var user = userRepository.findByGithubUserId(githubId);
        return user.map(UserMapper::mapUserToDomain);
    }

    @Override
    public void createUser(User user) {
        userRepository.save(UserMapper.mapUserToEntity(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileView getProfileById(UUID userId) {
        return customUserRepository.findProfileById(userId)
                .map(userProfileView -> {
                    for (ProjectIdsForUserEntity projectIdsForUserEntity :
                            customUserRepository.getProjectIdsForUserId(userId)) {
                        userProfileView.addProjectStats(UserProfileView.ProjectStats.builder()
                                .contributorCount(projectIdsForUserEntity.getContributorsCount())
                                .isProjectLead(projectIdsForUserEntity.getIsLead())
                                .totalGranted(projectIdsForUserEntity.getTotalGranted())
                                .userContributionCount(projectIdsForUserEntity.getUserContributionsCount())
                                .userLastContributedAt(projectIdsForUserEntity.getLastContributionDate())
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

    @Override
    @Transactional(readOnly = true)
    public UserPayoutInformation getPayoutInformationById(UUID id) {
        final UserPayoutInfoEntity userPayoutInfoEntity = userPayoutInfoRepository.getById(id);
        return UserPayoutInfoMapper.mapEntityToDomain(userPayoutInfoEntity);
    }
}
