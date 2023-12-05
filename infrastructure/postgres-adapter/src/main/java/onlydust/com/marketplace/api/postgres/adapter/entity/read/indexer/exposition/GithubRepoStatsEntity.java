package onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

import static java.util.Objects.nonNull;

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
