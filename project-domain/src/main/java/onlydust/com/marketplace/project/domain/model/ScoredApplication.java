package onlydust.com.marketplace.project.domain.model;

import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Getter
public class ScoredApplication extends Application {
    private final int score;

    private ScoredApplication(Application application, Integer score) {
        super(application.id(),
                application.projectId(),
                application.applicantId(),
                application.origin(),
                application.appliedAt(),
                application.issueId(),
                application.commentId(),
                application.motivations(),
                application.problemSolvingApproach());
        this.score = score;
    }

    public static ScoredApplication of(Application application, Integer score) {
        return new ScoredApplication(application, score);
    }
}
