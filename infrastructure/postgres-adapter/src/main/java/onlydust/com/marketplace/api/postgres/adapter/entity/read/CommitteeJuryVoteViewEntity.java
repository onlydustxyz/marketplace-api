package onlydust.com.marketplace.api.postgres.adapter.entity.read;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "committee_jury_votes", schema = "public")
@IdClass(CommitteeJuryVoteViewEntity.PrimaryKey.class)
@Immutable
public class CommitteeJuryVoteViewEntity {

    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID projectId;
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID criteriaId;
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID committeeId;
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID userId;
    @NonNull
    Long userGithubId;
    @NonNull
    String userGithubLogin;
    @NonNull
    String userGithubAvatarUrl;
    Integer score;
    @NonNull
    String criteria;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projectId", referencedColumnName = "id", insertable = false, updatable = false)
    ProjectShortViewEntity project;

    @EqualsAndHashCode
    @AllArgsConstructor
    @Data
    @NoArgsConstructor(force = true)
    public static class PrimaryKey implements Serializable {
        UUID projectId;
        UUID committeeId;
        UUID criteriaId;
        UUID userId;
    }
}
