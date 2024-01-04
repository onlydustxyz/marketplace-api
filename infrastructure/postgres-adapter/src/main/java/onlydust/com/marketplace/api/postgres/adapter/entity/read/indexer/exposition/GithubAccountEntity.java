package onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition;

import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(schema = "indexer_exp", name = "github_accounts")
@Immutable
public class GithubAccountEntity {

  @Id
  @EqualsAndHashCode.Include
  Long id;
  String login;
  String type;
  String htmlUrl;
  String avatarUrl;
  String name;
  @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
  Set<GithubRepoEntity> repos;
  @OneToOne(mappedBy = "account", fetch = FetchType.LAZY)
  GithubAppInstallationEntity installation;
}
