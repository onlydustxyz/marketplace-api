package onlydust.com.marketplace.api.postgres.adapter.it.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.*;
import onlydust.com.marketplace.api.postgres.adapter.it.AbstractPostgresIT;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
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
    @Autowired
    ContactInformationRepository contactInformationRepository;
    @Autowired
    WalletRepository walletRepository;
    @Autowired
    BankAccountRepository bankAccountRepository;
    @Autowired
    CryptoUsdQuotesRepository cryptoUsdQuotesRepository;
    @Autowired
    SponsorRepository sponsorRepository;
    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    ProjectIdRepository projectIdRepository;


    @Test
    void should_create_project() {
        // Given
        final List<SponsorEntity> sponsors = List.of(
                SponsorEntity.builder()
                        .id(UUID.randomUUID())
                        .name(faker.name().name())
                        .url(faker.rickAndMorty().location())
                        .logoUrl(faker.rickAndMorty().character())
                        .build(), SponsorEntity.builder()
                        .id(UUID.randomUUID())
                        .name(faker.name().name())
                        .url(faker.rickAndMorty().location())
                        .logoUrl(faker.rickAndMorty().character())
                        .build()
        );
        sponsorRepository.saveAll(sponsors);
        final UUID projectId = UUID.randomUUID();
        projectIdRepository.save(ProjectIdEntity.builder().id(projectId).build());
        final ProjectEntity expected = ProjectEntity.builder()
                .key(faker.address().fullAddress())
                .name(faker.name().name())
                .longDescription(faker.pokemon().location())
                .shortDescription(faker.pokemon().name())
                .logoUrl(faker.rickAndMorty().location())
                .hiring(false)
                .id(projectId)
                .rank(faker.number().randomDigit())
                .visibility(ProjectVisibilityEnumEntity.PUBLIC)
                .sponsors(sponsors)
                .build();

        assertIsSaved(expected, projectRepository);
    }

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
                .allocatedTime(AllocatedTimeEnumEntity.one_to_three_days)
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

    @Test
    void should_create_contact_information() {
        // Given
        final ContactInformationEntity expected = ContactInformationEntity.builder()
                .contact(faker.rickAndMorty().location())
                .id(ContactInformationIdEntity.builder()
                        .channel(ContactChanelEnumEntity.email)
                        .userId(UUID.randomUUID())
                        .build())
                .isPublic(false)
                .build();

        assertIsSaved(expected, contactInformationRepository);
    }

    @Test
    void should_create_wallet() {
        // Given
        final WalletEntity expected = WalletEntity.builder()
                .id(
                        WalletIdEntity.builder()
                                .network(NetworkEnumEntity.ethereum)
                                .userId(UUID.randomUUID())
                                .build()
                )
                .address(faker.address().fullAddress())
                .type(WalletTypeEnumEntity.address)
                .build();

        assertIsSaved(expected, walletRepository);
    }

    @Test
    void should_create_bank_account() {
        // Given
        final BankAccountEntity expected = BankAccountEntity.builder()
                .bic(faker.pokemon().location())
                .userId(UUID.randomUUID())
                .iban(faker.hacker().abbreviation())
                .build();

        assertIsSaved(expected, bankAccountRepository);
    }

    @Test
    void should_create_crypto_usd_quotes() {
        // Given
        final CryptoUsdQuotesEntity expected = CryptoUsdQuotesEntity.builder()
                .currency(CurrencyEnumEntity.usd)
                .price(BigDecimal.ZERO)
                .updatedAt(new Date())
                .build();

        assertIsSaved(expected, cryptoUsdQuotesRepository);
    }

    @Test
    void should_create_sponsor() {
        // Given
        final SponsorEntity expected = SponsorEntity.builder()
                .logoUrl(faker.rickAndMorty().location())
                .name(faker.hacker().abbreviation())
                .id(UUID.randomUUID())
                .build();

        assertIsSaved(expected, sponsorRepository);
    }

    private <Entity, ID> void assertIsSaved(Entity expected, JpaRepository<Entity, ID> repository) {
        // When
        final Entity result = repository.save(expected);

        // Then
        assertEquals(1, repository.findAll().size());
        assertEquals(expected, result);
    }
}
