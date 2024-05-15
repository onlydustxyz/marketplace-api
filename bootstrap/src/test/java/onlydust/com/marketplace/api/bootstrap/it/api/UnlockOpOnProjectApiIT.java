package onlydust.com.marketplace.api.bootstrap.it.api;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import onlydust.com.marketplace.project.domain.service.UserService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UnlockOpOnProjectApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    EntityManagerFactory entityManagerFactory;
    @Autowired
    UserService userService;
    private final UUID bretzelWithOpBudget = UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56");

    void setUp() {
        final EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("insert into unlock_op_on_projects (project_id) values ('%s')".formatted(bretzelWithOpBudget))
                .executeUpdate();
        em.flush();
        em.getTransaction().commit();
        em.close();
    }

//    @Test
//    @Order(1)
//    void should_unlock_op_on_project() {
//        // Given
//        setUp();
//        final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.authenticateGregoire();
//        final UUID billingProfileId = userService.getCompanyBillingProfile(authenticatedUser.user().getId()).getId();
//        final CompanyBillingProfileEntity companyBillingProfileEntity = companyBillingProfileRepository.findById(billingProfileId).orElseThrow();
//        companyBillingProfileRepository.save(companyBillingProfileEntity.toBuilder()
//                .verificationStatus(OldVerificationStatus.VERIFIED)
//                .build());
//        userService.updateBillingProfileType(authenticatedUser.user().getId(), OldBillingProfileType.COMPANY);
//        walletRepository.save(OldWalletEntity.builder()
//                .userId(authenticatedUser.user().getId())
//                .type(WalletTypeEnumEntity.address)
//                .network(NetworkEnumEntity.optimism)
//                .address("0x807fbf4e9ff6f6ad24145ade1fff905d955afefc")
//                .build());
//
//        final UUID opRewardId = paymentRequestRepository.save(new PaymentRequestEntity(
//                UUID.randomUUID(), authenticatedUser.user().getId(), authenticatedUser.user().getGithubUserId(),
//                new Date(), BigDecimal.valueOf(11.22), null, 0, bretzelWithOpBudget, CurrencyEnumEntity.op, null
//        )).getId();
//
//        // When
//        client.get()
//                .uri(getApiURI(PROJECTS_REWARDS.formatted(bretzelWithOpBudget), Map.of("pageIndex", "0", "pageSize", "20")))
//                .header("Authorization", "Bearer " + authenticatedUser.jwt())
//                // Then
//                .exchange()
//                .expectStatus()
//                .is2xxSuccessful()
//                .expectBody()
//                .jsonPath("$.rewards[?(@.amount.currency == 'OP')].status").isEqualTo("PROCESSING");
//
//        // When
//        client.get()
//                .uri(getApiURI(PROJECTS_REWARD.formatted(bretzelWithOpBudget, opRewardId)))
//                .header("Authorization", "Bearer " + authenticatedUser.jwt())
//                // Then
//                .exchange()
//                .expectStatus()
//                .is2xxSuccessful()
//                .expectBody()
//                .jsonPath("$.status").isEqualTo("PROCESSING");
//
//
//        // When
//        client.get()
//                .uri(getApiURI(ME_GET_REWARDS, Map.of("pageIndex", "0", "pageSize", "20", "currencies", "OP")))
//                .header("Authorization", "Bearer " + authenticatedUser.jwt())
//                // Then
//                .exchange()
//                .expectStatus()
//                .is2xxSuccessful()
//                .expectBody()
//                .consumeWith(System.out::println)
//                .jsonPath("$.rewards[?(@.amount.currency == 'OP')].status").isEqualTo("PENDING_INVOICE");
//
//        // When
//        client.get()
//                .uri(getApiURI(ME_REWARD.formatted(opRewardId)))
//                .header("Authorization", "Bearer " + authenticatedUser.jwt())
//                // Then
//                .exchange()
//                .expectStatus()
//                .is2xxSuccessful()
//                .expectBody()
//                .jsonPath("$.status").isEqualTo("PENDING_INVOICE");
//
//    }
}
