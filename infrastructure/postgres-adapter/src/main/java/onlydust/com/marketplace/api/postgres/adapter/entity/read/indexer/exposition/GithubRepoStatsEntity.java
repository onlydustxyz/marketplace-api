package onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import java.time.ZonedDateTime;

@EqualsAndHashCode
@Data
@Entity
@Table(schema = "indexer_exp", name = "github_repos_stats")
@Immutable
public class GithubRepoStatsEntity {
    @Id
    Long id;
    ZonedDateTime lastIndexedAt;
}
