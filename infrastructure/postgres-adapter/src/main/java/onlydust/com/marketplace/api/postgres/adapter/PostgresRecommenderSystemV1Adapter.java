package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.recommendation.MatchingQuestionEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.recommendation.UserAnswerEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.RecommendationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserAnswerRepository;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.recommendation.MatchingAnswer;
import onlydust.com.marketplace.project.domain.model.recommendation.MatchingQuestion;
import onlydust.com.marketplace.project.domain.port.output.RecommenderSystemPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toSet;

@Component
@AllArgsConstructor
public class PostgresRecommenderSystemV1Adapter implements RecommenderSystemPort {
    private final String MATCHING_SYSTEM_ID;
    private final RecommendationRepository recommendationRepository;
    private final UserAnswerRepository userAnswerRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MatchingQuestion> getMatchingQuestions(UserId userId) {
        final var questions = recommendationRepository.findAllByMatchingSystemId(MATCHING_SYSTEM_ID).stream()
                .sorted(comparing(MatchingQuestionEntity::getIndex))
                .toList();
        final var userAnswers = userAnswerRepository
                .findAllByUserIdAndQuestionIdIn(userId.value(), questions.stream().map(MatchingQuestionEntity::getId).toList())
                .stream().map(UserAnswerEntity::getAnswerId).collect(toSet());

        return questions.stream().map(question -> question.toDomain(userAnswers)).toList();
    }

    @Override
    @Transactional
    public void saveMatchingAnswers(UserId userId, MatchingQuestion.Id questionId,
                                    List<MatchingAnswer.Id> chosenAnswerIds) {
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
    public List<ProjectId> getRecommendedProjects(UserId userId) {
        // TODO
        return List.of();
    }
} 