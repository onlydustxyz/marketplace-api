package onlydust.com.marketplace.bff.read.entities.project;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Immutable;

import java.util.UUID;
import java.util.stream.Stream;

import static java.util.stream.Collectors.averagingInt;


@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Table(name = "application_rankings", schema = "public")
@Immutable
@Accessors(fluent = true)
public class ApplicationRankingReadEntity {
    @Id
    @NonNull
    @EqualsAndHashCode.Include
    UUID applicationId;

    @NonNull
    double availabilityPercentile;
    @NonNull
    double bestProjectsSimilarityPercentile;
    @NonNull
    double mainRepoLanguageUserPercentile;
    @NonNull
    double projectFidelityPercentile;

    public int availabilityScore() {
        return percentileToScorePercentage(availabilityPercentile);
    }

    public int bestProjectsSimilarityScore() {
        return percentileToScorePercentage(bestProjectsSimilarityPercentile);
    }

    public int mainRepoLanguageUserScore() {
        return percentileToScorePercentage(mainRepoLanguageUserPercentile);
    }

    public int projectFidelityScore() {
        return percentileToScorePercentage(projectFidelityPercentile);
    }

    public int recommandationScore() {
        return Stream.of(availabilityScore(), bestProjectsSimilarityScore(), mainRepoLanguageUserScore(), projectFidelityScore())
                .collect(averagingInt(Integer::intValue))
                .intValue();
    }

    private int percentileToScorePercentage(double percentile) {
        return (int) (1 - percentile) * 100;
    }
}
