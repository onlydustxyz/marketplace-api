package onlydust.com.marketplace.api.read.entities.github;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.GithubLabelWithCountResponse;

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@NoArgsConstructor(force = true)
@Immutable
public class GithubLabelWithCountReadEntity {
    @Id
    @NonNull
    Long id;

    @NonNull
    String name;

    String description;

    Integer issueCount;

    public GithubLabelWithCountResponse toResponse() {
        return new GithubLabelWithCountResponse()
            .name(name)
            .description(description)
            .count(issueCount);
    }
}


