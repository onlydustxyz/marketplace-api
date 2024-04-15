package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.service.CachedAccountBookProvider;
import onlydust.com.marketplace.api.bootstrap.helper.AccountingHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.SponsorEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.AccountBookEventRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.AccountBookRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CurrencyRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.SponsorAccountRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.SponsorRepository;
import onlydust.com.marketplace.project.domain.service.RewardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;


public class ProjectBudgetsApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    AccountingHelper accountingHelper;
    @Autowired
    CurrencyRepository currencyRepository;
    @Autowired
    AccountingFacadePort accountingFacadePort;
    @Autowired
    RewardService rewardService;
    @Autowired
    SponsorRepository sponsorRepository;
    @Autowired
    SponsorAccountRepository sponsorAccountRepository;
    @Autowired
    AccountBookRepository accountBookRepository;
    @Autowired
    private AccountBookEventRepository accountBookEventRepository;
    @Autowired
    private CachedAccountBookProvider accountBookProvider;

    @BeforeEach
    void setup() {
        accountBookEventRepository.deleteAll();
        sponsorAccountRepository.deleteAll();
        accountBookRepository.deleteAll();
        accountBookProvider.evictAll();
    }

    @Test
    void should_return_forbidden_status_when_getting_project_budgets_given_user_not_project_lead() {
        // Given
        final String jwt = userAuthHelper.authenticatePierre().jwt();
        final UUID projectId = UUID.fromString("298a547f-ecb6-4ab2-8975-68f4e9bf7b39");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_BUDGETS, projectId)))
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
        accountingHelper.clearAllQuotes();

        accountingHelper.saveQuote(new Quote(Currency.Id.of(eth.id()), Currency.Id.of(usd.id()), BigDecimal.valueOf(1.10), Instant.EPOCH));
        accountingHelper.saveQuote(new Quote(Currency.Id.of(usdc.id()), Currency.Id.of(usd.id()), BigDecimal.valueOf(1.27), Instant.EPOCH));

        final var sponsorId = SponsorId.of(UUID.randomUUID());
        sponsorRepository.save(SponsorEntity.builder()
                .id(sponsorId.value())
                .name("Sponsor")
                .logoUrl("https://logo.com")
                .url("https://sponsor.com")
                .build());

        final var accountEth = accountingFacadePort.createSponsorAccountWithInitialAllowance(sponsorId, Currency.Id.of(eth.id()), null,
                PositiveAmount.of(1000L));
        final var accountUsdc = accountingFacadePort.createSponsorAccountWithInitialAllowance(sponsorId, Currency.Id.of(usdc.id()), null,
                PositiveAmount.of(20000L));

        accountingFacadePort.allocate(accountEth.account().id(), ProjectId.of(projectId), PositiveAmount.of(100L), Currency.Id.of(eth.id()));
        accountingFacadePort.allocate(accountUsdc.account().id(), ProjectId.of(projectId), PositiveAmount.of(200L), Currency.Id.of(usdc.id()));

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_BUDGETS, projectId)))
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
                              "currency": {"id":"71bdfcf4-74ee-486b-8cfe-5d841dd93d5c","code":"ETH","name":"Ether","logoUrl":null,"decimals":18},
                              "initialAmount": 100,
                              "remaining": 100,
                              "remainingDollarsEquivalent": 110.0,
                              "initialDollarsEquivalent": 110.0,
                              "dollarsConversionRate": 1.1
                            },
                            {
                              "currency": {"id":"562bbf65-8a71-4d30-ad63-520c0d68ba27","code":"USDC","name":"USD Coin","logoUrl":"https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png","decimals":6},
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
