package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.recommendation.ProjectRecommendationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.recommendation.UserAnswersV1Entity;
import onlydust.com.marketplace.api.postgres.adapter.repository.EcosystemRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.LanguageRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRecommendationV1Repository;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserAnswersV1Repository;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.recommendation.MatchingAnswer;
import onlydust.com.marketplace.project.domain.model.recommendation.MatchingQuestion;
import onlydust.com.marketplace.project.domain.port.output.RecommenderSystemPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@Component
@AllArgsConstructor
public class PostgresRecommenderSystemV1Adapter implements RecommenderSystemPort {
    private final UserAnswersV1Repository userAnswersV1Repository;
    private final ProjectRecommendationV1Repository projectRecommendationV1Repository;
    private final LanguageRepository languageRepository;
    private final EcosystemRepository ecosystemRepository;

    @Override
    public boolean isMultipleChoice(final @NonNull MatchingQuestion.Id questionId) {
        return getMatchingQuestions().stream()
                .filter(q -> q.id().equals(questionId))
                .findFirst()
                .map(MatchingQuestion::multipleChoice)
                .orElseThrow(() -> notFound("Question %s not found".formatted(questionId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchingQuestion<?>> getMatchingQuestions(final @NonNull UserId userId) {
        final var userAnswers = userAnswersV1Repository.findById(userId.value())
                .orElse(UserAnswersV1Entity.builder().userId(userId.value()).build());
        return getMatchingQuestions(userAnswers);
    }

    @Override
    @Transactional
    public void saveMatchingAnswers(@NonNull UserId userId, @NonNull MatchingQuestion.Id questionId, @NonNull Set<Integer> chosenAnswerIndexes) {
        final var userAnswers = userAnswersV1Repository.findById(userId.value())
                .orElse(UserAnswersV1Entity.builder().userId(userId.value()).build());
        final var matchingQuestion = getMatchingQuestions(userAnswers).stream()
                .filter(q -> q.id().equals(questionId))
                .findFirst()
                .orElseThrow(() -> notFound("Question %s not found".formatted(questionId)));

        final var chosenAnswers = IntStream.range(0, matchingQuestion.answers().size())
                .filter(chosenAnswerIndexes::contains)
                .mapToObj(i -> matchingQuestion.answers().get(i));

        switch (questionId.toString()) {
            case "b98a375e-3a9d-4b63-a553-4d8d0c31d7c4":
                userAnswers.setPrimaryGoals(chosenAnswers.map(a -> (Integer) a.value()).toArray(Integer[]::new));
                break;
            case "2e44c33e-2c29-4f72-8d03-55aa1b83e3f1":
                userAnswers.setLearningPreference(chosenAnswers.map(a -> (Integer) a.value()).findFirst().orElse(null));
                break;
            case "4f52195c-1c13-4c54-9132-a89e73e4c69d":
                userAnswers.setExperienceLevel(chosenAnswers.map(a -> (Integer) a.value()).findFirst().orElse(null));
                break;
            case "7d052a24-7824-43d8-8e7b-3727c2c1c9b4":
                userAnswers.setLanguages(chosenAnswers.map(a -> (UUID) a.value()).toArray(UUID[]::new));
                break;
            case "e9e8e9b4-3c1a-4c54-9c3e-44c9b2c1c9b4":
                userAnswers.setEcosystems(chosenAnswers.map(a -> (UUID) a.value()).toArray(UUID[]::new));
                break;
            case "f1c2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d":
                userAnswers.setProjectMaturity(chosenAnswers.map(a -> (Integer) a.value()).findFirst().orElse(null));
                break;
            case "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d":
                userAnswers.setCommunityImportance(chosenAnswers.map(a -> (Integer) a.value()).findFirst().orElse(null));
                break;
            case "b1c2d3e4-5f6a-7b8c-9d0e-1f2a3b4c5d6e":
                userAnswers.setLongTermInvolvement(chosenAnswers.map(a -> (Integer) a.value()).findFirst().orElse(null));
                break;
        }
        userAnswersV1Repository.save(userAnswers);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectId> getRecommendedProjects(final @NonNull UserId userId) {
        // Get all potential projects
        final var matchingProjects = projectRecommendationV1Repository.findTopMatchingProjects(userId.value(), 15)
                .stream()
                .map(ProjectRecommendationEntity::getProjectId)
                .toList();
        final var topProjects = projectRecommendationV1Repository.findTopProjects(userId.value(), 3)
                .stream()
                .map(ProjectRecommendationEntity::getProjectId)
                .toList();
        final var lastActiveProjects = projectRecommendationV1Repository.findLastActiveProjects(userId.value(), 3)
                .stream()
                .map(ProjectRecommendationEntity::getProjectId)
                .toList();

        // Start with top matching projects (3)
        var recommendedProjects = completeWith(new HashSet<>(), matchingProjects, 3);

        // Add top projects (up to 3)
        recommendedProjects = completeWith(recommendedProjects, topProjects, 6);

        // Add last active projects (up to 3)
        recommendedProjects = completeWith(recommendedProjects, lastActiveProjects, 9);

        // If we don't have 9 projects yet, complete with remaining matching projects
        recommendedProjects = completeWith(recommendedProjects, matchingProjects, 9);

        return recommendedProjects.stream()
                .map(ProjectId::of)
                .toList();
    }

    @Override
    @Transactional
    public void refreshData() {
        projectRecommendationV1Repository.refreshMaterializedViews();
    }

    private Set<UUID> completeWith(Set<UUID> current, List<UUID> complement, int upTo) {
        return current.size() >= upTo ? current : Stream.concat(current.stream(), complement.stream())
                .collect(HashSet<UUID>::new, HashSet::add, HashSet::addAll)
                .stream()
                .limit(upTo)
                .collect(Collectors.toSet());
    }

    private List<MatchingQuestion<?>> getMatchingQuestions() {
        return getMatchingQuestions(UserAnswersV1Entity.builder().build());
    }

    private List<MatchingQuestion<?>> getMatchingQuestions(final @NonNull UserAnswersV1Entity userAnswers) {
        final var languages = languageRepository.findAll();
        final var ecosystems = ecosystemRepository.findAll();
        return List.of(
                // Question 0: Primary goals
                MatchingQuestion.<Integer>builder()
                        .id(MatchingQuestion.Id.of(UUID.fromString("b98a375e-3a9d-4b63-a553-4d8d0c31d7c4")))
                        .body("What are your primary goals for contributing to open-source projects?")
                        .description(
                                "Your goals help us understand what motivates you and find projects that align with your aspirations.")
                        .multipleChoice(true)
                        .answers(List.of(
                                createAnswer("Learning new skills", 1, userAnswers.primaryGoals()),
                                createAnswer("Building a professional network", 2, userAnswers.primaryGoals()),
                                createAnswer("Gaining practical experience", 3, userAnswers.primaryGoals()),
                                createAnswer("Supporting meaningful projects", 4, userAnswers.primaryGoals()),
                                createAnswer("Earning recognition in the community", 5, userAnswers.primaryGoals())))
                        .build(),
                // Question 1: Learning preference
                MatchingQuestion.<Integer>builder()
                        .id(MatchingQuestion.Id.of(UUID.fromString("2e44c33e-2c29-4f72-8d03-55aa1b83e3f1")))
                        .body("Do you prefer contributing to projects that align with your current skills, challenge you to learn new ones, or both?")
                        .description("This helps us understand your learning preferences and comfort zone.")
                        .multipleChoice(false)
                        .answers(List.of(
                                createAnswer("Align with my skills", 1, userAnswers.learningPreference()),
                                createAnswer("Challenge me to learn", 2, userAnswers.learningPreference()),
                                createAnswer("Both", 0, userAnswers.learningPreference())))
                        .build(),
                // Question 2: Experience level
                MatchingQuestion.<Integer>builder()
                        .id(MatchingQuestion.Id.of(UUID.fromString("4f52195c-1c13-4c54-9132-a89e73e4c69d")))
                        .body("How would you rate your experience in software development?")
                        .description("This helps us recommend projects matching your experience level.")
                        .multipleChoice(false)
                        .answers(List.of(
                                createAnswer("Beginner", 1, userAnswers.experienceLevel()),
                                createAnswer("Intermediate", 2, userAnswers.experienceLevel()),
                                createAnswer("Advanced", 3, userAnswers.experienceLevel()),
                                createAnswer("Expert", 4, userAnswers.experienceLevel())))
                        .build(),
                // Question 3: Programming languages
                MatchingQuestion.<UUID>builder()
                        .id(MatchingQuestion.Id.of(UUID.fromString("7d052a24-7824-43d8-8e7b-3727c2c1c9b4")))
                        .body("Which programming languages are you proficient in or interested in using?")
                        .description("Select the languages you'd like to work with in open source projects.")
                        .multipleChoice(true)
                        .answers(languages.stream().map(l -> createAnswer(l.name(), l.id(), userAnswers.languages())).toList())
                        // languages table
                        .build(),
                // Question 4: Blockchain ecosystems
                MatchingQuestion.<UUID>builder()
                        .id(MatchingQuestion.Id.of(UUID.fromString("e9e8e9b4-3c1a-4c54-9c3e-44c9b2c1c9b4")))
                        .body("Which blockchain ecosystems are you interested in or curious about?")
                        .description("This helps us match you with projects in your preferred blockchain ecosystems.")
                        .multipleChoice(true)
                        .answers(Stream.concat(ecosystems.stream().map(e -> createAnswer(e.getName(), e.getId(), userAnswers.ecosystems())),
                                Stream.of(createAnswer("Don't know", UUID.fromString("00000000-0000-0000-0000-000000000000"), userAnswers.ecosystems()))).toList())
                        .build(),
                // Question 5: Project maturity
                MatchingQuestion.<Integer>builder()
                        .id(MatchingQuestion.Id.of(UUID.fromString("f1c2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d")))
                        .body("Do you prefer working on well-established projects or emerging ones with room for innovation?")
                        .description("Your preference helps us recommend projects at the right stage of development.")
                        .multipleChoice(false)
                        .answers(List.of(
                                createAnswer("Well-established projects", 1, userAnswers.projectMaturity()),
                                createAnswer("Emerging projects", 2, userAnswers.projectMaturity()),
                                createAnswer("No preference", 0, userAnswers.projectMaturity())))
                        .build(),
                // Question 6: Community importance
                MatchingQuestion.<Integer>builder()
                        .id(MatchingQuestion.Id.of(UUID.fromString("a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d")))
                        .body("How important is an active community around an open-source project for you?")
                        .description("This helps us understand how much you value community interaction.")
                        .multipleChoice(false)
                        .answers(List.of(
                                createAnswer("Very important", 2, userAnswers.communityImportance()),
                                createAnswer("Somewhat important", 1, userAnswers.communityImportance()),
                                createAnswer("Not important", 0, userAnswers.communityImportance())))
                        .build(),
                // Question 7: Long-term involvement
                MatchingQuestion.<Integer>builder()
                        .id(MatchingQuestion.Id.of(UUID.fromString("b1c2d3e4-5f6a-7b8c-9d0e-1f2a3b4c5d6e")))
                        .body("How important is long-term project involvement to you?")
                        .description("This helps us understand your preferred engagement duration.")
                        .multipleChoice(false)
                        .answers(List.of(
                                createAnswer("Very important", 2, userAnswers.longTermInvolvement()),
                                createAnswer("Somewhat important", 1, userAnswers.longTermInvolvement()),
                                createAnswer("Not important", 0, userAnswers.longTermInvolvement())))
                        .build());
    }

    private static MatchingAnswer<Integer> createAnswer(String text, int value, Set<Integer> chosenValues) {
        return new MatchingAnswer<>(text, chosenValues.contains(value), value);
    }

    private static MatchingAnswer<UUID> createAnswer(String text, UUID value, Set<UUID> chosenValues) {
        return new MatchingAnswer<>(text, chosenValues.contains(value), value);
    }
}