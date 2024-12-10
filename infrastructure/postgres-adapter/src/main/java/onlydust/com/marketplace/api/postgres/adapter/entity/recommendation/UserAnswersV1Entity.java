package onlydust.com.marketplace.api.postgres.adapter.entity.recommendation;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "user_answers_v1", schema = "reco")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAnswersV1Entity {
    @Id
    private UUID userId;

    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<UUID> primaryGoals;

    private UUID learningPreference;

    private UUID experienceLevel;

    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<UUID> languages;

    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<UUID> ecosystems;

    private UUID projectMaturity;

    private UUID communityImportance;

    private UUID longTermInvolvement;

    public Set<UUID> allAnswerIds() {
        final var answerIds = new HashSet<UUID>();
        if (primaryGoals != null) answerIds.addAll(primaryGoals);
        if (learningPreference != null) answerIds.add(learningPreference);
        if (experienceLevel != null) answerIds.add(experienceLevel);
        if (languages != null) answerIds.addAll(languages);
        if (ecosystems != null) answerIds.addAll(ecosystems);
        if (projectMaturity != null) answerIds.add(projectMaturity);
        if (communityImportance != null) answerIds.add(communityImportance);
        if (longTermInvolvement != null) answerIds.add(longTermInvolvement);
        return answerIds;
    }
}