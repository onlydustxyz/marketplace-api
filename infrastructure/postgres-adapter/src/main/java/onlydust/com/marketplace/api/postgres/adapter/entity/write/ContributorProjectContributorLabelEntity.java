package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "contributor_project_contributor_labels", schema = "public")
@IdClass(ContributorProjectContributorLabelEntity.PrimaryKey.class)
public class ContributorProjectContributorLabelEntity {
    @Id
    private @NonNull UUID labelId;
    @Id
    private @NonNull Long githubUserId;

    @ManyToOne
    @JoinColumn(name = "labelId", insertable = false, updatable = false)
    private ProjectContributorLabelEntity projectContributorLabel;

    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PrimaryKey implements Serializable {
        UUID labelId;
        Long githubUserId;
    }
}
