package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.BudgetEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.CryptoUsdQuotesEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectToBudgetEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectToBudgetIdRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.BudgetRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.CryptoUsdQuotesRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;


public class ProjectBudgetsApiIT extends AbstractMarketplaceApiIT {


    @Autowired
    ProjectToBudgetIdRepository projectToBudgetIdRepository;
    @Autowired
    BudgetRepository budgetRepository;
    @Autowired
    CryptoUsdQuotesRepository cryptoUsdQuotesRepository;

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
        cryptoUsdQuotesRepository.save(CryptoUsdQuotesEntity.builder()
                .updatedAt(new Date())
                .price(BigDecimal.valueOf(1500))
                .currency(CurrencyEnumEntity.eth)
                .build());
        cryptoUsdQuotesRepository.save(CryptoUsdQuotesEntity.builder()
                .updatedAt(new Date())
                .price(BigDecimal.valueOf(120))
                .currency(CurrencyEnumEntity.apt)
                .build());
        cryptoUsdQuotesRepository.save(CryptoUsdQuotesEntity.builder()
                .updatedAt(new Date())
                .price(BigDecimal.valueOf(1.11))
                .currency(CurrencyEnumEntity.lords)
                .build());
        final BudgetEntity budget1 = budgetRepository.save(BudgetEntity.builder()
                .id(UUID.randomUUID())
                .initialAmount(BigDecimal.valueOf(3000))
                .remainingAmount(BigDecimal.valueOf(100))
                .currency(CurrencyEnumEntity.strk)
                .build());
        final BudgetEntity budget2 = budgetRepository.save(BudgetEntity.builder()
                .id(UUID.randomUUID())
                .initialAmount(BigDecimal.valueOf(500))
                .remainingAmount(BigDecimal.valueOf(0))
                .currency(CurrencyEnumEntity.apt)
                .build());
        final BudgetEntity budget3 = budgetRepository.save(BudgetEntity.builder()
                .id(UUID.randomUUID())
                .initialAmount(BigDecimal.valueOf(412))
                .remainingAmount(BigDecimal.valueOf(212))
                .currency(CurrencyEnumEntity.lords)
                .build());
        projectToBudgetIdRepository.save(ProjectToBudgetEntity.builder()
                .id(ProjectToBudgetEntity.ProjectToBudgetIdEntity.builder()
                        .budgetId(budget1.getId())
                        .projectId(projectId)
                        .build())
                .build());
        projectToBudgetIdRepository.save(ProjectToBudgetEntity.builder()
                .id(ProjectToBudgetEntity.ProjectToBudgetIdEntity.builder()
                        .budgetId(budget2.getId())
                        .projectId(projectId)
                        .build())
                .build());
        projectToBudgetIdRepository.save(ProjectToBudgetEntity.builder()
                .id(ProjectToBudgetEntity.ProjectToBudgetIdEntity.builder()
                        .budgetId(budget3.getId())
                        .projectId(projectId)
                        .build())
                .build());

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
                            "initialDollarsEquivalent": 370557.32,
                            "remainingDollarsEquivalent": 304275.32,
                            "budgets": [
                                {
                                    "currency": "USDC",
                                    "initialAmount": 10000,
                                    "remaining": 4000,
                                    "remainingDollarsEquivalent": 4040.0,
                                    "initialDollarsEquivalent": 10100.0,
                                    "dollarsConversionRate": 1.01
                                },
                                {
                                    "currency": "STRK",
                                    "initialAmount": 3000,
                                    "remaining": 100,
                                    "remainingDollarsEquivalent": null,
                                    "initialDollarsEquivalent": null,
                                    "dollarsConversionRate": null
                                },
                                {
                                    "currency": "APT",
                                    "initialAmount": 500,
                                    "remaining": 0,
                                    "remainingDollarsEquivalent": 0,
                                    "initialDollarsEquivalent": 60000,
                                    "dollarsConversionRate": 120
                                },
                                {
                                    "currency": "ETH",
                                    "initialAmount": 200,
                                    "remaining": 200,
                                    "remainingDollarsEquivalent": 300000,
                                    "initialDollarsEquivalent": 300000,
                                    "dollarsConversionRate": 1500
                                },
                                {
                                    "currency": "LORDS",
                                    "initialAmount": 412,
                                    "remaining": 212,
                                    "remainingDollarsEquivalent": 235.32,
                                    "initialDollarsEquivalent": 457.32,
                                    "dollarsConversionRate": 1.11
                                }
                            ]
                        }""");
    }
}
