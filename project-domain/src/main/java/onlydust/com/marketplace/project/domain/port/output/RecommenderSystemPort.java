package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.recommendation.MatchingAnswer;
import onlydust.com.marketplace.project.domain.model.recommendation.MatchingQuestion;

import java.util.List;

public interface RecommenderSystemPort {
    List<MatchingQuestion> getMatchingQuestions(UserId userId);
    
    void saveMatchingAnswers(UserId userId, MatchingQuestion.Id questionId, List<MatchingAnswer.Id> chosenAnswerIds);

    List<ProjectId> getRecommendedProjects(UserId userId);
}
