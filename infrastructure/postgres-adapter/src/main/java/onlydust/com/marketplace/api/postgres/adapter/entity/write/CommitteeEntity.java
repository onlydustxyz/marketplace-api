package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.view.commitee.CommitteeLinkView;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "committees", schema = "public")
public class CommitteeEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull UUID id;
    @NonNull Date applicationStartDate;
    @NonNull Date applicationEndDate;
    @NonNull String name;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @NonNull Committee.Status status;

    @Column(insertable = false, updatable = false)
    Date techCreatedAt;
    UUID sponsorId;
    Integer votePerJury;

    @OneToMany(mappedBy = "committee", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    Set<CommitteeProjectQuestionEntity> projectQuestions;

    @OneToMany(mappedBy = "committee", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    Set<CommitteeProjectAnswerEntity> projectAnswers;

    public static CommitteeEntity fromDomain(final Committee committee) {
        final var entity = CommitteeEntity.builder()
                .id(committee.id().value())
                .name(committee.name())
                .applicationEndDate(Date.from(committee.applicationEndDate().toInstant()))
                .applicationStartDate(Date.from(committee.applicationStartDate().toInstant()))
                .status(committee.status())
                .sponsorId(committee.sponsorId())
                .votePerJury(committee.votePerJury())
                .projectQuestions(new HashSet<>())
                .projectAnswers(new HashSet<>())
                .build();

        committee.projectQuestions()
                .forEach(projectQuestion -> entity.projectQuestions.add(CommitteeProjectQuestionEntity.fromDomain(entity, projectQuestion)));

        committee.projectApplications()
                .forEach((projectId, application) -> entity.projectAnswers.addAll(CommitteeProjectAnswerEntity.fromDomain(entity, application)));

        return entity;
    }

    public Committee toDomain() {
        return Committee.builder()
                .id(Committee.Id.of(id))
                .name(name)
                .applicationStartDate(ZonedDateTime.ofInstant(applicationStartDate.toInstant(), ZoneOffset.UTC))
                .applicationEndDate(ZonedDateTime.ofInstant(applicationEndDate.toInstant(), ZoneOffset.UTC))
                .status(status)
                .votePerJury(this.votePerJury)
                .projectApplications(Optional.ofNullable(projectAnswers).orElse(Set.of()).stream()
                        .collect(groupingBy(CommitteeProjectAnswerEntity::getProjectId,
                                mapping(CommitteeProjectAnswerEntity::toApplication,
                                        reducing(null, this::merge)))))
                .build();
    }

    private Committee.Application merge(final Committee.Application left, final @NonNull Committee.Application right) {
        return left == null ? right : new Committee.Application(left.projectId(), left.userId(), concat(left.answers(), right.answers()));
    }

    private List<Committee.ProjectAnswer> concat(final @NonNull List<Committee.ProjectAnswer> left, final @NonNull List<Committee.ProjectAnswer> right) {
        return Stream.concat(left.stream(), right.stream()).collect(toList());
    }
}
