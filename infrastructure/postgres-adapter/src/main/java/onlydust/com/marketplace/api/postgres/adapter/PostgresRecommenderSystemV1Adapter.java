package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.recommendation.MatchingQuestionEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.recommendation.ProjectRecommendationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.recommendation.UserAnswerEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.MatchingQuestionRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRecommendationV1Repository;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserAnswerRepository;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.recommendation.MatchingAnswer;
import onlydust.com.marketplace.project.domain.model.recommendation.MatchingQuestion;
import onlydust.com.marketplace.project.domain.port.output.RecommenderSystemPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toSet;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@Component
@AllArgsConstructor
public class PostgresRecommenderSystemV1Adapter implements RecommenderSystemPort {
    @Getter
    private final String matchingSystemId;
    private final MatchingQuestionRepository matchingQuestionRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final ProjectRecommendationV1Repository projectRecommendationV1Repository;

    @Override
    public boolean isMultipleChoice(final @NonNull MatchingQuestion.Id questionId) {
        final var question = matchingQuestionRepository.findById(questionId.value())
                .orElseThrow(() -> notFound("Question %s not found".formatted(questionId)));
        return question.getMultipleChoice();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchingQuestion> getMatchingQuestions(final @NonNull UserId userId) {
        final var questions = matchingQuestionRepository.findAllByMatchingSystemId(matchingSystemId).stream()
                .sorted(comparing(MatchingQuestionEntity::getIndex))
                .toList();
        final var userAnswers = userAnswerRepository
                .findAllByUserIdAndQuestionIdIn(userId.value(), questions.stream().map(MatchingQuestionEntity::getId).toList())
                .stream().map(UserAnswerEntity::getAnswerId).collect(toSet());

        return questions.stream().map(question -> question.toDomain(userAnswers)).toList();
    }

    @Override
    @Transactional
    public void saveMatchingAnswers(final @NonNull UserId userId,
                                    final @NonNull MatchingQuestion.Id questionId,
                                    final @NonNull List<MatchingAnswer.Id> chosenAnswerIds) {
        userAnswerRepository.deleteAllByUserIdAndQuestionId(userId.value(), questionId.value());

        chosenAnswerIds.forEach(answerId -> {
            userAnswerRepository.save(UserAnswerEntity.builder()
                    .userId(userId.value())
                    .questionId(questionId.value())
                    .answerId(answerId.value())
                    .build());
        });
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