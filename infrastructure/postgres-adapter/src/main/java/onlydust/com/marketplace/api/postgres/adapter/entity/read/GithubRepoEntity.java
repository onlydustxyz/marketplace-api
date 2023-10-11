package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

@EqualsAndHashCode
@Data
@Entity
@Table(schema = "indexer_exp", name = "github_repos")
public class GithubRepoEntity {
    @Id
    Long id;
    @ManyToOne
    GithubAccountEntity owner;
    String name;
    String htmlUrl;
    Date updatedAt;
    String Description;
    Long starsCount;
    Long forksCount;
}
