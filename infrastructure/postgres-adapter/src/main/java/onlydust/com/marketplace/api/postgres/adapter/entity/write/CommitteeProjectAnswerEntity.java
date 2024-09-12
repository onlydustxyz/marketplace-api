package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.ProjectQuestion;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "committee_project_answers", schema = "public")
@IdClass(CommitteeProjectAnswerEntity.PrimaryKey.class)
public class CommitteeProjectAnswerEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID committeeId;
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID projectId;
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID questionId;

    @NonNull
    UUID userId;
    String answer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "committeeId", insertable = false, updatable = false)
    @NonNull
    CommitteeEntity committee;

    public static List<CommitteeProjectAnswerEntity> fromDomain(final CommitteeEntity committee, final Committee.Application application) {
        return application.answers().stream()
                .map(projectAnswer -> CommitteeProjectAnswerEntity.builder()
                        .committeeId(committee.id)
                        .projectId(application.projectId().value())
                        .userId(application.userId().value())
                        .questionId(projectAnswer.projectQuestionId().value())
                        .answer(projectAnswer.answer())
                        .committee(committee)
                        .build()).toList();
    }

    public Committee.Application toApplication() {
        return new Committee.Application(UserId.of(userId),
                ProjectId.of(projectId),
                List.of(new Committee.ProjectAnswer(ProjectQuestion.Id.of(questionId), answer)));
    }

    @EqualsAndHashCode
    @AllArgsConstructor
    @Data
    @NoArgsConstructor(force = true)
    public static class PrimaryKey implements Serializable {
        UUID projectId;
        UUID committeeId;
        UUID questionId;
    }
}
