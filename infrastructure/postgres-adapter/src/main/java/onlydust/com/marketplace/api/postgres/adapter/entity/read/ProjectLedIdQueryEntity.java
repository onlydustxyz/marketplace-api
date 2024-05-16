package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
@Immutable
public class ProjectLedIdQueryEntity {
    @EmbeddedId
    Id id;
    String projectSlug;
    String logoUrl;
    String name;
    Boolean pending;
    Long contributorCount;
    Boolean isMissingGithubAppInstallation;

    @Embeddable
    @NoArgsConstructor
    @Data
    public static class Id implements Serializable {
        UUID userId;
        UUID projectId;
    }
}
