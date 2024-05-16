package onlydust.com.marketplace.api.postgres.adapter.it.repository;

import onlydust.com.marketplace.api.postgres.adapter.PostgresUserAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.*;
import onlydust.com.marketplace.api.postgres.adapter.it.AbstractPostgresIT;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.*;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
public class AllRepositoriesIT extends AbstractPostgresIT {

    @Autowired
    OnboardingRepository onboardingRepository;
    @Autowired
    ApplicationRepository applicationRepository;
    @Autowired
    UserProfileInfoRepository userProfileInfoRepository;
    @Autowired
    ContactInformationRepository contactInformationRepository;
    @Autowired
    SponsorRepository sponsorRepository;
    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserViewRepository userViewRepository;
    @Autowired
    PostgresUserAdapter postgresUserAdapter;
    @Autowired
    RewardRepository rewardRepository;
    @Autowired
    CustomProjectRankingRepository customProjectRankingRepository;

    @Test
    void should_create_user() {
        final UserEntity expected = UserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().randomNumber())
                .githubLogin(faker.name().name())
                .githubAvatarUrl(faker.internet().avatar())
                .githubEmail(faker.internet().emailAddress())
                .roles(new AuthenticatedUser.Role[]{AuthenticatedUser.Role.USER, AuthenticatedUser.Role.ADMIN})
                .lastSeenAt(new Date())
                .build();

        userRepository.deleteAll();
        assertIsSaved(expected, userRepository);
    }

    @Test
    void should_read_user_onboarding() {
        // Given
        final UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().randomNumber(15, true))
                .githubLogin(faker.name().name())
                .githubAvatarUrl(faker.internet().avatar())
                .githubEmail(faker.internet().emailAddress())
                .roles(new AuthenticatedUser.Role[]{AuthenticatedUser.Role.USER, AuthenticatedUser.Role.ADMIN})
                .lastSeenAt(new Date())
                .build();
        final OnboardingEntity onboarding = OnboardingEntity.builder().id(user.getId())
                .termsAndConditionsAcceptanceDate(new Date())
                .profileWizardDisplayDate(new Date())
                .build();

        userRepository.deleteAll();

        // When
        assertIsSaved(user, userRepository);
        assertIsSaved(onboarding, onboardingRepository);
        final UserViewEntity result = userViewRepository.findById(user.getId()).orElseThrow();

        // Then
        assertEquals(1, userRepository.findAll().size());
        assertEquals(onboarding, result.onboarding());
        assertThat(result.githubUserId()).isEqualTo(user.getGithubUserId());
    }

    @Test
    void should_create_project() {
        // Given
        final List<SponsorEntity> sponsors = List.of(
                SponsorEntity.builder()
                        .id(UUID.randomUUID())
                        .name(faker.name().name())
                        .url(faker.rickAndMorty().location())
                        .logoUrl(faker.rickAndMorty().character())
                        .build(), SponsorEntity.builder()
                        .id(UUID.randomUUID())
                        .name(faker.name().name())
                        .url(faker.rickAndMorty().location())
                        .logoUrl(faker.rickAndMorty().character())
                        .build()
        );
        sponsorRepository.saveAll(sponsors);
        final UUID projectId = UUID.randomUUID();
        final ProjectEntity expected = ProjectEntity.builder()
                .key(faker.address().fullAddress())
                .name(faker.name().name())
                .longDescription(faker.pokemon().location())
                .shortDescription(faker.pokemon().name())
                .logoUrl(faker.rickAndMorty().location())
                .hiring(false)
                .id(projectId)
                .rank(faker.number().randomDigit())
                .visibility(ProjectVisibilityEnumEntity.PUBLIC)
                .ignorePullRequests(true)
                .ignoreCodeReviews(true)
                .ignoreIssues(true)
                .ignoreContributionsBefore(new Date())
                .build();

        assertIsSaved(expected, projectRepository);

        // Should not throw
        customProjectRankingRepository.updateProjectsRanking();
    }

    @Test
    void should_create_onboarding() {
        // Given
        final OnboardingEntity expected = OnboardingEntity.builder().id(UUID.randomUUID())
                .termsAndConditionsAcceptanceDate(new Date())
                .profileWizardDisplayDate(new Date())
                .build();

        assertIsSaved(expected, onboardingRepository);
    }

    @Test
    void should_create_application() {
        // Given
        final ApplicationEntity expected = ApplicationEntity.builder()
                .applicantId(UUID.randomUUID())
                .projectId(UUID.randomUUID())
                .receivedAt(new Date())
                .id(UUID.randomUUID())
                .build();

        assertIsSaved(expected, applicationRepository);
    }

    @Test
    void should_create_user_profile_info() {
        // Given
        final UserProfileInfoEntity expected = UserProfileInfoEntity.builder()
                .weeklyAllocatedTime(AllocatedTimeEnumEntity.one_to_three_days)
                .avatarUrl(faker.pokemon().name())
                .bio(faker.hacker().abbreviation())
                .cover(ProfileCoverEnumEntity.yellow)
                .isLookingForAJob(false)
                .website(faker.harryPotter().location())
                .location(faker.rickAndMorty().location())
                .languages(Map.of(faker.rickAndMorty().location(), 5L, faker.hacker().adjective(), 10L))
                .contactInformations(List.of(ContactInformationEntity.builder()
                        .contact(faker.rickAndMorty().location())
                        .id(ContactInformationIdEntity.builder()
                                .channel(ContactChanelEnumEntity.email)
                                .userId(UUID.randomUUID())
                                .build())
                        .isPublic(false)
                        .build()))
                .isLookingForAJob(true)
                .id(UUID.randomUUID())
                .build();

        assertIsSaved(expected, userProfileInfoRepository);
    }

    @Test
    void should_create_contact_information() {
        // Given
        final ContactInformationEntity expected = ContactInformationEntity.builder()
                .contact(faker.rickAndMorty().location())
                .id(ContactInformationIdEntity.builder()
                        .channel(ContactChanelEnumEntity.email)
                        .userId(UUID.randomUUID())
                        .build())
                .isPublic(false)
                .build();

        assertIsSaved(expected, contactInformationRepository);
    }

    @Test
    void should_create_sponsor() {
        // Given
        final SponsorEntity expected = SponsorEntity.builder()
                .logoUrl(faker.rickAndMorty().location())
                .name(faker.hacker().abbreviation())
                .id(UUID.randomUUID())
                .build();

        assertIsSaved(expected, sponsorRepository);
    }

    private <Entity, ID> void assertIsSaved(Entity expected, JpaRepository<Entity, ID> repository) {
        // When
        final Entity result = repository.save(expected);

        // Then
        assertEquals(1, repository.findAll().size());
        assertEquals(expected, result);
    }
}
