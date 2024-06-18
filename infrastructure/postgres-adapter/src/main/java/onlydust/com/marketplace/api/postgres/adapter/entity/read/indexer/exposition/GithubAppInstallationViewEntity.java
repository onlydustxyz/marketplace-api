package onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition;

import io.hypersistence.utils.hibernate.type.array.StringArrayType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import onlydust.com.marketplace.project.domain.model.GithubAppInstallationStatus;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;

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

    @Getter(AccessLevel.NONE)
    ZonedDateTime suspendedAt;

    @Type(value = StringArrayType.class)
    @Column(columnDefinition = "text[]")
    @Getter(AccessLevel.NONE)
    String[] permissions;

    @Formula("""
            (SELECT
                CASE
                    WHEN suspended_at IS NOT NULL THEN 'SUSPENDED'
                    WHEN NOT permissions @> gs.required_github_app_permissions THEN 'MISSING_PERMISSIONS'
                    ELSE 'COMPLETE'
                END
             FROM global_settings gs
             WHERE gs.id = 1
            )
            """)
    @Enumerated(EnumType.STRING)
    Status status;

    public enum Status {
        SUSPENDED,
        MISSING_PERMISSIONS,
        COMPLETE;

        public GithubAppInstallationStatus toDomain() {
            return switch (this) {
                case SUSPENDED -> GithubAppInstallationStatus.SUSPENDED;
                case MISSING_PERMISSIONS -> GithubAppInstallationStatus.MISSING_PERMISSIONS;
                case COMPLETE -> GithubAppInstallationStatus.COMPLETE;
            };
        }

    }
}
