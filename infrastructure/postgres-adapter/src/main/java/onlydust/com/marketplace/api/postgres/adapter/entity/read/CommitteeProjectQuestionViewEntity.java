package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.ProjectQuestion;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "committee_project_questions", schema = "public")
@Immutable
public class CommitteeProjectQuestionViewEntity {
    @Id
    UUID id;
    @NonNull
    String question;
    @NonNull
    Boolean required;
    @NonNull
    UUID committeeId;

    public static CommitteeProjectQuestionViewEntity fromDomain(final Committee.Id committeeId, final ProjectQuestion projectQuestion) {
        return CommitteeProjectQuestionViewEntity.builder()
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
