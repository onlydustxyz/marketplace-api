package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.contract.model.MatchingAnswerResponse;
import onlydust.com.marketplace.api.contract.model.MatchingQuestionsResponse;
import onlydust.com.marketplace.api.contract.model.RecommendedProjectsResponse;
import onlydust.com.marketplace.api.contract.model.SaveMatchingAnswersRequest;
import onlydust.com.marketplace.api.helper.MatchingHelper;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.PostgresRecommenderSystemV1Adapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.recommendation.MatchingAnswerEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.recommendation.MatchingQuestionEntity;
import onlydust.com.marketplace.api.suites.tags.TagRecommendation;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@TagRecommendation
public class RecommendationApiIT extends AbstractMarketplaceApiIT {
    private static final String RECOMMENDER_SYSTEM_VERSION = "v1";
    @Autowired
    MatchingHelper matchingHelper;
    @Autowired
    PostgresRecommenderSystemV1Adapter recommenderSystemV1;
    UserAuthHelper.AuthenticatedUser pierre;
    MatchingQuestionEntity singleChoiceQuestion;
    MatchingQuestionEntity multipleChoiceQuestion;
    List<MatchingAnswerEntity> singleChoiceAnswers;
    List<MatchingAnswerEntity> multipleChoiceAnswers;

    @BeforeEach
    void setUp() {
        matchingHelper.cleanup();
        pierre = userAuthHelper.authenticatePierre();

        // Create test questions and answers
        singleChoiceQuestion = matchingHelper.createQuestionWithAnswers(recommenderSystemV1.getMatchingSystemId(), 0, false, 3);
        multipleChoiceQuestion = matchingHelper.createQuestionWithAnswers(recommenderSystemV1.getMatchingSystemId(), 1, true, 4);
        singleChoiceAnswers = singleChoiceQuestion.getPossibleAnswers();
        multipleChoiceAnswers = multipleChoiceQuestion.getPossibleAnswers();

        matchingHelper.createQuestionWithAnswers("another-matching-id-for-ITs", 0, true, 2);
    }

    @Test
    void should_get_matching_questions() {
        // When
        final var response = client.get()
                .uri(getApiURI("/api/v1/me/reco/projects/matching-questions", Map.of("v", RECOMMENDER_SYSTEM_VERSION)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + pierre.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(MatchingQuestionsResponse.class)
                .returnResult()
                .getResponseBody();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getQuestions()).hasSize(2);

        // Verify single choice question
        final var firstQuestion = response.getQuestions().get(0);
        assertThat(firstQuestion.getId()).isEqualTo(singleChoiceQuestion.getId());
        assertThat(firstQuestion.getBody()).isEqualTo(singleChoiceQuestion.getBody());
        assertThat(firstQuestion.getDescription()).isEqualTo(singleChoiceQuestion.getDescription());
        assertThat(firstQuestion.getMultipleChoice()).isFalse();
        assertThat(firstQuestion.getAnswers()).hasSize(3);
        assertThat(firstQuestion.getAnswers()).allMatch(answer -> !answer.getChosen());

        // Verify multiple choice question
        final var secondQuestion = response.getQuestions().get(1);
        assertThat(secondQuestion.getId()).isEqualTo(multipleChoiceQuestion.getId());
        assertThat(secondQuestion.getBody()).isEqualTo(multipleChoiceQuestion.getBody());
        assertThat(secondQuestion.getDescription()).isEqualTo(multipleChoiceQuestion.getDescription());
        assertThat(secondQuestion.getMultipleChoice()).isTrue();
        assertThat(secondQuestion.getAnswers()).hasSize(4);
        assertThat(secondQuestion.getAnswers()).allMatch(answer -> !answer.getChosen());
    }

    @Test
    void should_save_single_choice_answer() {
        testSaveAnswerSingle(singleChoiceAnswers.get(1).getId());
        testSaveAnswerSingle(singleChoiceAnswers.get(0).getId());
    }

    private void testSaveAnswerSingle(UUID chosenAnswerId) {
        // When
        client.put()
                .uri(getApiURI("/api/v1/me/reco/projects/matching-questions/%s/answers"
                        .formatted(singleChoiceQuestion.getId())))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + pierre.jwt())
                .bodyValue(new SaveMatchingAnswersRequest().answerIds(List.of(chosenAnswerId)))
                .exchange()
                .expectStatus()
                .isNoContent();

        // Then
        final var response = client.get()
                .uri(getApiURI("/api/v1/me/reco/projects/matching-questions", Map.of("v", RECOMMENDER_SYSTEM_VERSION)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + pierre.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(MatchingQuestionsResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        final var question = response.getQuestions().get(0);
        assertThat(question.getAnswers())
                .filteredOn(MatchingAnswerResponse::getChosen)
                .hasSize(1)
                .extracting(MatchingAnswerResponse::getId)
                .containsExactly(chosenAnswerId);
    }

    @Test
    void should_save_multiple_choice_answers() {
        testSaveAnswers(List.of(
                multipleChoiceAnswers.get(0).getId(),
                multipleChoiceAnswers.get(2).getId()));
        testSaveAnswers(List.of(
                multipleChoiceAnswers.get(1).getId(),
                multipleChoiceAnswers.get(2).getId(),
                multipleChoiceAnswers.get(3).getId()));
    }

    private void testSaveAnswers(List<@NotNull UUID> chosenAnswerIds) {
        // When
        client.put()
                .uri(getApiURI("/api/v1/me/reco/projects/matching-questions/%s/answers"
                        .formatted(multipleChoiceQuestion.getId())))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + pierre.jwt())
                .bodyValue(new SaveMatchingAnswersRequest().answerIds(
                        chosenAnswerIds.stream().toList()))
                .exchange()
                .expectStatus()
                .isNoContent();

        // Then
        final var response = client.get()
                .uri(getApiURI("/api/v1/me/reco/projects/matching-questions", Map.of("v", RECOMMENDER_SYSTEM_VERSION)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + pierre.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(MatchingQuestionsResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        final var question = response.getQuestions().get(1);
        assertThat(question.getAnswers())
                .filteredOn(MatchingAnswerResponse::getChosen)
                .hasSize(chosenAnswerIds.size())
                .extracting(MatchingAnswerResponse::getId)
                .containsExactlyInAnyOrderElementsOf(
                        chosenAnswerIds.stream().toList());
    }

    @Test
    void should_fail_when_multiple_answers_for_single_choice_question() {
        // Given
        final var chosenAnswerIds = List.of(
                singleChoiceAnswers.get(0).getId(),
                singleChoiceAnswers.get(1).getId()
        );

        // When
        client.put()
                .uri(getApiURI("/api/v1/me/reco/projects/matching-questions/%s/answers"
                        .formatted(singleChoiceQuestion.getId())))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + pierre.jwt())
                .bodyValue(new SaveMatchingAnswersRequest().answerIds(chosenAnswerIds))
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void should_fail_when_not_authenticated() {
        client.get()
                .uri(getApiURI("/api/v1/me/reco/projects/matching-questions", Map.of("v", RECOMMENDER_SYSTEM_VERSION)))
                .exchange()
                .expectStatus()
                .isUnauthorized();

        client.put()
                .uri(getApiURI("/api/v1/me/reco/projects/matching-questions/%s/answers"
                        .formatted(UUID.randomUUID())))
                .bodyValue(new SaveMatchingAnswersRequest().answerIds(List.of(UUID.randomUUID())))
                .exchange()
                .expectStatus()
                .isUnauthorized();

        client.get()
                .uri(getApiURI("/api/v1/me/reco/projects", Map.of("v", RECOMMENDER_SYSTEM_VERSION)))
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void should_fail_when_question_not_found() {
        client.put()
                .uri(getApiURI("/api/v1/me/reco/projects/matching-questions/%s/answers"
                        .formatted(UUID.randomUUID())))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + pierre.jwt())
                .bodyValue(new SaveMatchingAnswersRequest().answerIds(List.of(UUID.randomUUID())))
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void should_get_recommended_projects_based_on_answers() {

        // When
        final var response = client.get()
                .uri(getApiURI("/api/v1/me/reco/projects", Map.of("v", RECOMMENDER_SYSTEM_VERSION)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + pierre.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(RecommendedProjectsResponse.class)
                .returnResult()
                .getResponseBody();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getProjects()).isNotEmpty();
    }
}