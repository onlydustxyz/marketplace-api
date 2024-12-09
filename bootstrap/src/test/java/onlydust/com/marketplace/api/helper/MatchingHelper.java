package onlydust.com.marketplace.api.helper;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.recommendation.MatchingAnswerEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.recommendation.MatchingQuestionEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.MatchingQuestionRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserAnswerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class MatchingHelper {
    private final MatchingQuestionRepository matchingQuestionRepository;
    private final UserAnswerRepository userAnswerRepository;

    @Transactional
    public MatchingQuestionEntity createQuestion(String matchingSystemId, int index, boolean multipleChoice) {
        final var question = MatchingQuestionEntity.builder()
                .id(UUID.randomUUID())
                .matchingSystemId(matchingSystemId)
                .index(index)
                .body("Is %s-%d the best question ever?".formatted(matchingSystemId, index))
                .description("Question %s-%d might be the best one ever".formatted(matchingSystemId, index))
                .multipleChoice(multipleChoice)
                .possibleAnswers(new ArrayList<>())
                .build();

        return matchingQuestionRepository.save(question);
    }

    @Transactional
    public List<MatchingAnswerEntity> createAnswersForQuestion(MatchingQuestionEntity question, int numberOfAnswers) {
        List<MatchingAnswerEntity> answers = new ArrayList<>();
        for (int i = 0; i < numberOfAnswers; i++) {
            final var answer = MatchingAnswerEntity.builder()
                    .id(UUID.randomUUID())
                    .question(question)
                    .index(i)
                    .body("Answer %d to question %s-%d".formatted(i, question.getMatchingSystemId(), question.getIndex()))
                    .build();
            answers.add(answer);
        }
        question.setPossibleAnswers(answers);
        matchingQuestionRepository.save(question);
        return answers;
    }

    @Transactional
    public MatchingQuestionEntity createQuestionWithAnswers(String matchingSystemId, int questionIndex, boolean multipleChoice,
                                                            int numberOfAnswers) {
        final var question = createQuestion(matchingSystemId, questionIndex, multipleChoice);
        createAnswersForQuestion(question, numberOfAnswers);
        return question;
    }

    @Transactional
    public void cleanup() {
        userAnswerRepository.deleteAll();
        matchingQuestionRepository.deleteAll();
    }
}