package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.api.domain.service.RewardV2Service;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.HistoricalQuoteEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.SponsorEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CurrencyRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.HistoricalQuoteRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectToBudgetIdRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.BudgetRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.SponsorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;


public class ProjectBudgetsApiV2IT extends AbstractMarketplaceApiIT {


    @Autowired
    ProjectToBudgetIdRepository projectToBudgetIdRepository;
    @Autowired
    BudgetRepository budgetRepository;
    @Autowired
    HistoricalQuoteRepository historicalQuoteRepository;
    @Autowired
    CurrencyRepository currencyRepository;
    @Autowired
    AccountingFacadePort accountingFacadePort;
    @Autowired
    RewardV2Service rewardV2Service;
    @Autowired
    SponsorRepository sponsorRepository;

    @Test
    void should_return_forbidden_status_when_getting_project_budgets_given_user_not_project_lead() {
        // Given
        final String jwt = userAuthHelper.authenticatePierre().jwt();
        final UUID projectId = UUID.fromString("298a547f-ecb6-4ab2-8975-68f4e9bf7b39");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_BUDGETS_V2, projectId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.FORBIDDEN);
    }


    @Test
    void should_return_project_budgets_given_a_project_lead() {
        // Given
        final String jwt = userAuthHelper.authenticatePierre().jwt();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");

        final var usd = currencyRepository.findByCode("USD").orElseThrow();
        final var eth = currencyRepository.findByCode("ETH").orElseThrow();
        final var usdc = currencyRepository.findByCode("USDC").orElseThrow();
        historicalQuoteRepository.save(HistoricalQuoteEntity.builder()
                .price(BigDecimal.valueOf(1.10))
                .currencyId(eth.id())
                .baseId(usd.id())
                .timestamp(Instant.EPOCH)
                .build());
        historicalQuoteRepository.save(HistoricalQuoteEntity.builder()
                .price(BigDecimal.valueOf(1.27))
                .currencyId(usdc.id())
                .baseId(usd.id())
                .timestamp(Instant.EPOCH)
                .build());

        final var sponsorId = SponsorId.of(UUID.randomUUID());
        sponsorRepository.save(SponsorEntity.builder()
                .id(sponsorId.value())
                .name("Sponsor")
                .logoUrl("https://logo.com")
                .url("https://sponsor.com")
                .build());

        final var accountEth = accountingFacadePort.createSponsorAccount(sponsorId, Currency.Id.of(eth.id()), PositiveAmount.of(1000L), null);
        final var accountUsdc = accountingFacadePort.createSponsorAccount(sponsorId, Currency.Id.of(usdc.id()), PositiveAmount.of(20000L), null);

        accountingFacadePort.transfer(accountEth.account().id(), ProjectId.of(projectId), PositiveAmount.of(100L), Currency.Id.of(eth.id()));
        accountingFacadePort.transfer(accountUsdc.account().id(), ProjectId.of(projectId), PositiveAmount.of(200L), Currency.Id.of(usdc.id()));

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_BUDGETS_V2, projectId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "initialDollarsEquivalent": 364.00,
                          "remainingDollarsEquivalent": 364.00,
                          "budgets": [
                            {
                              "currency": "ETH",
                              "initialAmount": 100,
                              "remaining": 100,
                              "remainingDollarsEquivalent": 110.0,
                              "initialDollarsEquivalent": 110.0,
                              "dollarsConversionRate": 1.1
                            },
                            {
                              "currency": "USDC",
                              "initialAmount": 200,
                              "remaining": 200,
                              "remainingDollarsEquivalent": 254.00,
                              "initialDollarsEquivalent": 254.00,
                              "dollarsConversionRate": 1.27
                            }
                          ]
                        }
                                                
                        """);
    }
}
