package onlydust.com.marketplace.api.postgres.adapter.entity.recommendation;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.project.domain.model.recommendation.MatchingAnswer;
import onlydust.com.marketplace.project.domain.model.recommendation.MatchingQuestion;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "matching_questions_v1", schema = "reco")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Immutable
public class MatchingQuestionV1Entity {
    @Id
    private UUID id;
    private Integer index;
    private String body;
    private String description;
    private Boolean multipleChoice;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<Answer> possibleAnswers;

    public MatchingQuestion toDomain(Set<UUID> chosenAnswerIds) {
        return MatchingQuestion.builder()
                .id(MatchingQuestion.Id.of(id))
                .body(body)
                .description(description)
                .multipleChoice(multipleChoice)
                .answers(parseAnswers(chosenAnswerIds))
                .build();
    }

    private List<MatchingAnswer> parseAnswers(Set<UUID> chosenAnswerIds) {
        return possibleAnswers.stream()
                .map(answer -> answer.toDomain(chosenAnswerIds.contains(answer.id())))
                .toList();
    }

    public record Answer(
            String text,
            UUID id) {
        public MatchingAnswer toDomain(boolean chosen) {
            return MatchingAnswer.builder()
                    .id(MatchingAnswer.Id.of(id))
                    .body(text)
                    .chosen(chosen)
                    .build();
        }
    }
}