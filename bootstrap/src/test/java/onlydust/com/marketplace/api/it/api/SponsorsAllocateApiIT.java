package onlydust.com.marketplace.api.it.api;

import lombok.NonNull;
import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagProject;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import onlydust.com.marketplace.project.domain.model.notification.FundsAllocatedToProgram;
import onlydust.com.marketplace.project.domain.model.notification.FundsUnallocatedFromProgram;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

@TagProject
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SponsorsAllocateApiIT extends AbstractMarketplaceApiIT {
    private final static SponsorId sponsorId = SponsorId.of("58a0a05c-c81e-447c-910f-629817a987b8");
    private ProgramId programId;
    private final static Currency.Id currencyId = Currency.Id.of("562bbf65-8a71-4d30-ad63-520c0d68ba27");
    private UserAuthHelper.AuthenticatedUser user;
    private UserAuthHelper.AuthenticatedUser programLead;

    @Autowired
    private NotificationPort notificationPort;

    @BeforeEach
    void setup() {
        user = userAuthHelper.authenticateAntho();
        programLead = userAuthHelper.authenticateOlivier();
        programId = programHelper.create(sponsorId, programLead).id();
    }

    @Test
    @Order(1)
    void should_return_forbidden_if_not_admin() {
        allocateTo(sponsorId, programId, currencyId, 100)
                .expectStatus()
                .isForbidden();
    }

    @Test
    @Order(2)
    void should_not_allocate_if_no_budget() {
        // Given
        addSponsorFor(user, sponsorId.value());

        // When
        allocateTo(sponsorId, programId, currencyId, 100)
                .expectStatus()
                .isBadRequest();

        // TODO - add call to sponsor stats endpoint
        // Then
//        getSponsor(sponsorId)
//                .expectStatus()
//                .isOk()
//                .expectBody()
//                .jsonPath("$.availableBudgets.size()").isEqualTo(1)
//                .jsonPath("$.availableBudgets[0].amount").isEqualTo(0)
//                .jsonPath("$.projects.size()").isEqualTo(1)
//                .jsonPath("$.projects[0].remainingBudgets.size()").isEqualTo(1)
//                .jsonPath("$.projects[0].remainingBudgets[0].amount").isEqualTo(17000)
//        ;
    }

    @SneakyThrows
    @Test
    @Order(3)
    void should_unallocate() {
        // Given
        accountingHelper.createSponsorAccount(sponsorId, 1000, currencyId);
        allocateTo(sponsorId, programId, currencyId, 1000)
                .expectStatus()
                .isNoContent();
        verify(notificationPort).push(any(), any(FundsAllocatedToProgram.class));

        // When
        unallocateFrom(sponsorId, programId, currencyId, 1000)
                .expectStatus()
                .isNoContent();
        verify(notificationPort).push(any(), any(FundsUnallocatedFromProgram.class));

        Thread.sleep(200);
        customerIOWireMockServer.verify(1,
                postRequestedFor(urlEqualTo("/send/email"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer %s".formatted(customerIOProperties.getApiKey())))
                        .withRequestBody(matchingJsonPath("$.transactional_message_id",
                                equalTo(customerIOProperties.getFundsUnallocatedFromProgramEmailId().toString())))
                        .withRequestBody(matchingJsonPath("$.identifiers.id", equalTo(user.user().getId().toString())))
                        .withRequestBody(matchingJsonPath("$.message_data.username", equalTo(user.user().getGithubLogin())))
                        .withRequestBody(matchingJsonPath("$.message_data.title", equalTo("Allocation returned from program")))
                        .withRequestBody(matchingJsonPath("$.message_data.description", equalTo(("An allocation has been returned to you from a program. The " +
                                                                                                 "funds have been credited back to your account. You can " +
                                                                                                 "review the details of this transaction dashboard."))))
                        .withRequestBody(matchingJsonPath("$.message_data.button.text", equalTo("Review transaction details")))
                        .withRequestBody(matchingJsonPath("$.message_data.button.link",
                                equalTo("https://develop-admin.onlydust.com/financials/%s".formatted(sponsorId))))
                        .withRequestBody(matchingJsonPath("$.to", equalTo(user.user().getEmail())))
                        .withRequestBody(matchingJsonPath("$.subject", equalTo("Allocation returned from program"))));

        // TODO - add call to sponsor stats endpoint
        // Then
//        getSponsor(sponsorId)
//                .expectStatus()
//                .isOk()
//                .expectBody()
//                .jsonPath("$.availableBudgets.size()").isEqualTo(1)
//                .jsonPath("$.availableBudgets[0].amount").isEqualTo(15000)
//                .jsonPath("$.projects.size()").isEqualTo(1)
//                .jsonPath("$.projects[0].remainingBudgets.size()").isEqualTo(1)
//                .jsonPath("$.projects[0].remainingBudgets[0].amount").isEqualTo(2000)
//        ;
    }

    @SneakyThrows
    @Test
    @Order(4)
    void should_allocate() {
        // Given
        addSponsorFor(user, sponsorId.value());
        accountingHelper.createSponsorAccount(sponsorId, 1000, currencyId);

        // When
        allocateTo(sponsorId, programId, currencyId, 1000)
                .expectStatus()
                .isNoContent();
        verify(notificationPort).push(any(), any(FundsAllocatedToProgram.class));

        Thread.sleep(200);
        customerIOWireMockServer.verify(1,
                postRequestedFor(urlEqualTo("/send/email"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer %s".formatted(customerIOProperties.getApiKey())))
                        .withRequestBody(matchingJsonPath("$.transactional_message_id",
                                equalTo(customerIOProperties.getFundsAllocatedToProgramEmailId().toString())))
                        .withRequestBody(matchingJsonPath("$.identifiers.id", equalTo(programLead.user().getId().toString())))
                        .withRequestBody(matchingJsonPath("$.message_data.username", equalTo(programLead.user().getGithubLogin())))
                        .withRequestBody(matchingJsonPath("$.message_data.title", equalTo("New allocation received")))
                        .withRequestBody(matchingJsonPath("$.message_data.description", equalTo(("We are pleased to inform you that a new allocation has been" +
                                                                                                 " granted to you. You can now view the details of this " +
                                                                                                 "allocation in your personal account."))))
                        .withRequestBody(matchingJsonPath("$.message_data.button.text", equalTo("Review allocation")))
                        .withRequestBody(matchingJsonPath("$.message_data.button.link",
                                equalTo("https://develop-admin.onlydust.com/programs/%s".formatted(programId))))
                        .withRequestBody(matchingJsonPath("$.to", equalTo(programLead.user().getEmail())))
                        .withRequestBody(matchingJsonPath("$.subject", equalTo("New allocation received"))));

        // TODO - add call to sponsor stats endpoint
        // Then
//        getSponsor(sponsorId)
//                .expectStatus()
//                .isOk()
//                .expectBody()
//                .jsonPath("$.availableBudgets.size()").isEqualTo(1)
//                .jsonPath("$.availableBudgets[0].amount").isEqualTo(14000)
//                .jsonPath("$.projects.size()").isEqualTo(1)
//                .jsonPath("$.projects[0].remainingBudgets.size()").isEqualTo(1)
//                .jsonPath("$.projects[0].remainingBudgets[0].amount").isEqualTo(3000)
//        ;
    }

    @Test
    @Order(5)
    void should_not_unallocate_if_no_budget() {
        // Given
        addSponsorFor(user, sponsorId.value());

        // When
        unallocateFrom(sponsorId, programId, currencyId, 5000)
                .expectStatus()
                .isBadRequest();
    }

    @NonNull
    private WebTestClient.ResponseSpec allocateTo(SponsorId sponsorId, ProgramId programId, Currency.Id currencyId, long amount) {
        reset(notificationPort);
        return client.post()
                .uri(SPONSOR_ALLOCATE.formatted(sponsorId))
                .header("Authorization", "Bearer " + user.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "programId": "%s",
                          "currencyId": "%s",
                          "amount": %d
                        }
                        """.formatted(programId, currencyId, amount))
                .exchange();
    }

    @NonNull
    private WebTestClient.ResponseSpec unallocateFrom(SponsorId sponsorId, ProgramId programId, Currency.Id currencyId, long amount) {
        reset(notificationPort);
        return client.post()
                .uri(SPONSOR_UNALLOCATE.formatted(sponsorId))
                .header("Authorization", "Bearer " + user.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "programId": "%s",
                          "currencyId": "%s",
                          "amount": %d
                        }
                        """.formatted(programId, currencyId, amount))
                .exchange();
    }
}
