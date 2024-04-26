package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import lombok.*;
import org.hibernate.annotations.Immutable;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
@Immutable
public class ProjectLedIdViewEntity {
    @EmbeddedId
    Id id;
    String projectSlug;
    String logoUrl;
    String name;
    Boolean pending;
    Long contributorCount;
    Boolean isMissingGithubAppInstallation;

    @Embeddable
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Id implements Serializable {
        UUID userId;
        UUID projectId;
    }
}
