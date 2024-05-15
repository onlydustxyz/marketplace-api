package onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import onlydust.com.marketplace.project.domain.view.GithubLabelView;
import org.hibernate.annotations.Immutable;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Value
@NoArgsConstructor(force = true)
@Table(schema = "indexer_exp", name = "github_labels")
@Immutable
public class GithubLabelViewEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    Long id;

    @NonNull
    String name;

    String description;

    public GithubLabelView toView() {
        return new GithubLabelView(id, name, description);
    }
}
