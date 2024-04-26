package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import onlydust.com.marketplace.project.domain.view.ShortRepoView;
import org.hibernate.annotations.Immutable;

import java.util.Date;

@EqualsAndHashCode
@Data
@Entity
@Table(name = "github_repos", schema = "indexer_exp")
@Immutable
public class GithubRepoViewEntity {
    @Id
    Long id;
    String ownerLogin;
    String name;
    String htmlUrl;
    Date updatedAt;
    String description;
    Long starsCount;
    Long forksCount;

    public ShortRepoView toShortView() {
        return ShortRepoView.builder()
                .id(id)
                .owner(ownerLogin)
                .name(name)
                .htmlUrl(htmlUrl)
                .description(description)
                .build();
    }
}
