package onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Table(name = "github_repo_languages", schema = "indexer_exp")
@IdClass(GithubRepoLanguageEntity.PrimaryKey.class)
public class GithubRepoLanguageEntity {
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
