package onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition;

import java.time.ZonedDateTime;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

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
