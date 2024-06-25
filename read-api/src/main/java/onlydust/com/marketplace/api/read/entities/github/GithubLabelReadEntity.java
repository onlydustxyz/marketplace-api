package onlydust.com.marketplace.api.read.entities.github;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.GithubLabel;
import org.hibernate.annotations.Immutable;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@NoArgsConstructor(force = true)
@Table(schema = "indexer_exp", name = "github_labels")
@Immutable
public class GithubLabelReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    Long id;

    @NonNull
    String name;

    String description;

    public GithubLabel toDto() {
        return new GithubLabel().name(name).description(description);
    }
}
