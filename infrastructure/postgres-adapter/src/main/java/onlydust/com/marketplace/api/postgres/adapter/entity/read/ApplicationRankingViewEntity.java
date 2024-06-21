package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Immutable;

import java.util.UUID;


@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Value
@Table(name = "application_rankings", schema = "public")
@Immutable
@Accessors(fluent = true)
public class ApplicationRankingViewEntity {
    @Id
    @NonNull
    @EqualsAndHashCode.Include
    UUID applicationId;

    int availabilityScore;
    int bestProjectsSimilarityScore;
    int mainRepoLanguageUserScore;
    int projectFidelityScore;
    int recommendationScore;

    int appliedProjectCount;
    int pendingApplicationCountOnThisProject;
    int pendingApplicationCountOnOtherProjects;
    int contributionInProgressCount;
}
