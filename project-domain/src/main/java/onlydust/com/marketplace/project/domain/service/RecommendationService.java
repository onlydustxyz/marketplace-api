package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.recommendation.MatchingQuestion;
import onlydust.com.marketplace.project.domain.port.input.RecommendationFacadePort;
import onlydust.com.marketplace.project.domain.port.output.RecommenderSystemPort;

import java.util.List;
import java.util.Set;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;

@AllArgsConstructor
public class RecommendationService implements RecommendationFacadePort {
    private final RecommenderSystemPort recommenderSystemPort; // In the future, we might have multiple recommender systems. For now, we only have one.

    @Override
    public List<MatchingQuestion<?>> getMatchingQuestions(final @NonNull UserId userId,
                                                          final @NonNull String recommenderSystemVersion) {
        return recommenderSystemPort.getMatchingQuestions(userId);
    }

    @Override
    public void saveMatchingAnswers(final @NonNull UserId userId,
                                    final @NonNull MatchingQuestion.Id questionId,
                                    final @NonNull Set<Integer> chosenAnswerIndexes) {
        if (!recommenderSystemPort.isMultipleChoice(questionId) && chosenAnswerIndexes.size() > 1) {
            throw badRequest("Question %s is not multiple choice".formatted(questionId));
        }
        recommenderSystemPort.saveMatchingAnswers(userId, questionId, chosenAnswerIndexes);
    }

    @Override
    public List<ProjectId> getRecommendedProjects(final @NonNull UserId userId,
                                                  final @NonNull String recommenderSystemVersion) {
        return recommenderSystemPort.getRecommendedProjects(userId);
    }
}