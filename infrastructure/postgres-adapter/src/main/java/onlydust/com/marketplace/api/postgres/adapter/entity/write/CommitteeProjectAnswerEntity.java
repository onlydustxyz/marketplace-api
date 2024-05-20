package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.project.domain.model.Committee;

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
    @NonNull
    UUID committeeId;
    @Id
    @NonNull
    UUID projectId;
    @Id
    @NonNull
    UUID questionId;
    @NonNull
    UUID userId;
    String answer;

    public static List<CommitteeProjectAnswerEntity> fromDomain(final Committee.Id committeeId, final Committee.Application application) {
        return application.answers().stream()
                .map(projectAnswer -> CommitteeProjectAnswerEntity.builder()
                        .committeeId(committeeId.value())
                        .projectId(application.projectId())
                        .userId(application.userId())
                        .questionId(projectAnswer.projectQuestionId().value())
                        .answer(projectAnswer.answer())
                        .build()).toList();
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
