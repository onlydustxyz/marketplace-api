package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.recommendation.MatchingAnswer;
import onlydust.com.marketplace.project.domain.model.recommendation.MatchingQuestion;

import java.util.List;

public interface RecommendationFacadePort {
    List<MatchingQuestion> getMatchingQuestions(UserId userId, String recommenderSystemVersion);

    void saveMatchingAnswers(UserId userId, MatchingQuestion.Id questionId, List<MatchingAnswer.Id> chosenAnswerIds);

    List<ProjectId> getRecommendedProjects(UserId userId, String recommenderSystemVersion);
}