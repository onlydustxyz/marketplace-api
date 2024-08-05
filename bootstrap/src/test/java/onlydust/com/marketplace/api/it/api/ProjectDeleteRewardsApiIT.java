package onlydust.com.marketplace.api.it.api;

import com.onlydust.customer.io.adapter.properties.CustomerIOProperties;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.service.BillingProfileService;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import onlydust.com.marketplace.api.read.repositories.BillingProfileReadRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.api.suites.tags.TagReward;
import onlydust.com.marketplace.kernel.model.bank.BankAccount;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Name;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;
import onlydust.com.marketplace.project.domain.port.output.ProjectRewardStoragePort;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

@TagReward
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectDeleteRewardsApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    public ProjectRepository projectRepository;

    @Autowired
    AuthenticatedAppUserService authenticatedAppUserService;
    @Autowired
    CustomerIOProperties customerIOProperties;
    @Autowired
    BillingProfileReadRepository billingProfileReadRepository;

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
        userAuthHelper.signUpUser(1L, faker.rickAndMorty().character(), faker.internet().url(),
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

    @Test
    @Order(4)
    void should_prevent_cancel_reward_action_given_a_reward_already_in_an_invoice() throws IOException, InterruptedException {
        // Given
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
        final String jwt = pierre.jwt();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
        final UUID userId = pierre.user().getId();
        final var rewardId = UUID.fromString("8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0");

        final var billingProfile = billingProfileReadRepository.findByUserId(userId).stream().findFirst().orElseThrow();
        final var billingProfileId = BillingProfile.Id.of(billingProfile.id());
        accountingHelper.patchBillingProfile(billingProfileId.value(), null, VerificationStatus.VERIFIED);
        kybRepository.findByBillingProfileId(billingProfileId.value())
                .ifPresent(kyb -> kybRepository.save(kyb.toBuilder()
                        .country("FRA")
                        .address("1 Infinite Loop, Cupertino, CA 95014, United States")
                        .euVATNumber("FR12345678901")
                        .name("Apple Inc.")
                        .registrationDate(faker.date().birthday())
                        .registrationNumber("123456789")
                        .usEntity(false)
                        .subjectToEuVAT(true)
                        .verificationStatus(VerificationStatus.VERIFIED).build()));
        billingProfileService.updatePayoutInfo(billingProfileId, UserId.of(userId),
                PayoutInfo.builder().ethWallet(new WalletLocator(new Name("foobar.eth")))
                        .bankAccount(new BankAccount("BIC", "FR76000111222333334444")).build());
        billingProfileService.acceptInvoiceMandate(UserId.of(userId), billingProfileId);
        final Invoice.Id invoiceId = billingProfileService.previewInvoice(UserId.of(userId), billingProfileId, List.of(RewardId.of(rewardId))).id();
        billingProfileService.uploadGeneratedInvoice(UserId.of(userId), billingProfileId, invoiceId,
                new FileSystemResource(Objects.requireNonNull(getClass().getResource("/invoices/invoice-sample.pdf")).getFile()).getInputStream());

        // When
        client.delete()
                .uri(getApiURI(String.format(PROJECTS_REWARD, projectId, rewardId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isForbidden()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Reward %s cannot be cancelled because it is included in an invoice".formatted(rewardId));
    }

    @Autowired
    ProjectRewardStoragePort projectRewardStoragePort;

    @Test
    @Order(5)
    void should_delete_reward() {
        // Given
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
        final String jwt = pierre.jwt();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
        final var rewardId = UUID.fromString("e1498a17-5090-4071-a88a-6f0b0c337c3a");

        // When
        client.delete()
                .uri(getApiURI(String.format(PROJECTS_REWARD, projectId, rewardId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        customerIOWireMockServer.verify(1,
                postRequestedFor(urlEqualTo("/send/email"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer %s".formatted(customerIOProperties.getApiKey())))
                        .withRequestBody(matchingJsonPath("$.transactional_message_id", equalTo(customerIOProperties.getRewardCanceledEmailId().toString())))
                        .withRequestBody(matchingJsonPath("$.identifiers.id", equalTo(pierre.user().getId().toString())))
                        .withRequestBody(matchingJsonPath("$.message_data.amount", equalTo("1000.000")))
                        .withRequestBody(matchingJsonPath("$.message_data.username", equalTo(pierre.user().getGithubLogin())))
                        .withRequestBody(matchingJsonPath("$.message_data.currency", equalTo("USDC")))
                        .withRequestBody(matchingJsonPath("$.message_data.rewardName", equalTo("#E1498 - QA new contributions")))
                        .withRequestBody(matchingJsonPath("$.to", equalTo(pierre.user().getEmail())))
                        .withRequestBody(matchingJsonPath("$.from", equalTo(customerIOProperties.getOnlyDustAdminEmail())))
                        .withRequestBody(matchingJsonPath("$.subject", equalTo("Reward #E1498 - QA new contributions got canceled"))));
    }
}
