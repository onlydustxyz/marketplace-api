package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.accounting.domain.model.billingprofile.CompanyBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.IndividualBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.SelfEmployedBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.service.BillingProfileService;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProjectVisibilityEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CurrencyRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PayoutPreferenceApiIT extends AbstractMarketplaceApiIT {

    @Test
    void should_be_authorized() {
        // When
        client.get()
                .uri(getApiURI(ME_GET_PAYOUT_PREFERENCES))
                // Then
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Autowired
    BillingProfileService billingProfileService;
    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    RewardRepository rewardRepository;
    @Autowired
    CurrencyRepository currencyRepository;

    @Test
    void should_get_and_put_payout_preferences() {
        // Given
        final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.newFakeUser(UUID.randomUUID(),
                faker.number().randomNumber() + faker.number().randomNumber(), faker.name().name(),
                faker.internet().url(), false);
        final UserId userId = UserId.of(authenticatedUser.user().getId());

        // When
        client.get()
                .uri(getApiURI(ME_GET_PAYOUT_PREFERENCES))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("[]");

        final CompanyBillingProfile companyBillingProfile = billingProfileService.createCompanyBillingProfile(userId, faker.rickAndMorty().character(), null);
        final IndividualBillingProfile individualBillingProfile = billingProfileService.createIndividualBillingProfile(userId,
                faker.rickAndMorty().location(), null);
        final SelfEmployedBillingProfile selfEmployedBillingProfile = billingProfileService.createSelfEmployedBillingProfile(userId,
                faker.rickAndMorty().quote(), null);

        final List<ProjectEntity> projectEntities = Stream.of(generateStubForProject(), generateStubForProject(), generateStubForProject())
                .sorted(Comparator.comparing(ProjectEntity::getName))
                .toList();

        final var STRK = currencyRepository.findByCode("STRK").orElseThrow();

        final RewardEntity r1 = rewardRepository.save(new RewardEntity(UUID.randomUUID(), projectEntities.get(0).getId(), userId.value(),
                authenticatedUser.user().getGithubUserId(),
                STRK, BigDecimal.ONE, new Date(), List.of(), null, null, null, null));
        final RewardEntity r2 = rewardRepository.save(new RewardEntity(UUID.randomUUID(), projectEntities.get(1).getId(), userId.value(),
                authenticatedUser.user().getGithubUserId(),
                STRK, BigDecimal.ONE, new Date(), List.of(), null, null, null, null));
        final RewardEntity r3 = rewardRepository.save(new RewardEntity(UUID.randomUUID(), projectEntities.get(2).getId(), userId.value(),
                authenticatedUser.user().getGithubUserId(),
                STRK, BigDecimal.ONE, new Date(), List.of(), null, null, null, null));

        // When
        client.get()
                .uri(getApiURI(ME_GET_PAYOUT_PREFERENCES))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[0].project.name").isEqualTo(projectEntities.get(0).getName())
                .jsonPath("$[0].project.slug").isEqualTo(projectEntities.get(0).getKey())
                .jsonPath("$[0].project.logoUrl").isEqualTo(projectEntities.get(0).getLogoUrl())
                .jsonPath("$[0].project.shortDescription").isEqualTo(projectEntities.get(0).getShortDescription())
                .jsonPath("$[0].billingProfile").isEmpty()

                .jsonPath("$[1].project.name").isEqualTo(projectEntities.get(1).getName())
                .jsonPath("$[1].project.slug").isEqualTo(projectEntities.get(1).getKey())
                .jsonPath("$[1].project.logoUrl").isEqualTo(projectEntities.get(1).getLogoUrl())
                .jsonPath("$[1].project.shortDescription").isEqualTo(projectEntities.get(1).getShortDescription())
                .jsonPath("$[1].billingProfile").isEmpty()

                .jsonPath("$[2].project.name").isEqualTo(projectEntities.get(2).getName())
                .jsonPath("$[2].project.slug").isEqualTo(projectEntities.get(2).getKey())
                .jsonPath("$[2].project.logoUrl").isEqualTo(projectEntities.get(2).getLogoUrl())
                .jsonPath("$[2].project.shortDescription").isEqualTo(projectEntities.get(2).getShortDescription())
                .jsonPath("$[2].billingProfile").isEmpty();


        // when
        client.put()
                .uri(getApiURI(ME_PUT_PAYOUT_PREFERENCES))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "billingProfileId": "%s",
                          "projectId": "%s"
                        }
                        """.formatted(companyBillingProfile.id().value(), projectEntities.get(0).getId()))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        assertEquals(companyBillingProfile.id().value(), rewardRepository.findById(r1.id()).orElseThrow().billingProfileId());

        // When
        client.get()
                .uri(getApiURI(ME_GET_PAYOUT_PREFERENCES))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[0].project.name").isEqualTo(projectEntities.get(0).getName())
                .jsonPath("$[0].project.slug").isEqualTo(projectEntities.get(0).getKey())
                .jsonPath("$[0].project.logoUrl").isEqualTo(projectEntities.get(0).getLogoUrl())
                .jsonPath("$[0].project.shortDescription").isEqualTo(projectEntities.get(0).getShortDescription())
                .jsonPath("$[0].billingProfile.name").isEqualTo(companyBillingProfile.name())
                .jsonPath("$[0].billingProfile.id").isEqualTo(companyBillingProfile.id().value().toString())
                .jsonPath("$[0].billingProfile.type").isEqualTo(companyBillingProfile.type().name())

                .jsonPath("$[1].project.name").isEqualTo(projectEntities.get(1).getName())
                .jsonPath("$[1].project.slug").isEqualTo(projectEntities.get(1).getKey())
                .jsonPath("$[1].project.logoUrl").isEqualTo(projectEntities.get(1).getLogoUrl())
                .jsonPath("$[1].project.shortDescription").isEqualTo(projectEntities.get(1).getShortDescription())
                .jsonPath("$[1].billingProfile").isEmpty()

                .jsonPath("$[2].project.name").isEqualTo(projectEntities.get(2).getName())
                .jsonPath("$[2].project.slug").isEqualTo(projectEntities.get(2).getKey())
                .jsonPath("$[2].project.logoUrl").isEqualTo(projectEntities.get(2).getLogoUrl())
                .jsonPath("$[2].project.shortDescription").isEqualTo(projectEntities.get(2).getShortDescription())
                .jsonPath("$[2].billingProfile").isEmpty();

        // when
        client.put()
                .uri(getApiURI(ME_PUT_PAYOUT_PREFERENCES))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "billingProfileId": "%s",
                          "projectId": "%s"
                        }
                        """.formatted(selfEmployedBillingProfile.id().value(), projectEntities.get(1).getId()))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        assertEquals(selfEmployedBillingProfile.id().value(), rewardRepository.findById(r2.id()).orElseThrow().billingProfileId());

        // When
        client.get()
                .uri(getApiURI(ME_GET_PAYOUT_PREFERENCES))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[0].project.name").isEqualTo(projectEntities.get(0).getName())
                .jsonPath("$[0].project.slug").isEqualTo(projectEntities.get(0).getKey())
                .jsonPath("$[0].project.logoUrl").isEqualTo(projectEntities.get(0).getLogoUrl())
                .jsonPath("$[0].project.shortDescription").isEqualTo(projectEntities.get(0).getShortDescription())
                .jsonPath("$[0].billingProfile.name").isEqualTo(companyBillingProfile.name())
                .jsonPath("$[0].billingProfile.id").isEqualTo(companyBillingProfile.id().value().toString())
                .jsonPath("$[0].billingProfile.type").isEqualTo(companyBillingProfile.type().name())

                .jsonPath("$[1].project.name").isEqualTo(projectEntities.get(1).getName())
                .jsonPath("$[1].project.slug").isEqualTo(projectEntities.get(1).getKey())
                .jsonPath("$[1].project.logoUrl").isEqualTo(projectEntities.get(1).getLogoUrl())
                .jsonPath("$[1].project.shortDescription").isEqualTo(projectEntities.get(1).getShortDescription())
                .jsonPath("$[1].billingProfile.name").isEqualTo(selfEmployedBillingProfile.name())
                .jsonPath("$[1].billingProfile.id").isEqualTo(selfEmployedBillingProfile.id().value().toString())
                .jsonPath("$[1].billingProfile.type").isEqualTo(selfEmployedBillingProfile.type().name())

                .jsonPath("$[2].project.name").isEqualTo(projectEntities.get(2).getName())
                .jsonPath("$[2].project.slug").isEqualTo(projectEntities.get(2).getKey())
                .jsonPath("$[2].project.logoUrl").isEqualTo(projectEntities.get(2).getLogoUrl())
                .jsonPath("$[2].project.shortDescription").isEqualTo(projectEntities.get(2).getShortDescription())
                .jsonPath("$[2].billingProfile").isEmpty();

        // when
        client.put()
                .uri(getApiURI(ME_PUT_PAYOUT_PREFERENCES))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "billingProfileId": "%s",
                          "projectId": "%s"
                        }
                        """.formatted(individualBillingProfile.id().value(), projectEntities.get(2).getId()))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        assertEquals(individualBillingProfile.id().value(), rewardRepository.findById(r3.id()).orElseThrow().billingProfileId());

        // When
        client.get()
                .uri(getApiURI(ME_GET_PAYOUT_PREFERENCES))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[0].project.name").isEqualTo(projectEntities.get(0).getName())
                .jsonPath("$[0].project.slug").isEqualTo(projectEntities.get(0).getKey())
                .jsonPath("$[0].project.logoUrl").isEqualTo(projectEntities.get(0).getLogoUrl())
                .jsonPath("$[0].project.shortDescription").isEqualTo(projectEntities.get(0).getShortDescription())
                .jsonPath("$[0].billingProfile.name").isEqualTo(companyBillingProfile.name())
                .jsonPath("$[0].billingProfile.id").isEqualTo(companyBillingProfile.id().value().toString())
                .jsonPath("$[0].billingProfile.type").isEqualTo(companyBillingProfile.type().name())

                .jsonPath("$[1].project.name").isEqualTo(projectEntities.get(1).getName())
                .jsonPath("$[1].project.slug").isEqualTo(projectEntities.get(1).getKey())
                .jsonPath("$[1].project.logoUrl").isEqualTo(projectEntities.get(1).getLogoUrl())
                .jsonPath("$[1].project.shortDescription").isEqualTo(projectEntities.get(1).getShortDescription())
                .jsonPath("$[1].billingProfile.name").isEqualTo(selfEmployedBillingProfile.name())
                .jsonPath("$[1].billingProfile.id").isEqualTo(selfEmployedBillingProfile.id().value().toString())
                .jsonPath("$[1].billingProfile.type").isEqualTo(selfEmployedBillingProfile.type().name())

                .jsonPath("$[2].project.name").isEqualTo(projectEntities.get(2).getName())
                .jsonPath("$[2].project.slug").isEqualTo(projectEntities.get(2).getKey())
                .jsonPath("$[2].project.logoUrl").isEqualTo(projectEntities.get(2).getLogoUrl())
                .jsonPath("$[2].project.shortDescription").isEqualTo(projectEntities.get(2).getShortDescription())
                .jsonPath("$[2].billingProfile.name").isEqualTo(individualBillingProfile.name())
                .jsonPath("$[2].billingProfile.id").isEqualTo(individualBillingProfile.id().value().toString())
                .jsonPath("$[2].billingProfile.type").isEqualTo(individualBillingProfile.type().name());

        // When
        client.put()
                .uri(getApiURI(BILLING_PROFILES_ENABLE_BY_ID.formatted(companyBillingProfile.id().value())))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "enable": false
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        assertNull(rewardRepository.findById(r1.id()).orElseThrow().billingProfileId());

        // When
        client.get()
                .uri(getApiURI(ME_GET_PAYOUT_PREFERENCES))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[0].project.name").isEqualTo(projectEntities.get(0).getName())
                .jsonPath("$[0].project.slug").isEqualTo(projectEntities.get(0).getKey())
                .jsonPath("$[0].project.logoUrl").isEqualTo(projectEntities.get(0).getLogoUrl())
                .jsonPath("$[0].project.shortDescription").isEqualTo(projectEntities.get(0).getShortDescription())
                .jsonPath("$[0].billingProfile").isEmpty()

                .jsonPath("$[1].project.name").isEqualTo(projectEntities.get(1).getName())
                .jsonPath("$[1].project.slug").isEqualTo(projectEntities.get(1).getKey())
                .jsonPath("$[1].project.logoUrl").isEqualTo(projectEntities.get(1).getLogoUrl())
                .jsonPath("$[1].project.shortDescription").isEqualTo(projectEntities.get(1).getShortDescription())
                .jsonPath("$[1].billingProfile.name").isEqualTo(selfEmployedBillingProfile.name())
                .jsonPath("$[1].billingProfile.id").isEqualTo(selfEmployedBillingProfile.id().value().toString())
                .jsonPath("$[1].billingProfile.type").isEqualTo(selfEmployedBillingProfile.type().name())

                .jsonPath("$[2].project.name").isEqualTo(projectEntities.get(2).getName())
                .jsonPath("$[2].project.slug").isEqualTo(projectEntities.get(2).getKey())
                .jsonPath("$[2].project.logoUrl").isEqualTo(projectEntities.get(2).getLogoUrl())
                .jsonPath("$[2].project.shortDescription").isEqualTo(projectEntities.get(2).getShortDescription())
                .jsonPath("$[2].billingProfile.name").isEqualTo(individualBillingProfile.name())
                .jsonPath("$[2].billingProfile.id").isEqualTo(individualBillingProfile.id().value().toString())
                .jsonPath("$[2].billingProfile.type").isEqualTo(individualBillingProfile.type().name());

        // when
        client.put()
                .uri(getApiURI(ME_PUT_PAYOUT_PREFERENCES))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "billingProfileId": "%s",
                          "projectId": "%s"
                        }
                        """.formatted(companyBillingProfile.id().value(), projectEntities.get(0).getId()))
                // Then
                .exchange()
                .expectStatus()
                .is4xxClientError();

        // when
        client.put()
                .uri(getApiURI(ME_PUT_PAYOUT_PREFERENCES))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "billingProfileId": "%s",
                          "projectId": "%s"
                        }
                        """.formatted(individualBillingProfile.id().value(), projectEntities.get(0).getId()))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // When
        client.put()
                .uri(getApiURI(BILLING_PROFILES_ENABLE_BY_ID.formatted(companyBillingProfile.id().value())))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "enable": true
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // When
        client.put()
                .uri(getApiURI(ME_PUT_PAYOUT_PREFERENCES))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "billingProfileId": "%s",
                          "projectId": "%s"
                        }
                        """.formatted(companyBillingProfile.id().value(), projectEntities.get(0).getId()))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        assertEquals(companyBillingProfile.id().value(), rewardRepository.findById(r1.id()).orElseThrow().billingProfileId());
    }


    private ProjectEntity generateStubForProject() {
        final ProjectEntity projectEntity = projectRepository.save(new ProjectEntity(UUID.randomUUID(),
                faker.gameOfThrones().character() + faker.number().randomNumber(),
                faker.rickAndMorty().character(), faker.pokemon().name(), null, faker.internet().url(), false, 0, null, ProjectVisibilityEnumEntity.PRIVATE,
                false, false, false, new Date(), null, null, null, null, null, null, null));
        // To get the slug
        return projectRepository.findById(projectEntity.getId()).orElseThrow();
    }
}
