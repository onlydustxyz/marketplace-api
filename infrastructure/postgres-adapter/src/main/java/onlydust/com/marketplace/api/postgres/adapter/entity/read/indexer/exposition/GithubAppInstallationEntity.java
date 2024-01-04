package onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition;

import java.time.ZonedDateTime;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

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
