package onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Immutable
@Table(name = "authorized_github_repos", schema = "indexer_exp")
@NoArgsConstructor
public class GithubAuthorizedRepoViewEntity {
    @EmbeddedId
    @EqualsAndHashCode.Include
    Id id;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @Builder
    public static class Id implements Serializable {
        Long repoId;
        Long installationId;
    }
}
