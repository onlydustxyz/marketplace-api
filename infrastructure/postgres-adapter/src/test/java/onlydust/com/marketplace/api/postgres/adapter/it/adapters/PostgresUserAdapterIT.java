package onlydust.com.marketplace.api.postgres.adapter.it.adapters;

import onlydust.com.marketplace.api.postgres.adapter.PostgresUserAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.OnboardingEntity;
import onlydust.com.marketplace.api.postgres.adapter.it.AbstractPostgresIT;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.OnboardingRepository;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.project.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresUserAdapterIT extends AbstractPostgresIT {

    @Autowired
    UserRepository userRepository;
    @Autowired
    OnboardingRepository onboardingRepository;
    @Autowired
    PostgresUserAdapter postgresUserAdapter;


    @Test
    void getUserByGithubId_should_map_onboarding_data() {
        // Given
        final UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().randomNumber() + faker.number().randomNumber())
                .githubLogin(faker.name().name())
                .githubAvatarUrl(faker.internet().avatar())
                .githubEmail(faker.internet().emailAddress())
                .roles(new AuthenticatedUser.Role[]{AuthenticatedUser.Role.USER, AuthenticatedUser.Role.ADMIN})
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
        assertThat(result.getRoles()).containsExactlyInAnyOrder(AuthenticatedUser.Role.USER, AuthenticatedUser.Role.ADMIN);
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
                .roles(new AuthenticatedUser.Role[]{AuthenticatedUser.Role.USER, AuthenticatedUser.Role.ADMIN})
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
        assertThat(result.getRoles()).containsExactlyInAnyOrder(AuthenticatedUser.Role.USER, AuthenticatedUser.Role.ADMIN);
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
                .roles(new AuthenticatedUser.Role[]{AuthenticatedUser.Role.USER, AuthenticatedUser.Role.ADMIN})
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
        assertThat(result.getRoles()).containsExactlyInAnyOrder(AuthenticatedUser.Role.USER, AuthenticatedUser.Role.ADMIN);
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
        assertThat(updatedOnboardingEntity.getTermsAndConditionsAcceptanceDate().toInstant()).isEqualTo(onboarding.getTermsAndConditionsAcceptanceDate().toInstant());
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
        assertThat(updatedOnboardingEntity.getProfileWizardDisplayDate().toInstant()).isEqualTo(onboarding.getProfileWizardDisplayDate().toInstant());
        assertThat(updatedOnboardingEntity.getTermsAndConditionsAcceptanceDate()).isEqualTo(newDate);
    }

    @Test
    void should_support_concurrent_calls_to_createUser() throws InterruptedException {
        final int numberOfThreads = 5;
        final int numberOfIterationPerThread = 50;
        final ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        final CountDownLatch latch = new CountDownLatch(numberOfThreads);
        final var thrown = new ConcurrentLinkedQueue<Throwable>();
        final var retrievedGithubUsers = new ConcurrentLinkedQueue<User>();

        final var githubUserId = faker.number().randomNumber(10, true);

        final var userData = User.builder()
                .githubUserId(githubUserId)
                .githubLogin(faker.name().name())
                .githubAvatarUrl(faker.internet().avatar())
                .githubEmail(faker.internet().emailAddress())
                .roles(List.of(AuthenticatedUser.Role.USER))
                .build();

        // When
        for (int t = 0; t < numberOfThreads; t++) {
            service.execute(() -> {
                TransactionSynchronizationManager.initSynchronization();
                try {
                    System.out.println("Thread " + Thread.currentThread().getName() + " started");
                    final var user = User.builder()
                            .githubUserId(githubUserId)
                            .id(UUID.randomUUID())
                            .githubLogin(userData.getGithubLogin())
                            .githubAvatarUrl(userData.getGithubAvatarUrl())
                            .githubEmail(userData.getGithubEmail())
                            .roles(userData.getRoles())
                            .build();
                    for (int i = 0; i < numberOfIterationPerThread; i++) {
                        try {
                            final var u = postgresUserAdapter.createUser(user);
                            retrievedGithubUsers.add(u);
                        } catch (Exception e) {
                            thrown.add(e);
                        }
                    }
                    latch.countDown();
                    System.out.println("Thread " + Thread.currentThread().getName() + " ended");
                } finally {
                    TransactionSynchronizationManager.clear();
                }
            });
        }
        latch.await();

        // Then
        assertThat(thrown).isEmpty();
        assertThat(retrievedGithubUsers).hasSize(numberOfThreads * numberOfIterationPerThread);
        final var userId = retrievedGithubUsers.stream().findFirst().get().getId();
        retrievedGithubUsers.forEach(u -> {
            assertThat(u.getGithubUserId()).isEqualTo(githubUserId);
            assertThat(u.getGithubLogin()).isEqualTo(userData.getGithubLogin());
            assertThat(u.getGithubAvatarUrl()).isEqualTo(userData.getGithubAvatarUrl());
            assertThat(u.getGithubEmail()).isEqualTo(userData.getGithubEmail());
            assertThat(u.getRoles()).containsExactlyElementsOf(userData.getRoles());
            assertThat(u.getId()).isEqualTo(userId);
        });
    }
}