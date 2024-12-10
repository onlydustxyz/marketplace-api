package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.RecommendationsApi;
import onlydust.com.marketplace.api.contract.model.MatchingAnswerResponse;
import onlydust.com.marketplace.api.contract.model.MatchingQuestionResponse;
import onlydust.com.marketplace.api.contract.model.MatchingQuestionsResponse;
import onlydust.com.marketplace.api.contract.model.SaveMatchingAnswersRequest;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.project.domain.model.recommendation.MatchingQuestion;
import onlydust.com.marketplace.project.domain.port.input.RecommendationFacadePort;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.UUID;
import java.util.stream.IntStream;

@RestController
@Tags(@Tag(name = "Recommendations"))
@AllArgsConstructor
@Profile("api")
public class RecommendationsRestApi implements RecommendationsApi {

    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final RecommendationFacadePort recommendationFacadePort;

    @Override
    public ResponseEntity<MatchingQuestionsResponse> getMatchingQuestions(String version) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var questions = recommendationFacadePort.getMatchingQuestions(authenticatedUser.id(), version);

        return ResponseEntity.ok(new MatchingQuestionsResponse()
                .questions(questions.stream()
                        .map(q -> new MatchingQuestionResponse()
                                .id(q.id().value())
                                .body(q.body())
                                .description(q.description())
                                .multipleChoice(q.multipleChoice())
                                .answers(IntStream.range(0, q.answers().size())
                                        .mapToObj(i -> {
                                            final var a = q.answers().get(i);
                                            return new MatchingAnswerResponse()
                                                    .index(i)
                                                    .body(a.body())
                                                    .chosen(a.chosen());
                                        }).toList()))
                        .toList()));
    }

    @Override
    public ResponseEntity<Void> saveMatchingQuestionAnswers(UUID questionId, SaveMatchingAnswersRequest request) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        recommendationFacadePort.saveMatchingAnswers(authenticatedUser.id(), MatchingQuestion.Id.of(questionId), new HashSet<>(request.getAnswerIndexes()));
        return ResponseEntity.noContent().build();
    }
}