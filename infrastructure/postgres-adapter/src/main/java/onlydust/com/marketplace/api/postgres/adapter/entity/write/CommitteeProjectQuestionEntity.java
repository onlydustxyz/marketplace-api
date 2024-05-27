package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.project.domain.model.ProjectQuestion;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "committee_project_questions", schema = "public")
public class CommitteeProjectQuestionEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull UUID id;
    @NonNull String question;
    @NonNull Boolean required;

    @ManyToOne
    @JoinColumn(name = "committeeId")
    @NonNull CommitteeEntity committee;

    public static CommitteeProjectQuestionEntity fromDomain(final CommitteeEntity committee, final ProjectQuestion projectQuestion) {
        return CommitteeProjectQuestionEntity.builder()
                .committee(committee)
                .id(projectQuestion.id().value())
                .question(projectQuestion.question())
                .required(projectQuestion.required())
                .build();
    }

    public ProjectQuestion toDomain() {
        return ProjectQuestion.builder()
                .id(ProjectQuestion.Id.of(id))
                .question(question)
                .required(required)
                .build();
    }
}
