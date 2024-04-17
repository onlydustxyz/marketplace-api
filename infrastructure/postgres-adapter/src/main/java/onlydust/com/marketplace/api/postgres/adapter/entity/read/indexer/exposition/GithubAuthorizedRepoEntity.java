package onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition;

import lombok.*;
import org.hibernate.annotations.Immutable;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.io.Serializable;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Immutable
@Table(name = "authorized_github_repos", schema = "indexer_exp")
@AllArgsConstructor
@NoArgsConstructor
public class GithubAuthorizedRepoEntity {
    @EmbeddedId
    @EqualsAndHashCode.Include
    Id id;

    @Embeddable
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Id implements Serializable {
        Long repoId;
        Long installationId;
    }
}
