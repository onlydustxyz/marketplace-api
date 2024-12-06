package onlydust.com.marketplace.api.postgres.adapter.entity.recommendation;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.project.domain.model.recommendation.MatchingQuestion;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Comparator.comparing;

@Entity
@Table(name = "matching_questions", schema = "reco")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchingQuestionEntity {
    @Id
    private UUID id;
    private String matchingSystemId;
    private Integer index;

    private String body;
    private String description;
    private Boolean multipleChoice;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<MatchingAnswerEntity> possibleAnswers;

    public MatchingQuestion toDomain(Set<UUID> chosenAnswerIds) {
        return MatchingQuestion.builder()
                .id(MatchingQuestion.Id.of(id))
                .body(body)
                .description(description)
                .multipleChoice(multipleChoice)
                .answers(possibleAnswers.stream()
                        .sorted(comparing(MatchingAnswerEntity::getIndex))
                        .map(a -> a.toDomain(chosenAnswerIds))
                        .toList())
                .build();
    }
}