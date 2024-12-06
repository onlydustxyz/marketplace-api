package onlydust.com.marketplace.project.domain.port.output;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.recommendation.MatchingAnswer;
import onlydust.com.marketplace.project.domain.model.recommendation.MatchingQuestion;

import java.util.List;

public interface RecommenderSystemPort {
    boolean isMultipleChoice(@NonNull MatchingQuestion.Id questionId);

    List<MatchingQuestion> getMatchingQuestions(@NonNull UserId userId);

    void saveMatchingAnswers(@NonNull UserId userId, @NonNull MatchingQuestion.Id questionId, @NonNull List<MatchingAnswer.Id> chosenAnswerIds);

    List<ProjectId> getRecommendedProjects(@NonNull UserId userId);
}
