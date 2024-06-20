package onlydust.com.marketplace.api.postgres.adapter.it.adapters;

import jakarta.persistence.EntityManagerFactory;
import onlydust.com.marketplace.api.postgres.adapter.PostgresUserAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.OnboardingEntity;
import onlydust.com.marketplace.api.postgres.adapter.it.AbstractPostgresIT;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ApplicationRepository;
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
    @Autowired
    ApplicationRepository applicationRepository;
    @Autowired
    EntityManagerFactory entityManagerFactory;


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
                .userId(user.getId())
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
                .userId(user.getId())
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
    }

    @Test
    @Transactional
    void should_update_onboarding_wizard_display_date() {
        // Given
        final var userId = UUID.randomUUID();
        final OnboardingEntity onboarding = OnboardingEntity.builder()
                .userId(userId)
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
                .userId(userId)
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

    @Test
    void should_clean_obsolete_applications() {
        // Given
        final var em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("""
                ALTER TABLE indexer_exp.github_issues DISABLE TRIGGER ALL;
                INSERT INTO indexer_exp.github_issues (id, repo_id, number, title, status, created_at, closed_at, author_id, html_url, body, comments_count, tech_created_at, tech_updated_at, repo_owner_login, repo_name, repo_html_url, author_login, author_html_url, author_avatar_url) VALUES (100, 1, 1, 'A1', 'OPEN',      '2023-06-01 15:04:42.000000', null, 112474158, 'https://github.com/od-mocks/cool.repo.B/issues/1122', '', 0, '2023-11-21 12:14:31.525295', '2023-11-25 10:28:38.676376', 'od-mocks', 'cool.repo.B', 'https://github.com/od-mocks/cool.repo.B', 'onlydust-contributor', 'https://github.com/onlydust-contributor', 'https://avatars.githubusercontent.com/u/112474158?v=4');
                INSERT INTO indexer_exp.github_issues (id, repo_id, number, title, status, created_at, closed_at, author_id, html_url, body, comments_count, tech_created_at, tech_updated_at, repo_owner_login, repo_name, repo_html_url, author_login, author_html_url, author_avatar_url) VALUES (101, 1, 2, 'A2', 'OPEN',      '2023-06-01 15:04:42.000000', null, 112474158, 'https://github.com/od-mocks/cool.repo.B/issues/1122', '', 0, '2023-11-21 12:14:31.525295', '2023-11-25 10:28:38.676376', 'od-mocks', 'cool.repo.B', 'https://github.com/od-mocks/cool.repo.B', 'onlydust-contributor', 'https://github.com/onlydust-contributor', 'https://avatars.githubusercontent.com/u/112474158?v=4');
                INSERT INTO indexer_exp.github_issues (id, repo_id, number, title, status, created_at, closed_at, author_id, html_url, body, comments_count, tech_created_at, tech_updated_at, repo_owner_login, repo_name, repo_html_url, author_login, author_html_url, author_avatar_url) VALUES (200, 2, 1, 'B1', 'COMPLETED', '2023-11-24 15:42:24.000000', '2023-11-28 13:36:18.000000', 82421016, 'https://github.com/kkrt-labs/kakarot-ssj/issues/574', '', 1, '2023-11-24 15:57:11.509427', '2023-11-28 13:36:46.383567', 'kkrt-labs', 'kakarot-ssj', 'https://github.com/kkrt-labs/kakarot-ssj', 'greged93', 'https://github.com/greged93', 'https://avatars.githubusercontent.com/u/82421016?v=4');
                INSERT INTO indexer_exp.github_issues (id, repo_id, number, title, status, created_at, closed_at, author_id, html_url, body, comments_count, tech_created_at, tech_updated_at, repo_owner_login, repo_name, repo_html_url, author_login, author_html_url, author_avatar_url) VALUES (201, 2, 2, 'B2', 'COMPLETED', '2023-11-24 15:42:24.000000', '2023-11-28 13:36:18.000000', 82421016, 'https://github.com/kkrt-labs/kakarot-ssj/issues/574', '', 1, '2023-11-24 15:57:11.509427', '2023-11-28 13:36:46.383567', 'kkrt-labs', 'kakarot-ssj', 'https://github.com/kkrt-labs/kakarot-ssj', 'greged93', 'https://github.com/greged93', 'https://avatars.githubusercontent.com/u/82421016?v=4');
                INSERT INTO indexer_exp.github_issues (id, repo_id, number, title, status, created_at, closed_at, author_id, html_url, body, comments_count, tech_created_at, tech_updated_at, repo_owner_login, repo_name, repo_html_url, author_login, author_html_url, author_avatar_url) VALUES (300, 3, 1, 'C1', 'CANCELLED', '2023-11-24 15:09:28.000000', '2023-11-24 20:43:58.000000', 147428564, 'https://github.com/calcom/cal.com/issues/12527', '', 0, '2023-11-24 15:37:42.770687', '2023-11-24 20:46:57.822598', 'calcom', 'cal.com', 'https://github.com/calcom/cal.com', 'pawar1231', 'https://github.com/pawar1231', 'https://avatars.githubusercontent.com/u/147428564?v=4');
                INSERT INTO indexer_exp.github_issues (id, repo_id, number, title, status, created_at, closed_at, author_id, html_url, body, comments_count, tech_created_at, tech_updated_at, repo_owner_login, repo_name, repo_html_url, author_login, author_html_url, author_avatar_url) VALUES (301, 3, 2, 'C2', 'CANCELLED', '2023-11-24 15:09:28.000000', '2023-11-24 20:43:58.000000', 147428564, 'https://github.com/calcom/cal.com/issues/12527', '', 0, '2023-11-24 15:37:42.770687', '2023-11-24 20:46:57.822598', 'calcom', 'cal.com', 'https://github.com/calcom/cal.com', 'pawar1231', 'https://github.com/pawar1231', 'https://avatars.githubusercontent.com/u/147428564?v=4');
                ALTER TABLE indexer_exp.github_issues ENABLE TRIGGER ALL;  
                ALTER TABLE indexer_exp.github_issues_assignees DISABLE TRIGGER ALL;
                INSERT INTO indexer_exp.github_issues_assignees (issue_id, user_id) VALUES (100, 1);
                INSERT INTO indexer_exp.github_issues_assignees (issue_id, user_id) VALUES (200, 1);
                INSERT INTO indexer_exp.github_issues_assignees (issue_id, user_id) VALUES (300, 1);
                ALTER TABLE indexer_exp.github_issues_assignees ENABLE TRIGGER ALL;

                ALTER TABLE public.applications DISABLE TRIGGER ALL;
                INSERT INTO public.applications (id, received_at, project_id, issue_id, origin, applicant_id) VALUES ('10e0ca81-7633-4357-a88b-168278facfab', '2023-08-07 11:38:54', gen_random_uuid(), null, 'MARKETPLACE', 1000);
                INSERT INTO public.applications (id, received_at, project_id, issue_id, origin, applicant_id) VALUES ('11e0ca81-7633-4357-a88b-168278facfab', '2023-08-07 11:38:54', gen_random_uuid(), 100, 'MARKETPLACE', 1000);
                INSERT INTO public.applications (id, received_at, project_id, issue_id, origin, applicant_id) VALUES ('12e0ca81-7633-4357-a88b-168278facfab', '2023-08-07 11:38:54', gen_random_uuid(), 101, 'MARKETPLACE', 1000);
                INSERT INTO public.applications (id, received_at, project_id, issue_id, origin, applicant_id) VALUES ('13e0ca81-7633-4357-a88b-168278facfab', '2023-08-07 11:38:54', gen_random_uuid(), 200, 'MARKETPLACE', 1000);
                INSERT INTO public.applications (id, received_at, project_id, issue_id, origin, applicant_id) VALUES ('14e0ca81-7633-4357-a88b-168278facfab', '2023-08-07 11:38:54', gen_random_uuid(), 201, 'MARKETPLACE', 1000);
                INSERT INTO public.applications (id, received_at, project_id, issue_id, origin, applicant_id) VALUES ('15e0ca81-7633-4357-a88b-168278facfab', '2023-08-07 11:38:54', gen_random_uuid(), 300, 'MARKETPLACE', 1000);
                INSERT INTO public.applications (id, received_at, project_id, issue_id, origin, applicant_id) VALUES ('16e0ca81-7633-4357-a88b-168278facfab', '2023-08-07 11:38:54', gen_random_uuid(), 301, 'MARKETPLACE', 1000);

                INSERT INTO public.applications (id, received_at, project_id, issue_id, origin, applicant_id) VALUES ('17e0ca81-7633-4357-a88b-168278facfab', '2099-08-07 11:38:54', gen_random_uuid(), null, 'MARKETPLACE', 1000);
                INSERT INTO public.applications (id, received_at, project_id, issue_id, origin, applicant_id) VALUES ('18e0ca81-7633-4357-a88b-168278facfab', '2099-08-07 11:38:54', gen_random_uuid(), 100, 'MARKETPLACE', 1000);
                INSERT INTO public.applications (id, received_at, project_id, issue_id, origin, applicant_id) VALUES ('19e0ca81-7633-4357-a88b-168278facfab', '2099-08-07 11:38:54', gen_random_uuid(), 101, 'MARKETPLACE', 1000);
                INSERT INTO public.applications (id, received_at, project_id, issue_id, origin, applicant_id) VALUES ('20e0ca81-7633-4357-a88b-168278facfab', '2099-08-07 11:38:54', gen_random_uuid(), 200, 'MARKETPLACE', 1000);
                INSERT INTO public.applications (id, received_at, project_id, issue_id, origin, applicant_id) VALUES ('21e0ca81-7633-4357-a88b-168278facfab', '2099-08-07 11:38:54', gen_random_uuid(), 201, 'MARKETPLACE', 1000);
                INSERT INTO public.applications (id, received_at, project_id, issue_id, origin, applicant_id) VALUES ('22e0ca81-7633-4357-a88b-168278facfab', '2099-08-07 11:38:54', gen_random_uuid(), 300, 'MARKETPLACE', 1000);
                INSERT INTO public.applications (id, received_at, project_id, issue_id, origin, applicant_id) VALUES ('23e0ca81-7633-4357-a88b-168278facfab', '2099-08-07 11:38:54', gen_random_uuid(), 301, 'MARKETPLACE', 1000);
                ALTER TABLE public.applications ENABLE TRIGGER ALL;
                """).executeUpdate();
        em.getTransaction().commit();
        em.close();

        // When
        postgresUserAdapter.deleteObsoleteApplications();

        // Then
        assertThat(applicationRepository.existsById(UUID.fromString("10e0ca81-7633-4357-a88b-168278facfab"))).isFalse();
        assertThat(applicationRepository.existsById(UUID.fromString("11e0ca81-7633-4357-a88b-168278facfab"))).isFalse();
        assertThat(applicationRepository.existsById(UUID.fromString("12e0ca81-7633-4357-a88b-168278facfab"))).isTrue(); // OPEN and not assigned
        assertThat(applicationRepository.existsById(UUID.fromString("13e0ca81-7633-4357-a88b-168278facfab"))).isFalse();
        assertThat(applicationRepository.existsById(UUID.fromString("14e0ca81-7633-4357-a88b-168278facfab"))).isFalse();
        assertThat(applicationRepository.existsById(UUID.fromString("15e0ca81-7633-4357-a88b-168278facfab"))).isFalse();
        assertThat(applicationRepository.existsById(UUID.fromString("16e0ca81-7633-4357-a88b-168278facfab"))).isFalse();

        assertThat(applicationRepository.existsById(UUID.fromString("17e0ca81-7633-4357-a88b-168278facfab"))).isTrue();
        assertThat(applicationRepository.existsById(UUID.fromString("18e0ca81-7633-4357-a88b-168278facfab"))).isTrue();
        assertThat(applicationRepository.existsById(UUID.fromString("19e0ca81-7633-4357-a88b-168278facfab"))).isTrue();
        assertThat(applicationRepository.existsById(UUID.fromString("20e0ca81-7633-4357-a88b-168278facfab"))).isTrue();
        assertThat(applicationRepository.existsById(UUID.fromString("21e0ca81-7633-4357-a88b-168278facfab"))).isTrue();
        assertThat(applicationRepository.existsById(UUID.fromString("22e0ca81-7633-4357-a88b-168278facfab"))).isTrue();
        assertThat(applicationRepository.existsById(UUID.fromString("23e0ca81-7633-4357-a88b-168278facfab"))).isTrue();
    }
}