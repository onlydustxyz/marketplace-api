package onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode
@Table(name = "github_repo_languages", schema = "indexer_exp")
@IdClass(GithubRepoLanguageViewEntity.PrimaryKey.class)
@Immutable
public class GithubRepoLanguageViewEntity {
    @Id
    Long repoId;
    @Id
    String language;
    Long lineCount;

    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        Long repoId;
        String language;
    }
}
