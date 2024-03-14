package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.accounting.domain.service.BillingProfileService;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.project.domain.service.UserService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectDeleteRewardsApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    public ProjectRepository projectRepository;

    @Autowired
    AuthenticatedAppUserService authenticatedAppUserService;

    @Test
    @Order(0)
    public void should_be_unauthorized() {
        final UUID projectId = UUID.randomUUID();
        final UUID rewardId = UUID.randomUUID();

        // When
        client.delete()
                .uri(getApiURI(String.format(PROJECTS_REWARD, projectId, rewardId)))
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(401);
    }

    @Test
    @Order(1)
    void should_be_forbidden_given_authenticated_user_not_project_lead() {
        // Given
        userAuthHelper.newFakeUser(UUID.randomUUID(), 1L, faker.rickAndMorty().character(), faker.internet().url(),
                false);
        final String jwt = userAuthHelper.authenticateUser(1L).jwt();
        final UUID projectId = projectRepository.findAll().get(0).getId();
        final UUID rewardId = UUID.randomUUID();

        // When
        client.delete()
                .uri(getApiURI(String.format(PROJECTS_REWARD, projectId, rewardId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(403)
                .expectBody()
                .jsonPath("$.message").isEqualTo("User must be project lead to cancel a reward");
    }

    @Autowired
    BillingProfileService billingProfileService;
    @Autowired
    UserService userService;

    @Test
    @Order(4)
    void should_prevent_cancel_reward_action_given_a_reward_already_in_an_invoice() throws IOException {
        //TODO
//        // Given
//        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
//        final String jwt = pierre.jwt();
//        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
//        final UUID userId = pierre.user().getId();
//        final var rewardId = UUID.fromString("8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0");
//
//        final OldCompanyBillingProfile companyBillingProfile = userService.getCompanyBillingProfile(userId);
//        userService.updateBillingProfileType(userId, OldBillingProfileType.COMPANY);
//        postgresOldBillingProfileAdapter.saveCompanyProfile(companyBillingProfile.toBuilder()
//                .name(faker.rickAndMorty().character())
//                .address(faker.address().fullAddress())
//                .euVATNumber("111")
//                .subjectToEuropeVAT(false)
//                .oldCountry(OldCountry.fromIso3("FRA"))
//                .usEntity(false)
//                .status(OldVerificationStatus.VERIFIED)
//                .build()
//        );
//        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(companyBillingProfile.getId());
//        billingProfileService.updateInvoiceMandateAcceptanceDate(UserId.of(userId), billingProfileId);
//        final Invoice.Id invoiceId =
//                billingProfileService.previewInvoice(UserId.of(userId), billingProfileId, List.of(RewardId.of(rewardId))).id();
//        billingProfileService.uploadGeneratedInvoice(UserId.of(userId), billingProfileId, invoiceId,
//                new FileSystemResource(Objects.requireNonNull(getClass().getResource("/invoices/invoice-sample.pdf")).getFile()).getInputStream());
//
//        // When
//        client.delete()
//                .uri(getApiURI(String.format(PROJECTS_REWARD, projectId, rewardId)))
//                .header("Authorization", BEARER_PREFIX + jwt)
//                // Then
//                .exchange()
//                .expectStatus()
//                .isEqualTo(403)
//                .expectBody()
//                .jsonPath("$.message").isEqualTo("Cannot cancel reward %s which is already contained in an invoice".formatted(rewardId));
    }
}
