package onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import java.time.ZonedDateTime;
import java.util.Set;

@Data
@EqualsAndHashCode
@Entity
@Table(schema = "indexer_exp", name = "github_app_installations")
@Immutable
public class GithubAppInstallationViewEntity {
    @Id
    Long id;
    @OneToOne
    GithubAccountViewEntity account;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "installationId", referencedColumnName = "id", updatable = false, insertable = false)
    Set<GithubAuthorizedRepoViewEntity> authorizedRepos;
    ZonedDateTime suspendedAt;
}
