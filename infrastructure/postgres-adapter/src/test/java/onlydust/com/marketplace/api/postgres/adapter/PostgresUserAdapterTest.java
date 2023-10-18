package onlydust.com.marketplace.api.postgres.adapter;

import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.model.UserRole;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.AuthUserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.OnboardingEntity;
import onlydust.com.marketplace.api.postgres.adapter.it.AbstractPostgresIT;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.AuthUserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.OnboardingRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.RegisteredUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresUserAdapterTest extends AbstractPostgresIT {

    @Autowired
    UserRepository userRepository;
    @Autowired
    AuthUserRepository authUserRepository;
    @Autowired
    RegisteredUserRepository registeredUserRepository;
    @Autowired
    OnboardingRepository onboardingRepository;
    @Autowired
    PostgresUserAdapter postgresUserAdapter;

    @Test
    void getUserByGithubId_on_hasura_user_should_map_onboarding_data() {
        // Given
        final AuthUserEntity user = AuthUserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().randomNumber())
                .loginAtSignup(faker.name().name())
                .avatarUrlAtSignup(faker.internet().avatar())
                .isAdmin(true)
                .createdAt(new Date())
                .build();
        authUserRepository.save(user);

        final OnboardingEntity onboarding = OnboardingEntity.builder()
                .id(user.getId())
                .termsAndConditionsAcceptanceDate(new Date())
                .profileWizardDisplayDate(new Date())
                .build();
        onboardingRepository.save(onboarding);

        // When
        final User result = postgresUserAdapter.getUserByGithubId(user.getGithubUserId()).orElseThrow();

        // Then
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getGithubUserId()).isEqualTo(user.getGithubUserId());
        assertThat(result.getLogin()).isEqualTo(user.getLoginAtSignup());
        assertThat(result.getAvatarUrl()).isEqualTo(user.getAvatarUrlAtSignup());
        assertThat(result.getRoles()).containsExactlyInAnyOrder(UserRole.USER, UserRole.ADMIN);
        assertThat(result.isHasSeenOnboardingWizard()).isTrue();
        assertThat(result.isHasAcceptedLatestTermsAndConditions()).isTrue();
    }

    @Test
    void getUserByGithubId_on_hasura_user_should_map_outdated_terms_and_conditions_acceptance() {
        // Given
        final AuthUserEntity user = AuthUserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().randomNumber())
                .loginAtSignup(faker.name().name())
                .avatarUrlAtSignup(faker.internet().avatar())
                .isAdmin(true)
                .createdAt(new Date())
                .build();
        authUserRepository.save(user);

        final OnboardingEntity onboarding = OnboardingEntity.builder()
                .id(user.getId())
                .termsAndConditionsAcceptanceDate(faker.date().birthday())
                .profileWizardDisplayDate(new Date())
                .build();
        onboardingRepository.save(onboarding);

        // When
        final User result = postgresUserAdapter.getUserByGithubId(user.getGithubUserId()).orElseThrow();

        // Then
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getGithubUserId()).isEqualTo(user.getGithubUserId());
        assertThat(result.getLogin()).isEqualTo(user.getLoginAtSignup());
        assertThat(result.getAvatarUrl()).isEqualTo(user.getAvatarUrlAtSignup());
        assertThat(result.getRoles()).containsExactlyInAnyOrder(UserRole.USER, UserRole.ADMIN);
        assertThat(result.isHasSeenOnboardingWizard()).isTrue();
        assertThat(result.isHasAcceptedLatestTermsAndConditions()).isFalse();
    }

    @Test
    void getUserByGithubId_on_hasura_user_without_onboarding_data() {
        // Given
        final AuthUserEntity user = AuthUserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().randomNumber())
                .loginAtSignup(faker.name().name())
                .avatarUrlAtSignup(faker.internet().avatar())
                .isAdmin(true)
                .createdAt(new Date())
                .build();
        authUserRepository.save(user);

        // When
        final User result = postgresUserAdapter.getUserByGithubId(user.getGithubUserId()).orElseThrow();

        // Then
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getGithubUserId()).isEqualTo(user.getGithubUserId());
        assertThat(result.getLogin()).isEqualTo(user.getLoginAtSignup());
        assertThat(result.getAvatarUrl()).isEqualTo(user.getAvatarUrlAtSignup());
        assertThat(result.getRoles()).containsExactlyInAnyOrder(UserRole.USER, UserRole.ADMIN);
        assertThat(result.isHasSeenOnboardingWizard()).isFalse();
        assertThat(result.isHasAcceptedLatestTermsAndConditions()).isFalse();
    }

    @Test
    void getUserByGithubId_on_hasura_user_not_admin_without_onboarding_data() {
        // Given
        final AuthUserEntity user = AuthUserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().randomNumber())
                .loginAtSignup(faker.name().name())
                .avatarUrlAtSignup(faker.internet().avatar())
                .isAdmin(false)
                .createdAt(new Date())
                .build();
        authUserRepository.save(user);

        // When
        final User result = postgresUserAdapter.getUserByGithubId(user.getGithubUserId()).orElseThrow();

        // Then
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getGithubUserId()).isEqualTo(user.getGithubUserId());
        assertThat(result.getLogin()).isEqualTo(user.getLoginAtSignup());
        assertThat(result.getAvatarUrl()).isEqualTo(user.getAvatarUrlAtSignup());
        assertThat(result.getRoles()).containsExactlyInAnyOrder(UserRole.USER);
        assertThat(result.isHasSeenOnboardingWizard()).isFalse();
        assertThat(result.isHasAcceptedLatestTermsAndConditions()).isFalse();
    }

    @Test
    void getUserByGithubId_should_map_onboarding_data() {
        // Given
        final UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().randomNumber())
                .githubLogin(faker.name().name())
                .githubAvatarUrl(faker.internet().avatar())
                .roles(new UserRole[]{UserRole.USER, UserRole.ADMIN})
                .build();
        userRepository.save(user);

        final OnboardingEntity onboarding = OnboardingEntity.builder()
                .id(user.getId())
                .termsAndConditionsAcceptanceDate(new Date())
                .profileWizardDisplayDate(new Date())
                .build();
        onboardingRepository.save(onboarding);

        // When
        final User result = postgresUserAdapter.getUserByGithubId(user.getGithubUserId()).orElseThrow();

        // Then
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getGithubUserId()).isEqualTo(user.getGithubUserId());
        assertThat(result.getLogin()).isEqualTo(user.getGithubLogin());
        assertThat(result.getAvatarUrl()).isEqualTo(user.getGithubAvatarUrl());
        assertThat(result.getRoles()).containsExactlyInAnyOrder(UserRole.USER, UserRole.ADMIN);
        assertThat(result.isHasSeenOnboardingWizard()).isTrue();
        assertThat(result.isHasAcceptedLatestTermsAndConditions()).isTrue();
    }

    @Test
    void getUserByGithubId_should_map_outdated_terms_and_conditions_acceptance() {
        // Given
        final UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().randomNumber())
                .githubLogin(faker.name().name())
                .githubAvatarUrl(faker.internet().avatar())
                .roles(new UserRole[]{UserRole.USER, UserRole.ADMIN})
                .build();
        userRepository.save(user);

        final OnboardingEntity onboarding = OnboardingEntity.builder()
                .id(user.getId())
                .termsAndConditionsAcceptanceDate(faker.date().birthday())
                .profileWizardDisplayDate(new Date())
                .build();
        onboardingRepository.save(onboarding);

        // When
        final User result = postgresUserAdapter.getUserByGithubId(user.getGithubUserId()).orElseThrow();

        // Then
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getGithubUserId()).isEqualTo(user.getGithubUserId());
        assertThat(result.getLogin()).isEqualTo(user.getGithubLogin());
        assertThat(result.getAvatarUrl()).isEqualTo(user.getGithubAvatarUrl());
        assertThat(result.getRoles()).containsExactlyInAnyOrder(UserRole.USER, UserRole.ADMIN);
        assertThat(result.isHasSeenOnboardingWizard()).isTrue();
        assertThat(result.isHasAcceptedLatestTermsAndConditions()).isFalse();
    }

    @Test
    void getUserByGithubId_without_onboarding_data() {
        // Given
        final UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().randomNumber())
                .githubLogin(faker.name().name())
                .githubAvatarUrl(faker.internet().avatar())
                .roles(new UserRole[]{UserRole.USER, UserRole.ADMIN})
                .build();
        userRepository.save(user);

        // When
        final User result = postgresUserAdapter.getUserByGithubId(user.getGithubUserId()).orElseThrow();

        // Then
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getGithubUserId()).isEqualTo(user.getGithubUserId());
        assertThat(result.getLogin()).isEqualTo(user.getGithubLogin());
        assertThat(result.getAvatarUrl()).isEqualTo(user.getGithubAvatarUrl());
        assertThat(result.getRoles()).containsExactlyInAnyOrder(UserRole.USER, UserRole.ADMIN);
        assertThat(result.isHasSeenOnboardingWizard()).isFalse();
        assertThat(result.isHasAcceptedLatestTermsAndConditions()).isFalse();
    }
}