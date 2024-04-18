package onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition;

import lombok.*;
import org.hibernate.annotations.Immutable;

import jakarta.persistence.*;

import java.time.ZonedDateTime;
import java.util.Set;

@Data
@EqualsAndHashCode
@Entity
@Table(schema = "indexer_exp", name = "github_app_installations")
@Immutable
public class GithubAppInstallationEntity {
    @Id
    Long id;
    @OneToOne
    GithubAccountEntity account;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "installationId", referencedColumnName = "id", updatable = false, insertable = false)
    Set<GithubAuthorizedRepoEntity> authorizedRepos;
    ZonedDateTime suspendedAt;
}
