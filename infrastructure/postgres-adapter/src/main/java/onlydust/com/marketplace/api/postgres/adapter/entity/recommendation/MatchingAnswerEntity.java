package onlydust.com.marketplace.api.postgres.adapter.entity.recommendation;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.project.domain.model.recommendation.MatchingAnswer;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "matching_answers", schema = "reco")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchingAnswerEntity {
    @Id
    private UUID id;
    private Integer index;

    private String body;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private MatchingQuestionEntity question;

    public MatchingAnswer toDomain(Set<UUID> chosenAnswerIds) {
        return MatchingAnswer.builder()
                .id(MatchingAnswer.Id.of(id))
                .body(body)
                .chosen(chosenAnswerIds.contains(id))
                .build();
    }
}