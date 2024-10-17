package onlydust.com.marketplace.api.postgres.adapter.it.adapters;

import jakarta.persistence.EntityManagerFactory;
import onlydust.com.marketplace.api.postgres.adapter.PostgresProjectApplicationAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresUserAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.OnboardingEntity;
import onlydust.com.marketplace.api.postgres.adapter.it.AbstractPostgresIT;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ApplicationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.OnboardingRepository;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.user.domain.model.CreatedUser;
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
    @Autowired
    PostgresProjectApplicationAdapter postgresProjectApplicationAdapter;


    @Test
    void getRegisteredUserByGithubId_should_map_onboarding_data() {
        // Given
        final UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().randomNumber() + faker.number().randomNumber())
                .githubLogin(faker.name().name())
                .githubAvatarUrl(faker.internet().avatar())
                .email(faker.internet().emailAddress())
                .roles(new AuthenticatedUser.Role[]{AuthenticatedUser.Role.USER, AuthenticatedUser.Role.ADMIN})
                .lastSeenAt(new Date())
                .build();
        userRepository.save(user);

        final OnboardingEntity onboarding = OnboardingEntity.builder()
                .userId(user.getId())
                .termsAndConditionsAcceptanceDate(new Date())
                .completionDate(new Date())
                .build();
        onboardingRepository.save(onboarding);

        // When
        final var result = postgresUserAdapter.getRegisteredUserByGithubId(user.getGithubUserId()).orElseThrow();

        // Then
        assertThat(result.id().value()).isEqualTo(user.getId());
        assertThat(result.githubUserId()).isEqualTo(user.getGithubUserId());
        assertThat(result.login()).isEqualTo(user.getGithubLogin());
        assertThat(result.avatarUrl()).isEqualTo(user.getGithubAvatarUrl());
        assertThat(result.roles()).containsExactlyInAnyOrder(AuthenticatedUser.Role.USER, AuthenticatedUser.Role.ADMIN);
    }

    @Test
    void getRegisteredUserByGithubId_should_map_outdated_terms_and_conditions_acceptance() {
        // Given
        final UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().randomNumber() + faker.number().randomNumber())
                .githubLogin(faker.name().name())
                .githubAvatarUrl(faker.internet().avatar())
                .email(faker.internet().emailAddress())
                .roles(new AuthenticatedUser.Role[]{AuthenticatedUser.Role.USER, AuthenticatedUser.Role.ADMIN})
                .lastSeenAt(new Date())
                .build();
        userRepository.save(user);

        final OnboardingEntity onboarding = OnboardingEntity.builder()
                .userId(user.getId())
                .termsAndConditionsAcceptanceDate(faker.date().birthday())
                .completionDate(new Date())
                .build();
        onboardingRepository.save(onboarding);

        // When
        final var result = postgresUserAdapter.getRegisteredUserByGithubId(user.getGithubUserId()).orElseThrow();

        // Then
        assertThat(result.id().value()).isEqualTo(user.getId());
        assertThat(result.githubUserId()).isEqualTo(user.getGithubUserId());
        assertThat(result.login()).isEqualTo(user.getGithubLogin());
        assertThat(result.avatarUrl()).isEqualTo(user.getGithubAvatarUrl());
        assertThat(result.roles()).containsExactlyInAnyOrder(AuthenticatedUser.Role.USER, AuthenticatedUser.Role.ADMIN);
    }

    @Test
    void getRegisteredUserByGithubId_without_onboarding_data() {
        // Given
        final UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().randomNumber() + faker.number().randomNumber())
                .githubLogin(faker.name().name())
                .githubAvatarUrl(faker.internet().avatar())
                .email(faker.internet().emailAddress())
                .roles(new AuthenticatedUser.Role[]{AuthenticatedUser.Role.USER, AuthenticatedUser.Role.ADMIN})
                .lastSeenAt(new Date())
                .build();
        userRepository.save(user);

        // When
        final var result = postgresUserAdapter.getRegisteredUserByGithubId(user.getGithubUserId()).orElseThrow();

        // Then
        assertThat(result.id().value()).isEqualTo(user.getId());
        assertThat(result.githubUserId()).isEqualTo(user.getGithubUserId());
        assertThat(result.login()).isEqualTo(user.getGithubLogin());
        assertThat(result.avatarUrl()).isEqualTo(user.getGithubAvatarUrl());
        assertThat(result.roles()).containsExactlyInAnyOrder(AuthenticatedUser.Role.USER, AuthenticatedUser.Role.ADMIN);
    }

    @Test
    @Transactional
    void should_update_onboarding_completion_date() {
        // Given
        final var userId = UUID.randomUUID();
        final OnboardingEntity onboarding = OnboardingEntity.builder()
                .userId(userId)
                .termsAndConditionsAcceptanceDate(faker.date().birthday(0, 3))
                .completionDate(faker.date().birthday(0, 3))
                .build();
        onboardingRepository.save(onboarding);

        final Date newDate = faker.date().birthday(0, 3);

        // When
        postgresUserAdapter.updateOnboardingCompletionDate(UserId.of(userId), newDate);

        // Then
        final OnboardingEntity updatedOnboardingEntity = onboardingRepository.getById(userId);
        assertThat(updatedOnboardingEntity.getCompletionDate()).isEqualTo(newDate);
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
                .completionDate(faker.date().birthday(0, 3))
                .build();
        onboardingRepository.save(onboarding);

        final Date newDate = faker.date().birthday(0, 3);

        // When
        postgresUserAdapter.updateTermsAndConditionsAcceptanceDate(UserId.of(userId), newDate);

        // Then
        final OnboardingEntity updatedOnboardingEntity = onboardingRepository.getById(userId);
        assertThat(updatedOnboardingEntity.getCompletionDate().toInstant()).isEqualTo(onboarding.getCompletionDate().toInstant());
        assertThat(updatedOnboardingEntity.getTermsAndConditionsAcceptanceDate()).isEqualTo(newDate);
    }

    @Test
    void should_support_concurrent_calls_to_tryCreateUser() throws InterruptedException {
        final int numberOfThreads = 10;
        final int numberOfIterationPerThread = 50;
        final ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        final CountDownLatch latch = new CountDownLatch(numberOfThreads);
        final var thrown = new ConcurrentLinkedQueue<Throwable>();
        final var retrievedGithubUsers = new ConcurrentLinkedQueue<CreatedUser>();

        final var githubUserId = faker.number().randomNumber(10, true);

        final var userData = AuthenticatedUser.builder()
                .githubUserId(githubUserId)
                .login(faker.name().name())
                .avatarUrl(faker.internet().avatar())
                .email(faker.internet().emailAddress())
                .roles(List.of(AuthenticatedUser.Role.USER))
                .build();

        // When
        for (int t = 0; t < numberOfThreads; t++) {
            service.execute(() -> {
                TransactionSynchronizationManager.initSynchronization();
                try {
                    System.out.println("Thread " + Thread.currentThread().getName() + " started");
                    for (int i = 0; i < numberOfIterationPerThread; i++) {
                        final var user = AuthenticatedUser.builder()
                                .githubUserId(githubUserId)
                                .id(UserId.random())
                                .login(userData.login())
                                .avatarUrl(userData.avatarUrl())
                                .email(userData.email())
                                .roles(userData.roles())
                                .build();
                        try {
                            final var u = postgresUserAdapter.tryCreateUser(user);
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
        final var userId = retrievedGithubUsers.stream().findFirst().get().user().id();
        retrievedGithubUsers.forEach(u -> {
            assertThat(u.user().githubUserId()).isEqualTo(githubUserId);
            assertThat(u.user().login()).isEqualTo(userData.login());
            assertThat(u.user().avatarUrl()).isEqualTo(userData.avatarUrl());
            assertThat(u.user().email()).isEqualTo(userData.email());
            assertThat(u.user().roles()).containsExactlyElementsOf(userData.roles());
            assertThat(u.user().id()).isEqualTo(userId);
        });
        assertThat(retrievedGithubUsers.stream().filter(CreatedUser::isNew).toList()).hasSize(1);
    }

    @Test
    void should_clean_obsolete_applications() {
        // Given
        final var em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("""
                ALTER TABLE indexer_exp.github_issues DISABLE TRIGGER ALL;
                INSERT INTO indexer_exp.github_issues (id, repo_id, number, title, status, created_at, updated_at, closed_at, author_id, html_url, body, comments_count, tech_created_at, tech_updated_at, repo_owner_login, repo_name, repo_html_url, author_login, author_html_url, author_avatar_url) VALUES (100, 1, 1, 'A1', 'OPEN',      '2023-06-01 15:04:42.000000', '2023-06-01 15:04:42.000000', null, 112474158, 'https://github.com/od-mocks/cool.repo.B/issues/1122', '', 0, '2023-11-21 12:14:31.525295', '2023-11-25 10:28:38.676376', 'od-mocks', 'cool.repo.B', 'https://github.com/od-mocks/cool.repo.B', 'onlydust-contributor', 'https://github.com/onlydust-contributor', 'https://avatars.githubusercontent.com/u/112474158?v=4');
                INSERT INTO indexer_exp.github_issues (id, repo_id, number, title, status, created_at, updated_at, closed_at, author_id, html_url, body, comments_count, tech_created_at, tech_updated_at, repo_owner_login, repo_name, repo_html_url, author_login, author_html_url, author_avatar_url) VALUES (101, 1, 2, 'A2', 'OPEN',      '2023-06-01 15:04:42.000000', '2023-06-01 15:04:42.000000', null, 112474158, 'https://github.com/od-mocks/cool.repo.B/issues/1122', '', 0, '2023-11-21 12:14:31.525295', '2023-11-25 10:28:38.676376', 'od-mocks', 'cool.repo.B', 'https://github.com/od-mocks/cool.repo.B', 'onlydust-contributor', 'https://github.com/onlydust-contributor', 'https://avatars.githubusercontent.com/u/112474158?v=4');
                INSERT INTO indexer_exp.github_issues (id, repo_id, number, title, status, created_at, updated_at, closed_at, author_id, html_url, body, comments_count, tech_created_at, tech_updated_at, repo_owner_login, repo_name, repo_html_url, author_login, author_html_url, author_avatar_url) VALUES (200, 2, 1, 'B1', 'COMPLETED', '2023-11-24 15:42:24.000000', '2023-11-28 13:36:18.000000', '2023-11-28 13:36:18.000000', 82421016, 'https://github.com/kkrt-labs/kakarot-ssj/issues/574', '', 1, '2023-11-24 15:57:11.509427', '2023-11-28 13:36:46.383567', 'kkrt-labs', 'kakarot-ssj', 'https://github.com/kkrt-labs/kakarot-ssj', 'greged93', 'https://github.com/greged93', 'https://avatars.githubusercontent.com/u/82421016?v=4');
                INSERT INTO indexer_exp.github_issues (id, repo_id, number, title, status, created_at, updated_at, closed_at, author_id, html_url, body, comments_count, tech_created_at, tech_updated_at, repo_owner_login, repo_name, repo_html_url, author_login, author_html_url, author_avatar_url) VALUES (201, 2, 2, 'B2', 'COMPLETED', '2023-11-24 15:42:24.000000', '2023-11-28 13:36:18.000000', '2023-11-28 13:36:18.000000', 82421016, 'https://github.com/kkrt-labs/kakarot-ssj/issues/574', '', 1, '2023-11-24 15:57:11.509427', '2023-11-28 13:36:46.383567', 'kkrt-labs', 'kakarot-ssj', 'https://github.com/kkrt-labs/kakarot-ssj', 'greged93', 'https://github.com/greged93', 'https://avatars.githubusercontent.com/u/82421016?v=4');
                INSERT INTO indexer_exp.github_issues (id, repo_id, number, title, status, created_at, updated_at, closed_at, author_id, html_url, body, comments_count, tech_created_at, tech_updated_at, repo_owner_login, repo_name, repo_html_url, author_login, author_html_url, author_avatar_url) VALUES (300, 3, 1, 'C1', 'CANCELLED', '2023-11-24 15:09:28.000000', '2023-11-24 20:43:58.000000', '2023-11-24 20:43:58.000000', 147428564, 'https://github.com/calcom/cal.com/issues/12527', '', 0, '2023-11-24 15:37:42.770687', '2023-11-24 20:46:57.822598', 'calcom', 'cal.com', 'https://github.com/calcom/cal.com', 'pawar1231', 'https://github.com/pawar1231', 'https://avatars.githubusercontent.com/u/147428564?v=4');
                INSERT INTO indexer_exp.github_issues (id, repo_id, number, title, status, created_at, updated_at, closed_at, author_id, html_url, body, comments_count, tech_created_at, tech_updated_at, repo_owner_login, repo_name, repo_html_url, author_login, author_html_url, author_avatar_url) VALUES (301, 3, 2, 'C2', 'CANCELLED', '2023-11-24 15:09:28.000000', '2023-11-24 20:43:58.000000', '2023-11-24 20:43:58.000000', 147428564, 'https://github.com/calcom/cal.com/issues/12527', '', 0, '2023-11-24 15:37:42.770687', '2023-11-24 20:46:57.822598', 'calcom', 'cal.com', 'https://github.com/calcom/cal.com', 'pawar1231', 'https://github.com/pawar1231', 'https://avatars.githubusercontent.com/u/147428564?v=4');
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
        postgresProjectApplicationAdapter.deleteObsoleteApplications();

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