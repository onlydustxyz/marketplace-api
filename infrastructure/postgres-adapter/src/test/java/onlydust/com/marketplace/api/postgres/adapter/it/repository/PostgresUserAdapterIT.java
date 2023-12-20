package onlydust.com.marketplace.api.postgres.adapter.it.repository;

import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.model.UserRole;
import onlydust.com.marketplace.api.postgres.adapter.PostgresUserAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.AuthUserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.OnboardingEntity;
import onlydust.com.marketplace.api.postgres.adapter.it.AbstractPostgresIT;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.AuthUserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.OnboardingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresUserAdapterIT extends AbstractPostgresIT {

    @Autowired
    UserRepository userRepository;
    @Autowired
    AuthUserRepository authUserRepository;
    @Autowired
    OnboardingRepository onboardingRepository;
    @Autowired
    PostgresUserAdapter postgresUserAdapter;


    public static AuthUserEntity newFakeAuthUserEntity(boolean isAdmin) {
        return AuthUserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().randomNumber() + faker.number().randomNumber())
                .loginAtSignup(faker.name().name())
                .avatarUrlAtSignup(faker.internet().avatar())
                .email(faker.internet().emailAddress())
                .isAdmin(isAdmin)
                .createdAt(new Date())
                .lastSeen(new Date())
                .build();
    }

    @Test
    void getUserByGithubId_on_hasura_user_should_map_onboarding_data() {
        // Given
        final AuthUserEntity user = newFakeAuthUserEntity(true);
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
        assertThat(result.getGithubLogin()).isEqualTo(user.getLoginAtSignup());
        assertThat(result.getGithubAvatarUrl()).isEqualTo(user.getAvatarUrlAtSignup());
        assertThat(result.getRoles()).containsExactlyInAnyOrder(UserRole.USER, UserRole.ADMIN);
        assertThat(result.hasSeenOnboardingWizard()).isTrue();
        assertThat(result.hasAcceptedLatestTermsAndConditions()).isTrue();
    }

    @Test
    void getUserByGithubId_on_hasura_user_should_map_outdated_terms_and_conditions_acceptance() {
        // Given
        final AuthUserEntity user = newFakeAuthUserEntity(true);
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
        assertThat(result.getGithubLogin()).isEqualTo(user.getLoginAtSignup());
        assertThat(result.getGithubAvatarUrl()).isEqualTo(user.getAvatarUrlAtSignup());
        assertThat(result.getRoles()).containsExactlyInAnyOrder(UserRole.USER, UserRole.ADMIN);
        assertThat(result.hasSeenOnboardingWizard()).isTrue();
        assertThat(result.hasAcceptedLatestTermsAndConditions()).isFalse();
    }

    @Test
    void getUserByGithubId_on_hasura_user_without_onboarding_data() {
        // Given
        final AuthUserEntity user = newFakeAuthUserEntity(true);
        authUserRepository.save(user);

        // When
        final User result = postgresUserAdapter.getUserByGithubId(user.getGithubUserId()).orElseThrow();

        // Then
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getGithubUserId()).isEqualTo(user.getGithubUserId());
        assertThat(result.getGithubLogin()).isEqualTo(user.getLoginAtSignup());
        assertThat(result.getGithubAvatarUrl()).isEqualTo(user.getAvatarUrlAtSignup());
        assertThat(result.getRoles()).containsExactlyInAnyOrder(UserRole.USER, UserRole.ADMIN);
        assertThat(result.hasSeenOnboardingWizard()).isFalse();
        assertThat(result.hasAcceptedLatestTermsAndConditions()).isFalse();
    }

    @Test
    void getUserByGithubId_on_hasura_user_not_admin_without_onboarding_data() {
        // Given
        final AuthUserEntity user = newFakeAuthUserEntity(true);
        authUserRepository.save(user);

        // When
        final User result = postgresUserAdapter.getUserByGithubId(user.getGithubUserId()).orElseThrow();

        // Then
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getGithubUserId()).isEqualTo(user.getGithubUserId());
        assertThat(result.getGithubLogin()).isEqualTo(user.getLoginAtSignup());
        assertThat(result.getGithubAvatarUrl()).isEqualTo(user.getAvatarUrlAtSignup());
        assertThat(result.getRoles()).containsExactlyInAnyOrder(UserRole.USER, UserRole.ADMIN);
        assertThat(result.hasSeenOnboardingWizard()).isFalse();
        assertThat(result.hasAcceptedLatestTermsAndConditions()).isFalse();
    }

    @Test
    void getUserByGithubId_should_map_onboarding_data() {
        // Given
        final UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().randomNumber() + faker.number().randomNumber())
                .githubLogin(faker.name().name())
                .githubAvatarUrl(faker.internet().avatar())
                .githubEmail(faker.internet().emailAddress())
                .roles(new UserRole[]{UserRole.USER, UserRole.ADMIN})
                .lastSeenAt(new Date())
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
        assertThat(result.getGithubLogin()).isEqualTo(user.getGithubLogin());
        assertThat(result.getGithubAvatarUrl()).isEqualTo(user.getGithubAvatarUrl());
        assertThat(result.getRoles()).containsExactlyInAnyOrder(UserRole.USER, UserRole.ADMIN);
        assertThat(result.hasSeenOnboardingWizard()).isTrue();
        assertThat(result.hasAcceptedLatestTermsAndConditions()).isTrue();
    }

    @Test
    void getUserByGithubId_should_map_outdated_terms_and_conditions_acceptance() {
        // Given
        final UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().randomNumber() + faker.number().randomNumber())
                .githubLogin(faker.name().name())
                .githubAvatarUrl(faker.internet().avatar())
                .githubEmail(faker.internet().emailAddress())
                .roles(new UserRole[]{UserRole.USER, UserRole.ADMIN})
                .lastSeenAt(new Date())
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
        assertThat(result.getGithubLogin()).isEqualTo(user.getGithubLogin());
        assertThat(result.getGithubAvatarUrl()).isEqualTo(user.getGithubAvatarUrl());
        assertThat(result.getRoles()).containsExactlyInAnyOrder(UserRole.USER, UserRole.ADMIN);
        assertThat(result.hasSeenOnboardingWizard()).isTrue();
        assertThat(result.hasAcceptedLatestTermsAndConditions()).isFalse();
    }

    @Test
    void getUserByGithubId_without_onboarding_data() {
        // Given
        final UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().randomNumber() + faker.number().randomNumber())
                .githubLogin(faker.name().name())
                .githubAvatarUrl(faker.internet().avatar())
                .githubEmail(faker.internet().emailAddress())
                .roles(new UserRole[]{UserRole.USER, UserRole.ADMIN})
                .lastSeenAt(new Date())
                .build();
        userRepository.save(user);

        // When
        final User result = postgresUserAdapter.getUserByGithubId(user.getGithubUserId()).orElseThrow();

        // Then
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getGithubUserId()).isEqualTo(user.getGithubUserId());
        assertThat(result.getGithubLogin()).isEqualTo(user.getGithubLogin());
        assertThat(result.getGithubAvatarUrl()).isEqualTo(user.getGithubAvatarUrl());
        assertThat(result.getRoles()).containsExactlyInAnyOrder(UserRole.USER, UserRole.ADMIN);
        assertThat(result.hasSeenOnboardingWizard()).isFalse();
        assertThat(result.hasAcceptedLatestTermsAndConditions()).isFalse();
    }

    @Test
    @Transactional
    void should_update_onboarding_wizard_display_date() {
        // Given
        final var userId = UUID.randomUUID();
        final OnboardingEntity onboarding = OnboardingEntity.builder()
                .id(userId)
                .termsAndConditionsAcceptanceDate(faker.date().birthday(0, 3))
                .profileWizardDisplayDate(faker.date().birthday(0, 3))
                .build();
        onboardingRepository.save(onboarding);

        final Date newDate = faker.date().birthday(0, 3);

        // When
        postgresUserAdapter.updateOnboardingWizardDisplayDate(userId, newDate);

        // Then
        final OnboardingEntity updatedOnboardingEntity = onboardingRepository.getById(userId);
        assertThat(updatedOnboardingEntity.getProfileWizardDisplayDate()).isEqualTo(newDate);
        assertThat(updatedOnboardingEntity.getTermsAndConditionsAcceptanceDate()).isEqualTo(onboarding.getTermsAndConditionsAcceptanceDate());
    }

    @Test
    @Transactional
    void should_update_onboarding_terms_and_conditions_acceptance_date() {
        // Given
        final var userId = UUID.randomUUID();
        final OnboardingEntity onboarding = OnboardingEntity.builder()
                .id(userId)
                .termsAndConditionsAcceptanceDate(faker.date().birthday(0, 3))
                .profileWizardDisplayDate(faker.date().birthday(0, 3))
                .build();
        onboardingRepository.save(onboarding);

        final Date newDate = faker.date().birthday(0, 3);

        // When
        postgresUserAdapter.updateTermsAndConditionsAcceptanceDate(userId, newDate);

        // Then
        final OnboardingEntity updatedOnboardingEntity = onboardingRepository.getById(userId);
        assertThat(updatedOnboardingEntity.getProfileWizardDisplayDate()).isEqualTo(onboarding.getProfileWizardDisplayDate());
        assertThat(updatedOnboardingEntity.getTermsAndConditionsAcceptanceDate()).isEqualTo(newDate);
    }
}