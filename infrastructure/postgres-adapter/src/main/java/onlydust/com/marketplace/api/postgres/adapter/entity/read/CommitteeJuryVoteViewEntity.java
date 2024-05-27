package onlydust.com.marketplace.api.postgres.adapter.entity.read;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CommitteeJuryVoteEntity;
import onlydust.com.marketplace.project.domain.view.RegisteredContributorLinkView;
import onlydust.com.marketplace.project.domain.view.commitee.ProjectJuryVoteView;
import onlydust.com.marketplace.project.domain.view.commitee.VoteView;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.util.*;

import static java.util.Objects.isNull;

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

    public static List<ProjectJuryVoteView> toDomain(final List<CommitteeJuryVoteViewEntity> committeeJuryVoteViewEntities) {
        if (isNull(committeeJuryVoteViewEntities) || committeeJuryVoteViewEntities.isEmpty()) {
            return List.of();
        }
        final Map<UUID, ProjectJuryVoteView> juryVoteViewMap = new HashMap<>();
        for (CommitteeJuryVoteViewEntity committeeJuryVoteViewEntity : committeeJuryVoteViewEntities) {
            if (juryVoteViewMap.containsKey(committeeJuryVoteViewEntity.getUserId())) {
                juryVoteViewMap.get(committeeJuryVoteViewEntity.getUserId()).voteViews().add(
                        new VoteView(committeeJuryVoteViewEntity.getCriteria(), committeeJuryVoteViewEntity.getScore()));
            } else {
                juryVoteViewMap.put(committeeJuryVoteViewEntity.getUserId(), new ProjectJuryVoteView(
                                RegisteredContributorLinkView.builder()
                                        .id(committeeJuryVoteViewEntity.getUserId())
                                        .githubUserId(committeeJuryVoteViewEntity.getUserGithubId())
                                        .avatarUrl(committeeJuryVoteViewEntity.getUserGithubAvatarUrl())
                                        .login(committeeJuryVoteViewEntity.getUserGithubLogin())
                                        .build(),
                                new ArrayList<>(List.of(new VoteView(committeeJuryVoteViewEntity.getCriteria(), committeeJuryVoteViewEntity.getScore())))
                        )
                );
            }
        }
        return juryVoteViewMap.values().stream().toList();
    }
}
