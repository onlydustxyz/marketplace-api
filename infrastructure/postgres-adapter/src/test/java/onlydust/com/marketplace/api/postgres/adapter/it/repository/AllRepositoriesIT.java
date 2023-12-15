package onlydust.com.marketplace.api.postgres.adapter.it.repository;

import com.vladmihalcea.hibernate.type.json.internal.JacksonUtil;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation;
import onlydust.com.marketplace.api.domain.model.UserRole;
import onlydust.com.marketplace.api.postgres.adapter.PostgresUserAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.*;
import onlydust.com.marketplace.api.postgres.adapter.it.AbstractPostgresIT;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Transactional
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
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserViewRepository userViewRepository;
    @Autowired
    PostgresUserAdapter postgresUserAdapter;

    @Test
    void should_create_user() {
        final UserEntity expected = UserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().randomNumber())
                .githubLogin(faker.name().name())
                .githubAvatarUrl(faker.internet().avatar())
                .email(faker.internet().emailAddress())
                .roles(new UserRole[]{UserRole.USER, UserRole.ADMIN})
                .lastSeenAt(new Date())
                .build();

        userRepository.deleteAll();
        assertIsSaved(expected, userRepository);
    }

    @Test
    void should_read_user_onboarding() {
        // Given
        final UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().randomNumber())
                .githubLogin(faker.name().name())
                .githubAvatarUrl(faker.internet().avatar())
                .email(faker.internet().emailAddress())
                .roles(new UserRole[]{UserRole.USER, UserRole.ADMIN})
                .lastSeenAt(new Date())
                .build();
        final OnboardingEntity onboarding = OnboardingEntity.builder().id(user.getId())
                .termsAndConditionsAcceptanceDate(new Date())
                .profileWizardDisplayDate(new Date())
                .build();

        userRepository.deleteAll();

        // When
        assertIsSaved(user, userRepository);
        assertIsSaved(onboarding, onboardingRepository);
        final UserViewEntity result = userViewRepository.findById(user.getId()).orElseThrow();

        // Then
        assertEquals(1, userRepository.findAll().size());
        assertEquals(onboarding, result.getOnboarding());
        assertThat(result.getGithubUserId()).isEqualTo(user.getGithubUserId());
    }

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
                .ignorePullRequests(true)
                .ignoreCodeReviews(true)
                .ignoreIssues(true)
                .ignoreContributionsBefore(new Date())
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
        authUserRepository.deleteAll();
        final AuthUserEntity expected = AuthUserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().randomNumber())
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
                .receipt(JacksonUtil.toJsonNode("""
                        {
                        "test": true
                        }"""))
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
                .recipientId(faker.number().randomNumber())
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
                .contributionType(ContributionTypeEnumEntity.PULL_REQUEST)
                .projectId(UUID.randomUUID())
                .recipientId(faker.number().randomNumber())
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
                .languages(Map.of(faker.rickAndMorty().location(), 5L, faker.hacker().adjective(), 10L))
                .contactInformations(List.of(ContactInformationEntity.builder()
                        .contact(faker.rickAndMorty().location())
                        .id(ContactInformationIdEntity.builder()
                                .channel(ContactChanelEnumEntity.email)
                                .userId(UUID.randomUUID())
                                .build())
                        .isPublic(false)
                        .build()))
                .isLookingForAJob(true)
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
        walletRepository.deleteAll();
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
        bankAccountRepository.deleteAll();
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


    @Test
    @Transactional(propagation = Propagation.NEVER)
    void should_save_and_read_and_update_user_payout_info() {
        // Given
        final UUID userId = UUID.randomUUID();
        authUserRepository.save(AuthUserEntity.builder()
                .id(userId)
                .githubUserId(1L)
                .createdAt(new Date())
                .loginAtSignup(faker.rickAndMorty().character())
                .avatarUrlAtSignup(faker.internet().url())
                .email(faker.internet().emailAddress())
                .isAdmin(false)
                .build());
        final UserPayoutInformation.Person person = UserPayoutInformation.Person.builder()
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .build();
        final UserPayoutInformation userPayoutInformation = UserPayoutInformation.builder()
                .isACompany(true)
                .company(UserPayoutInformation.Company.builder()
                        .identificationNumber(faker.number().digit())
                        .name(faker.name().name())
                        .owner(person)
                        .build())
                .location(UserPayoutInformation.Location.builder()
                        .country(faker.address().country())
                        .city(faker.address().city())
                        .postalCode(faker.address().countryCode())
                        .address(faker.address().fullAddress())
                        .build())
                .payoutSettings(UserPayoutInformation.PayoutSettings.builder()
                        .aptosAddress(faker.rickAndMorty().character())
                        .starknetAddress(faker.pokemon().location())
                        .ethName(faker.pokemon().name())
                        .optimismAddress(faker.pokemon().name())
                        .sepaAccount(UserPayoutInformation.SepaAccount.builder()
                                .bic(faker.hacker().abbreviation())
                                .iban(faker.hacker().abbreviation())
                                .build())
                        .usdPreferredMethodEnum(UserPayoutInformation.UsdPreferredMethodEnum.CRYPTO)
                        .build())
                .build();

        // When
        postgresUserAdapter.savePayoutInformationForUserId(userId, userPayoutInformation);
        final UserPayoutInformation payoutInformationById = postgresUserAdapter.getPayoutInformationById(userId);

        // Then
        assertEquals(userPayoutInformation.toBuilder().hasValidPerson(false).hasValidCompany(true).hasValidLocation(true)
                        .payoutSettings(payoutInformationById.getPayoutSettings()
                                .toBuilder()
                                .hasMissingOptimismWallet(false)
                                .hasMissingEthWallet(false)
                                .hasMissingAptosWallet(false)
                                .hasMissingBankingAccount(false)
                                .hasMissingStarknetWallet(false)
                                .build()).build(),
                payoutInformationById);

        final UserPayoutInformation userPayoutInformationUpdated =
                postgresUserAdapter.savePayoutInformationForUserId(userId, userPayoutInformation.toBuilder()
                        .payoutSettings(userPayoutInformation.getPayoutSettings().toBuilder()
                                .ethAddress(null)
                                .aptosAddress(null)
                                .ethName(null)
                                .sepaAccount(null)
                                .build()).build());
        assertNull(userPayoutInformationUpdated.getPayoutSettings().getEthAddress());
        assertNull(userPayoutInformationUpdated.getPayoutSettings().getAptosAddress());
        assertNull(userPayoutInformationUpdated.getPayoutSettings().getEthName());
        assertNull(userPayoutInformationUpdated.getPayoutSettings().getSepaAccount());
    }
}
