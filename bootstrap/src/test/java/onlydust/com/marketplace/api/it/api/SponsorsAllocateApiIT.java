package onlydust.com.marketplace.api.it.api;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagProject;
import onlydust.com.marketplace.project.domain.model.Sponsor;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@TagProject
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SponsorsAllocateApiIT extends AbstractMarketplaceApiIT {
    private final static Sponsor.Id sponsorId = Sponsor.Id.of("58a0a05c-c81e-447c-910f-629817a987b8");
    private final static ProjectId projectId = ProjectId.of("45ca43d6-130e-4bf7-9776-2b1eb1dcb782");
    private final static Currency.Id currencyId = Currency.Id.of("562bbf65-8a71-4d30-ad63-520c0d68ba27");
    private UserAuthHelper.AuthenticatedUser user;

    @BeforeEach
    void setup() {
        user = userAuthHelper.authenticateAntho();
    }

    @Test
    @Order(1)
    void should_return_forbidden_if_not_admin() {
        allocateTo(sponsorId, projectId, currencyId, 100)
                .expectStatus()
                .isForbidden();
    }

    @Test
    @Order(2)
    void should_not_allocate_if_no_budget() {
        // Given
        addSponsorFor(user, sponsorId.value());

        // When
        allocateTo(sponsorId, projectId, currencyId, 100)
                .expectStatus()
                .isBadRequest();

        // Then
        getSponsor(sponsorId)
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.availableBudgets.size()").isEqualTo(1)
                .jsonPath("$.availableBudgets[0].amount").isEqualTo(0)
                .jsonPath("$.projects.size()").isEqualTo(1)
                .jsonPath("$.projects[0].remainingBudgets.size()").isEqualTo(1)
                .jsonPath("$.projects[0].remainingBudgets[0].amount").isEqualTo(17000)
        ;
    }

    @Test
    @Order(3)
    void should_unallocate() {
        // When
        unallocateFrom(sponsorId, projectId, currencyId, 15000)
                .expectStatus()
                .isNoContent();

        // Then
        getSponsor(sponsorId)
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.availableBudgets.size()").isEqualTo(1)
                .jsonPath("$.availableBudgets[0].amount").isEqualTo(15000)
                .jsonPath("$.projects.size()").isEqualTo(1)
                .jsonPath("$.projects[0].remainingBudgets.size()").isEqualTo(1)
                .jsonPath("$.projects[0].remainingBudgets[0].amount").isEqualTo(2000)
        ;
    }

    @Test
    @Order(4)
    void should_allocate() {
        // Given
        addSponsorFor(user, sponsorId.value());

        // When
        allocateTo(sponsorId, projectId, currencyId, 1000)
                .expectStatus()
                .isNoContent();

        // Then
        getSponsor(sponsorId)
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.availableBudgets.size()").isEqualTo(1)
                .jsonPath("$.availableBudgets[0].amount").isEqualTo(14000)
                .jsonPath("$.projects.size()").isEqualTo(1)
                .jsonPath("$.projects[0].remainingBudgets.size()").isEqualTo(1)
                .jsonPath("$.projects[0].remainingBudgets[0].amount").isEqualTo(3000)
        ;
    }

    @Test
    @Order(5)
    void should_not_unallocate_if_no_budget() {
        // Given
        addSponsorFor(user, sponsorId.value());

        // When
        unallocateFrom(sponsorId, projectId, currencyId, 5000)
                .expectStatus()
                .isBadRequest();
    }

    @NonNull
    private WebTestClient.ResponseSpec getSponsor(Sponsor.Id id) {
        return client.get()
                .uri(SPONSOR.formatted(id))
                .header("Authorization", "Bearer " + user.jwt())
                .exchange();
    }

    @NonNull
    private WebTestClient.ResponseSpec allocateTo(Sponsor.Id sponsorId, ProjectId projectId, Currency.Id currencyId, long amount) {
        return client.post()
                .uri(SPONSOR_ALLOCATE.formatted(sponsorId))
                .header("Authorization", "Bearer " + user.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "projectId": "%s",
                          "currencyId": "%s",
                          "amount": %d
                        }
                        """.formatted(projectId, currencyId, amount))
                .exchange();
    }

    @NonNull
    private WebTestClient.ResponseSpec unallocateFrom(Sponsor.Id sponsorId, ProjectId projectId, Currency.Id currencyId, long amount) {
        return client.post()
                .uri(SPONSOR_UNALLOCATE.formatted(sponsorId))
                .header("Authorization", "Bearer " + user.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "projectId": "%s",
                          "currencyId": "%s",
                          "amount": %d
                        }
                        """.formatted(projectId, currencyId, amount))
                .exchange();
    }
}
