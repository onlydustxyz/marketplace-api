package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.ProjectQuestion;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "committee_project_questions", schema = "public")
public class CommitteeProjectQuestionEntity {
    @Id
    UUID id;
    @NonNull
    String question;
    @NonNull
    Boolean required;
    @NonNull
    UUID committeeId;

    public static CommitteeProjectQuestionEntity fromDomain(final Committee.Id committeeId, final ProjectQuestion projectQuestion) {
        return CommitteeProjectQuestionEntity.builder()
                .id(projectQuestion.id().value())
                .committeeId(committeeId.value())
                .question(projectQuestion.question())
                .required(projectQuestion.required())
                .build();
    }

    public ProjectQuestion toDomain() {
        return new ProjectQuestion(ProjectQuestion.Id.of(id), question, required);
    }
}
