package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Data
@EqualsAndHashCode
@Entity
@Table(schema = "indexer_exp", name = "github_accounts")
public class GithubAccountEntity {
    @Id
    Long id;
    String login;
    String type;
    String htmlUrl;
    String avatarUrl;
    String name;
    @OneToMany(mappedBy = "owner")
    List<GithubRepoEntity> repos;
}
