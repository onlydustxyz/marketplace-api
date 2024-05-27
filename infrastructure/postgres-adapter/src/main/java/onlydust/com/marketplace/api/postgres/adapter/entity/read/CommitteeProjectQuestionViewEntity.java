package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
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
    Integer rank;

    public ProjectQuestion toDomain() {
        return new ProjectQuestion(ProjectQuestion.Id.of(id), question, required);
    }
}
