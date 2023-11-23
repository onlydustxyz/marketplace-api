package onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition;

import lombok.*;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.util.Set;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(schema = "indexer_exp", name = "github_accounts")
@Immutable
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
