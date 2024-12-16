package onlydust.com.marketplace.project.domain.port.output;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.recommendation.MatchingQuestion;

import java.util.List;
import java.util.Set;

public interface RecommenderSystemPort {
    boolean isMultipleChoice(@NonNull MatchingQuestion.Id questionId);

    List<MatchingQuestion<?>> getMatchingQuestions(@NonNull UserId userId);

    void saveMatchingAnswers(@NonNull UserId userId, @NonNull MatchingQuestion.Id questionId, @NonNull Set<Integer> chosenAnswerIndexes);

    List<ProjectId> getRecommendedProjects(@NonNull UserId userId);

    void refreshData();
}
