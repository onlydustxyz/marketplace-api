package onlydust.com.marketplace.api.read.entities.committee;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectLinkViewEntity;
import onlydust.com.marketplace.api.read.entities.user.AllUserReadEntity;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Value
@Immutable
@Table(name = "committee_jury_votes", schema = "public")
@IdClass(CommitteeJuryVoteReadEntity.PrimaryKey.class)
@Accessors(fluent = true)
public class CommitteeJuryVoteReadEntity {
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
    Integer score;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criteriaId", insertable = false, updatable = false)
    CommitteeJuryCriteriaReadEntity criteria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", referencedColumnName = "userId", insertable = false, updatable = false)
    AllUserReadEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projectId", insertable = false, updatable = false)
    ProjectLinkViewEntity project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "committeeId", insertable = false, updatable = false)
    CommitteeReadEntity committee;

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
