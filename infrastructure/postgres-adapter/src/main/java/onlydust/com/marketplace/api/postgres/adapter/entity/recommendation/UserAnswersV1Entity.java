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
    private Integer[] primaryGoals;

    private Integer learningPreference;

    private Integer experienceLevel;

    @JdbcTypeCode(SqlTypes.ARRAY)
    private UUID[] languages;

    @JdbcTypeCode(SqlTypes.ARRAY)
    private UUID[] ecosystems;

    private Integer projectMaturity;

    private Integer communityImportance;

    private Integer longTermInvolvement;

    public Set<Integer> primaryGoals() {
        return primaryGoals == null ? Set.of() : Set.of(primaryGoals);
    }

    public Set<Integer> learningPreference() {
        return learningPreference == null ? Set.of() : Set.of(learningPreference);
    }

    public Set<Integer> experienceLevel() {
        return experienceLevel == null ? Set.of() : Set.of(experienceLevel);
    }

    public Set<UUID> languages() {
        return languages == null ? Set.of() : Set.of(languages);
    }

    public Set<UUID> ecosystems() {
        return ecosystems == null ? Set.of() : Set.of(ecosystems);
    }

    public Set<Integer> projectMaturity() {
        return projectMaturity == null ? Set.of() : Set.of(projectMaturity);
    }

    public Set<Integer> communityImportance() {
        return communityImportance == null ? Set.of() : Set.of(communityImportance);
    }

    public Set<Integer> longTermInvolvement() {
        return longTermInvolvement == null ? Set.of() : Set.of(longTermInvolvement);
    }
}