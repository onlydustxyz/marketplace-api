package onlydust.com.marketplace.api.postgres.adapter.it.entity;

import jakarta.persistence.EntityManagerFactory;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubAppInstallationViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.it.AbstractPostgresIT;
import onlydust.com.marketplace.api.postgres.adapter.repository.GithubAppInstallationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class GithubAppInstallationViewEntityIT extends AbstractPostgresIT {
    @Autowired
    GithubAppInstallationRepository githubAppInstallationRepository;
    @Autowired
    EntityManagerFactory entityManagerFactory;

    @Test
    void should_determine_status_complete() {
        // Given
        final var id = 1000L;
        saveGithubAppInstallation(id, null, new String[]{"issues:write", "issues:read", "metadata:read", "pull_requests:read"});

        // When
        final var githubAppInstallation = githubAppInstallationRepository.findById(id).get();

        // Then
        assertThat(githubAppInstallation.getStatus()).isEqualTo(GithubAppInstallationViewEntity.Status.COMPLETE);
    }

    @Test
    void should_determine_status_complete_with_too_many_permissions() {
        // Given
        final var id = 2000L;
        saveGithubAppInstallation(id, null, new String[]{"issues:write", "issues:read", "metadata:read", "pull_requests:read", "foo:bar", "fooo:baar"});

        // When
        final var githubAppInstallation = githubAppInstallationRepository.findById(id).get();

        // Then
        assertThat(githubAppInstallation.getStatus()).isEqualTo(GithubAppInstallationViewEntity.Status.COMPLETE);
    }

    @Test
    void should_determine_status_missing_permissions() {
        // Given
        var id = 3000L;
        saveGithubAppInstallation(id, null, new String[]{"issues:read", "metadata:read", "pull_requests:read", "foo:bar", "fooo:baar"});

        // When
        final var githubAppInstallation = githubAppInstallationRepository.findById(id).get();

        // Then
        assertThat(githubAppInstallation.getStatus()).isEqualTo(GithubAppInstallationViewEntity.Status.MISSING_PERMISSIONS);
    }

    @Test
    void should_determine_status_suspended() {
        // Given
        final var id = 4000L;
        saveGithubAppInstallation(id, ZonedDateTime.now(), new String[]{"issues:write", "issues:read", "metadata:read", "pull_requests:read"});

        // When
        final var githubAppInstallation = githubAppInstallationRepository.findById(id).get();

        // Then
        assertThat(githubAppInstallation.getStatus()).isEqualTo(GithubAppInstallationViewEntity.Status.SUSPENDED);
    }

    protected void saveGithubAppInstallation(Long id, ZonedDateTime suspendedAt, String[] permissions) {
        final var em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        // Disable FK checks
        em.createNativeQuery("ALTER TABLE indexer_exp.github_app_installations DISABLE TRIGGER ALL;").executeUpdate();
        em.createNativeQuery("INSERT INTO indexer_exp.github_app_installations (id, account_id, suspended_at, permissions) VALUES (:id, :accountId, :suspendedAt, :permissions)")
                .setParameter("id", id)
                .setParameter("accountId", id)
                .setParameter("suspendedAt", suspendedAt)
                .setParameter("permissions", permissions)
                .executeUpdate();
        em.createNativeQuery("ALTER TABLE indexer_exp.github_app_installations ENABLE TRIGGER ALL;").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }
}
