package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.contract.model.MatchingAnswerResponse;
import onlydust.com.marketplace.api.contract.model.MatchingQuestionsResponse;
import onlydust.com.marketplace.api.contract.model.RecommendedProjectsResponse;
import onlydust.com.marketplace.api.contract.model.SaveMatchingAnswersRequest;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagRecommendation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@TagRecommendation
public class RecommendationV1ApiIT extends AbstractMarketplaceApiIT {
    private static final String RECOMMENDER_SYSTEM_VERSION = "v1";
    UserAuthHelper.AuthenticatedUser pierre;

    @BeforeEach
    void setUp() {
        pierre = userAuthHelper.authenticatePierre();
    }

    @Test
    void should_get_matching_v1_questions_and_answers() {
        // When
        final var response = client.get()
                .uri(getApiURI("/api/v1/me/reco/projects/matching-questions",
                        Map.of("v", RECOMMENDER_SYSTEM_VERSION)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + pierre.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(MatchingQuestionsResponse.class).returnResult().getResponseBody();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getQuestions()).hasSize(8);

        // Check primary goals question (index 0)
        final var primaryGoalsQuestion = response.getQuestions().get(0);
        assertThat(primaryGoalsQuestion.getBody())
                .isEqualTo("What are your primary goals for contributing to open-source projects?");
        assertThat(primaryGoalsQuestion.getDescription())
                .contains("goals help us understand what motivates you");
        assertThat(primaryGoalsQuestion.getMultipleChoice()).isTrue();
        assertThat(primaryGoalsQuestion.getAnswers()).extracting(MatchingAnswerResponse::getBody)
                .containsExactlyInAnyOrder("Learning new skills",
                        "Building a professional network", "Gaining practical experience",
                        "Supporting meaningful projects",
                        "Earning recognition in the community");

        // Check experience level question (index 2)
        final var experienceQuestion = response.getQuestions().get(2);
        assertThat(experienceQuestion.getBody())
                .isEqualTo("How would you rate your experience in software development?");
        assertThat(experienceQuestion.getDescription())
                .contains("recommend projects matching your experience level");
        assertThat(experienceQuestion.getMultipleChoice()).isFalse();
        assertThat(experienceQuestion.getAnswers()).extracting(MatchingAnswerResponse::getBody).containsExactly(
                "Beginner", "Intermediate", "Advanced",
                "Expert");

        // Check languages question (index 3)
        final var languagesQuestion = response.getQuestions().get(3);
        assertThat(languagesQuestion.getBody())
                .isEqualTo("Which programming languages are you proficient in or interested in using?");
        assertThat(languagesQuestion.getDescription())
                .contains("Select the languages you'd like to work with in open source projects.");
        assertThat(languagesQuestion.getMultipleChoice()).isTrue();
        assertThat(languagesQuestion.getAnswers()).extracting(MatchingAnswerResponse::getBody).containsExactly(
                "Rust",
                "Cairo",
                "Noir",
                "Javascript",
                "Python",
                "Go",
                "Zig",
                "Java",
                "Ruby",
                "Kotlin",
                "Solidity",
                "Swift",
                "Typescript",
                "C++",
                "C#");

        // Check blockchain ecosystems question (index 4)
        final var blockchainQuestion = response.getQuestions().get(4);
        assertThat(blockchainQuestion.getBody())
                .isEqualTo("Which blockchain ecosystems are you interested in or curious about?");
        assertThat(blockchainQuestion.getDescription())
                .contains("match you with projects in your preferred blockchain ecosystems");
        assertThat(blockchainQuestion.getMultipleChoice()).isTrue();
        assertThat(blockchainQuestion.getAnswers()).extracting(MatchingAnswerResponse::getBody).containsExactly(
                "Aptos", "Avail", "Aztec", "Ethereum", "Lava",
                "Optimism", "Starknet", "Zama", "Don't know");
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

    @Test
    void should_save_single_choice_answer() {
        // When saving a single answer for experience level question
        client.put()
                .uri(getApiURI("/api/v1/me/reco/projects/matching-questions/4f52195c-1c13-4c54-9132-a89e73e4c69d/answers"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + pierre.jwt())
                .bodyValue(new SaveMatchingAnswersRequest().answerIndexes(List.of(2))) // Advanced
                .exchange()
                .expectStatus()
                .isNoContent();

        // Then the answer should be reflected in the questions
        final var response = client.get()
                .uri(getApiURI("/api/v1/me/reco/projects/matching-questions",
                        Map.of("v", RECOMMENDER_SYSTEM_VERSION)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + pierre.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(MatchingQuestionsResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        final var experienceQuestion = response.getQuestions().get(2);
        assertThat(experienceQuestion.getAnswers())
                .filteredOn(MatchingAnswerResponse::getChosen)
                .hasSize(1)
                .extracting(MatchingAnswerResponse::getBody)
                .containsExactly("Advanced");
    }

    @Test
    void should_save_multiple_choice_answers() {
        // When saving multiple answers for languages question
        client.put()
                .uri(getApiURI("/api/v1/me/reco/projects/matching-questions/7d052a24-7824-43d8-8e7b-3727c2c1c9b4/answers"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + pierre.jwt())
                .bodyValue(new SaveMatchingAnswersRequest().answerIndexes(List.of(0, 2))) // JavaScript
                // and Rust
                .exchange()
                .expectStatus()
                .isNoContent();

        // Then the answers should be reflected in the questions
        final var response = client.get()
                .uri(getApiURI("/api/v1/me/reco/projects/matching-questions",
                        Map.of("v", RECOMMENDER_SYSTEM_VERSION)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + pierre.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(MatchingQuestionsResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        final var languagesQuestion = response.getQuestions().get(3);
        assertThat(languagesQuestion.getAnswers())
                .filteredOn(MatchingAnswerResponse::getChosen)
                .hasSize(2)
                .extracting(MatchingAnswerResponse::getBody)
                .containsExactlyInAnyOrder("Rust", "Noir");
    }

    @Test
    void should_fail_when_multiple_answers_for_single_choice_question() {
        // When trying to save multiple answers for a single-choice question
        client.put()
                .uri(getApiURI("/api/v1/me/reco/projects/matching-questions/4f52195c-1c13-4c54-9132-a89e73e4c69d/answers"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + pierre.jwt())
                .bodyValue(new SaveMatchingAnswersRequest().answerIndexes(List.of(1, 2))) // Intermediate
                // and
                // Advanced
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void should_fail_when_question_not_found() {
        client.put()
                .uri(getApiURI("/api/v1/me/reco/projects/matching-questions/00000000-0000-0000-0000-000000000000/answers"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + pierre.jwt())
                .bodyValue(new SaveMatchingAnswersRequest().answerIndexes(List.of(0)))
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void should_fail_when_not_authenticated() {
        client.put()
                .uri(getApiURI("/api/v1/me/reco/projects/matching-questions/4f52195c-1c13-4c54-9132-a89e73e4c69d/answers"))
                .bodyValue(new SaveMatchingAnswersRequest().answerIndexes(List.of(0)))
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }
}