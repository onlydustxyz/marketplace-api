package onlydust.com.marketplace.project.domain.port.input;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.recommendation.MatchingQuestion;

import java.util.List;
import java.util.Set;

public interface RecommendationFacadePort {
    List<MatchingQuestion<?>> getMatchingQuestions(@NonNull UserId userId, @NonNull String recommenderSystemVersion);

    void saveMatchingAnswers(@NonNull UserId userId, @NonNull MatchingQuestion.Id questionId, @NonNull Set<String> chosenAnswerValues);

    List<ProjectId> getRecommendedProjects(@NonNull UserId userId, @NonNull String recommenderSystemVersion);
}