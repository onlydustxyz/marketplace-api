package onlydust.com.marketplace.api.postgres.adapter.it.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.*;
import onlydust.com.marketplace.api.postgres.adapter.it.AbstractPostgresIT;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AllRepositoriesIT extends AbstractPostgresIT {

    @Autowired
    OnboardingRepository onboardingRepository;
    @Autowired
    AuthUserRepository authUserRepository;
    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    BudgetRepository budgetRepository;
    @Autowired
    PaymentRequestRepository paymentRequestRepository;
    @Autowired
    WorkItemRepository workItemRepository;
    @Autowired
    ApplicationRepository applicationRepository;
    @Autowired
    UserProfileInfoRepository userProfileInfoRepository;

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
    void should_create_auth_user() {
        // Given
        final AuthUserEntity expected = AuthUserEntity.builder()
                .id(UUID.randomUUID())
                .createdAt(new Date())
                .avatarUrlAtSignup(faker.pokemon().name())
                .email("test@gmail.com")
                .lastSeen(new Date())
                .isAdmin(Boolean.FALSE)
                .loginAtSignup(faker.pokemon().location())
                .build();

        assertIsSaved(expected, authUserRepository);
    }

    @Test
    void should_create_payment() {
        // Given
        final PaymentEntity expected = PaymentEntity.builder()
                .id(UUID.randomUUID())
                .amount(BigDecimal.valueOf(faker.number().randomNumber()))
                .currencyCode(faker.programmingLanguage().creator())
                .receipt(faker.programmingLanguage().name())
                .processedAt(new Date())
                .requestId(UUID.randomUUID())
                .build();

        assertIsSaved(expected, paymentRepository);
    }

    @Test
    void should_create_budget() {
        // Given
        final BudgetEntity expected = BudgetEntity.builder()
                .id(UUID.randomUUID())
                .currency(CurrencyEnumEntity.stark)
                .initialAmount(BigDecimal.ZERO)
                .remainingAmount(BigDecimal.ZERO)
                .build();

        assertIsSaved(expected, budgetRepository);
    }

    @Test
    void should_create_payment_request() {
        // Given
        final PaymentRequestEntity expected = PaymentRequestEntity.builder()
                .amount(BigDecimal.ZERO)
                .requestedAt(new Date())
                .currency(CurrencyEnumEntity.usd)
                .recipientId(faker.number().randomDigit())
                .id(UUID.randomUUID())
                .projectId(UUID.randomUUID())
                .requestorId(UUID.randomUUID())
                .hoursWorked(faker.number().randomDigit())
                .build();

        assertIsSaved(expected, paymentRequestRepository);
    }

    @Test
    void should_create_work_item() {
        // Given
        final WorkItemEntity expected = WorkItemEntity
                .builder()
                .workItemId(WorkItemIdEntity.builder()
                        .repoId(faker.number().randomDigit())
                        .number(faker.number().randomDigit())
                        .paymentId(UUID.randomUUID())
                        .build())
                .id(faker.pokemon().location())
                .contributionType(ContributionTypeEnumEntity.pull_request)
                .projectId(UUID.randomUUID())
                .recipientId(faker.number().randomDigit())
                .build();

        assertIsSaved(expected, workItemRepository);
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
                .allocatedTime(AllocatedTimeEntityEnum.ONE_TO_THREE_DAYS)
                .avatarUrl(faker.pokemon().name())
                .bio(faker.hacker().abbreviation())
                .cover(ProfileCoverEnumEntity.yellow)
                .isLookingForAJob(false)
                .website(faker.harryPotter().location())
                .location(faker.rickAndMorty().location())
                .languages(Map.of(faker.rickAndMorty().location(), 5, faker.hacker().adjective(), 10))
                .id(UUID.randomUUID())
                .build();

        assertIsSaved(expected, userProfileInfoRepository);
    }

    private <Entity, ID> void assertIsSaved(Entity expected, JpaRepository<Entity, ID> repository) {
        // When
        final Entity result = repository.save(expected);

        // Then
        assertEquals(1, repository.findAll().size());
        assertEquals(expected, result);
    }
}
