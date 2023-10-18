package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation;
import onlydust.com.marketplace.api.domain.port.output.UserStoragePort;
import onlydust.com.marketplace.api.domain.view.UserProfileView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectIdsForUserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.old.RegisteredUserViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.UserPayoutInfoEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.UserMapper;
import onlydust.com.marketplace.api.postgres.adapter.mapper.UserPayoutInfoMapper;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomUserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.GlobalSettingsRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.RegisteredUserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.UserPayoutInfoRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;

@AllArgsConstructor
public class PostgresUserAdapter implements UserStoragePort {

    private final CustomUserRepository customUserRepository;
    private final UserRepository userRepository;
    private final UserViewRepository userViewRepository;
    private final GlobalSettingsRepository globalSettingsRepository;
    private final RegisteredUserRepository registeredUserRepository;
    private final UserPayoutInfoRepository userPayoutInfoRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserByGithubId(Long githubId) {
        final var settings = globalSettingsRepository.findAll().stream().findFirst().orElseThrow(() -> OnlyDustException.internalServerError("No global settings found", null));
        Optional<UserViewEntity> user = userViewRepository.findByGithubUserId(githubId);
        if (user.isPresent()) {
            return user.map(u -> UserMapper.mapUserToDomain(u, settings.getTermsAndConditionsLatestVersionDate()));
        }
        // Fallback on hasura auth user
        Optional<RegisteredUserViewEntity> hasuraUser = registeredUserRepository.findByGithubId(githubId);
        return hasuraUser.map(u -> UserMapper.mapUserToDomain(u, settings.getTermsAndConditionsLatestVersionDate()));
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
                .orElseThrow(() -> OnlyDustException.notFound(format("User profile %s not found", userId)));

    }

    @Override
    @Transactional(readOnly = true)
    public UserPayoutInformation getPayoutInformationById(UUID id) {
        final UserPayoutInfoEntity userPayoutInfoEntity = userPayoutInfoRepository.getById(id);
        return UserPayoutInfoMapper.mapEntityToDomain(userPayoutInfoEntity);
    }

    @Transactional
    public void savePayoutInformationForUserId(UUID userId, UserPayoutInformation userPayoutInformation) {
        final UserPayoutInfoEntity userPayoutInfoEntity = UserPayoutInfoMapper.mapDomainToEntity(userId,
                userPayoutInformation);
        userPayoutInfoRepository.save(userPayoutInfoEntity);
    }
}
