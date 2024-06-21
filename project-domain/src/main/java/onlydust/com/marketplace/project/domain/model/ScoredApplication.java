package onlydust.com.marketplace.project.domain.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Getter
public class ScoredApplication extends Application {
    private final int availabilityScore;
    private final int bestProjectsSimilarityScore;
    private final int mainRepoLanguageUserScore;
    private final int projectFidelityScore;
    private final int recommendationScore;

    private ScoredApplication(final @NonNull Application application,
                              final int availabilityScore,
                              final int bestProjectsSimilarityScore,
                              final int mainRepoLanguageUserScore,
                              final int projectFidelityScore,
                              final int recommendationScore) {
        super(application.id(),
                application.projectId(),
                application.applicantId(),
                application.origin(),
                application.appliedAt(),
                application.issueId(),
                application.commentId(),
                application.motivations(),
                application.problemSolvingApproach());
        this.availabilityScore = availabilityScore;
        this.bestProjectsSimilarityScore = bestProjectsSimilarityScore;
        this.mainRepoLanguageUserScore = mainRepoLanguageUserScore;
        this.projectFidelityScore = projectFidelityScore;
        this.recommendationScore = recommendationScore;
    }

    public static ScoredApplication of(final @NonNull Application application,
                                       final int availabilityScore,
                                       final int bestProjectsSimilarityScore,
                                       final int mainRepoLanguageUserScore,
                                       final int projectFidelityScore,
                                       final int recommendationScore) {
        return new ScoredApplication(application,
                availabilityScore,
                bestProjectsSimilarityScore,
                mainRepoLanguageUserScore,
                projectFidelityScore,
                recommendationScore);
    }
}
