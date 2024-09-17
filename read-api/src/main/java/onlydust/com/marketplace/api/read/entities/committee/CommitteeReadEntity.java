package onlydust.com.marketplace.api.read.entities.committee;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.project.domain.model.Committee;
import org.hibernate.annotations.Immutable;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "committees", schema = "public")
@Immutable
@Accessors(fluent = true)
public class CommitteeReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID id;
    @NonNull
    Date applicationStartDate;
    @NonNull
    Date applicationEndDate;
    @NonNull
    String name;

    @Enumerated(EnumType.STRING)
    @NonNull
    Committee.Status status;

    @Column(insertable = false, updatable = false)
    Date techCreatedAt;

    Integer votePerJury;

    @OneToMany(mappedBy = "committee", fetch = FetchType.LAZY)
    @OrderColumn(name = "rank", nullable = false)
    List<CommitteeProjectQuestionReadEntity> projectQuestions;

    @OneToMany(mappedBy = "committee", fetch = FetchType.LAZY)
    Set<CommitteeProjectAnswerReadEntity> projectAnswers;

    @OneToMany(mappedBy = "committee", fetch = FetchType.LAZY)
    Set<CommitteeJuryReadEntity> juries;

    @OneToMany(mappedBy = "committee", fetch = FetchType.LAZY)
    @OrderColumn(name = "rank", nullable = false)
    List<CommitteeJuryCriteriaReadEntity> juryCriterias;

    @OneToMany(mappedBy = "committee", fetch = FetchType.LAZY)
    List<CommitteeJuryVoteReadEntity> juryVotes;

    @OneToMany(mappedBy = "committee", fetch = FetchType.LAZY)
    List<CommitteeBudgetAllocationReadEntity> budgetAllocations;
}
