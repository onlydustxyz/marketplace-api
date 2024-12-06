package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.recommendation.MatchingAnswer;
import onlydust.com.marketplace.project.domain.model.recommendation.MatchingQuestion;
import onlydust.com.marketplace.project.domain.port.input.RecommendationFacadePort;
import onlydust.com.marketplace.project.domain.port.output.RecommenderSystemPort;

import java.util.List;

@AllArgsConstructor
public class RecommendationService implements RecommendationFacadePort {
    private final RecommenderSystemPort recommenderSystemPort; // In the future, we might have multiple recommender systems. For now, we only have one.

    @Override
    public List<MatchingQuestion> getMatchingQuestions(UserId userId, String recommenderSystemVersion) {
        return recommenderSystemPort.getMatchingQuestions(userId);
    }

    @Override
    public void saveMatchingAnswers(UserId userId, MatchingQuestion.Id questionId,
                                    List<MatchingAnswer.Id> chosenAnswerIds) {
        recommenderSystemPort.saveMatchingAnswers(userId, questionId, chosenAnswerIds);
    }

    @Override
    public List<ProjectId> getRecommendedProjects(UserId userId, String recommenderSystemVersion) {
        return recommenderSystemPort.getRecommendedProjects(userId);
    }
}