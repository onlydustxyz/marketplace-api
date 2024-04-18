package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import lombok.*;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "custom_ignored_contributions", schema = "public")
public class CustomIgnoredContributionEntity {

    @EmbeddedId
    @EqualsAndHashCode.Include
    Id id;

    @Column(name = "ignored")
    @EqualsAndHashCode.Include
    Boolean ignored;

    @Builder
    @Data
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Id implements Serializable {
        @Column(name = "project_id")
        UUID projectId;
        @Column(name = "contribution_id")
        String contributionId;
    }
}
