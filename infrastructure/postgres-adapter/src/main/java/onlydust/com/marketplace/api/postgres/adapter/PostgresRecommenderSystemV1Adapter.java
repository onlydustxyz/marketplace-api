package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.recommendation.ProjectRecommendationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.recommendation.UserAnswersV1Entity;
import onlydust.com.marketplace.api.postgres.adapter.repository.MatchingQuestionV1Repository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRecommendationV1Repository;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserAnswersV1Repository;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.recommendation.MatchingAnswer;
import onlydust.com.marketplace.project.domain.model.recommendation.MatchingQuestion;
import onlydust.com.marketplace.project.domain.port.output.RecommenderSystemPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@Component
@AllArgsConstructor
public class PostgresRecommenderSystemV1Adapter implements RecommenderSystemPort {
    @Getter
    private final String matchingSystemId;
    private final MatchingQuestionV1Repository matchingQuestionV1Repository;
    private final UserAnswersV1Repository userAnswersV1Repository;
    private final ProjectRecommendationV1Repository projectRecommendationV1Repository;

    @Override
    public boolean isMultipleChoice(final @NonNull MatchingQuestion.Id questionId) {
        final var question = matchingQuestionV1Repository.findById(questionId.value())
                .orElseThrow(() -> notFound("Question %s not found".formatted(questionId)));
        return question.getMultipleChoice();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchingQuestion> getMatchingQuestions(final @NonNull UserId userId) {
        final var userAnswers = userAnswersV1Repository.findById(userId.value())
                .map(UserAnswersV1Entity::allAnswerIds)
                .orElse(Set.of());

        return matchingQuestionV1Repository.findAllByOrderByIndex().stream()
                .map(q -> q.toDomain(userAnswers))
                .toList();
    }

    @Override
    @Transactional
    public void saveMatchingAnswers(final @NonNull UserId userId,
                                    final @NonNull MatchingQuestion.Id questionId,
                                    final @NonNull List<MatchingAnswer.Id> chosenAnswerIds) {
        final var question = matchingQuestionV1Repository.findById(questionId.value())
                .orElseThrow(() -> notFound("Question %s not found".formatted(questionId)));

        final var userAnswers = userAnswersV1Repository.findById(userId.value())
                .orElse(UserAnswersV1Entity.builder().userId(userId.value()).build());

        updateUserAnswers(userAnswers, question.getIndex(), chosenAnswerIds.stream().map(MatchingAnswer.Id::value).toList());
        userAnswersV1Repository.save(userAnswers);
    }

    private void updateUserAnswers(UserAnswersV1Entity userAnswers, int questionIndex, List<UUID> answerIds) {
        switch (questionIndex) {
            case 0 -> userAnswers.setPrimaryGoals(answerIds);
            case 1 -> userAnswers.setLearningPreference(answerIds.getFirst());
            case 2 -> userAnswers.setExperienceLevel(answerIds.getFirst());
            case 3 -> userAnswers.setLanguages(answerIds);
            case 4 -> userAnswers.setEcosystems(answerIds);
            case 5 -> userAnswers.setProjectMaturity(answerIds.getFirst());
            case 6 -> userAnswers.setCommunityImportance(answerIds.getFirst());
            case 7 -> userAnswers.setLongTermInvolvement(answerIds.getFirst());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectId> getRecommendedProjects(final @NonNull UserId userId) {
        return Stream.concat(projectRecommendationV1Repository.findTopProjects(3).stream(),
                        projectRecommendationV1Repository.findLastActiveProjects(3).stream())
                .map(ProjectRecommendationEntity::getProjectId)
                .map(ProjectId::of)
                .toList();
    }
}