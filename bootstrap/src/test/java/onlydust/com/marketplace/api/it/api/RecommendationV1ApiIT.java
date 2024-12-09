package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.contract.model.MatchingAnswerResponse;
import onlydust.com.marketplace.api.contract.model.MatchingQuestionsResponse;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagRecommendation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

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
        final var response =
                client.get()
                        .uri(getApiURI("/api/v1/me/reco/projects/matching-questions", Map.of("v", RECOMMENDER_SYSTEM_VERSION)))
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
        assertThat(primaryGoalsQuestion.getBody()).isEqualTo("What are your primary goals for contributing to open-source projects?");
        assertThat(primaryGoalsQuestion.getDescription()).contains("goals help us understand what motivates you");
        assertThat(primaryGoalsQuestion.getMultipleChoice()).isTrue();
        assertThat(primaryGoalsQuestion.getAnswers()).extracting(MatchingAnswerResponse::getBody).containsExactlyInAnyOrder("Learning new skills",
                "Building a professional network", "Gaining practical experience", "Supporting meaningful projects", "Earning recognition in the community");

        // Check experience level question (index 2)
        final var experienceQuestion = response.getQuestions().get(2);
        assertThat(experienceQuestion.getBody()).isEqualTo("How would you rate your experience in software development?");
        assertThat(experienceQuestion.getDescription()).contains("recommend projects matching your experience level");
        assertThat(experienceQuestion.getMultipleChoice()).isFalse();
        assertThat(experienceQuestion.getAnswers()).extracting(MatchingAnswerResponse::getBody).containsExactly("Beginner", "Intermediate", "Advanced",
                "Expert");

        // Check blockchain ecosystems question (index 4)
        final var blockchainQuestion = response.getQuestions().get(4);
        assertThat(blockchainQuestion.getBody()).isEqualTo("Which blockchain ecosystems are you interested in or curious about?");
        assertThat(blockchainQuestion.getDescription()).contains("match you with projects in your preferred blockchain ecosystems");
        assertThat(blockchainQuestion.getMultipleChoice()).isTrue();
        assertThat(blockchainQuestion.getAnswers()).extracting(MatchingAnswerResponse::getBody).containsExactlyInAnyOrder("Ethereum", "Solana", "Polkadot",
                "Cosmos", "Avalanche", "Bitcoin", "Don't know");
    }
}