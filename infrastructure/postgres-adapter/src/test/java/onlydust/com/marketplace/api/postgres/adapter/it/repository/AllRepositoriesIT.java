package onlydust.com.marketplace.api.postgres.adapter.it.repository;

import com.vladmihalcea.hibernate.type.json.internal.JacksonUtil;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NetworkEnumEntity;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.api.postgres.adapter.PostgresUserAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.*;
import onlydust.com.marketplace.api.postgres.adapter.it.AbstractPostgresIT;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.*;
import onlydust.com.marketplace.kernel.model.blockchain.Aptos;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.model.blockchain.Optimism;
import onlydust.com.marketplace.kernel.model.blockchain.StarkNet;
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
    OldWalletRepository oldWalletRepository;
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
    @Autowired
    RewardRepository rewardRepository;

    @Test
    void should_create_user() {
        final UserEntity expected = UserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().randomNumber())
                .githubLogin(faker.name().name())
                .githubAvatarUrl(faker.internet().avatar())
                .githubEmail(faker.internet().emailAddress())
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
                .githubUserId(faker.number().randomNumber(15, true))
                .githubLogin(faker.name().name())
                .githubAvatarUrl(faker.internet().avatar())
                .githubEmail(faker.internet().emailAddress())
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
                .currency(CurrencyEnumEntity.strk)
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
        oldWalletRepository.deleteAll();
        final OldWalletEntity expected = OldWalletEntity.builder()
                .network(NetworkEnumEntity.ethereum)
                .userId(UUID.randomUUID())
                .address(faker.address().fullAddress())
                .type(WalletTypeEnumEntity.address)
                .build();

        assertIsSaved(expected, oldWalletRepository);
    }

    @Test
    void should_create_bank_account() {
        // Given
        bankAccountRepository.deleteAll();
        final OldBankAccountEntity expected = OldBankAccountEntity.builder()
                .bic(faker.pokemon().location())
                .userId(UUID.randomUUID())
                .iban("FR1014508000702139488771C56")
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

    @Test
    void should_create_reward() {
        // Given
        final UUID projectId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        projectIdRepository.save(ProjectIdEntity.builder().id(projectId).build());
        userRepository.save(UserEntity.builder()
                .id(userId)
                .githubUserId(faker.number().randomNumber())
                .createdAt(new Date())
                .lastSeenAt(new Date())
                .githubLogin(faker.rickAndMorty().character())
                .githubAvatarUrl(faker.internet().url())
                .githubEmail(faker.internet().emailAddress())
                .roles(new UserRole[]{UserRole.USER})
                .build());

        final RewardEntity expected = RewardEntity.of(new Reward(
                UUID.randomUUID(),
                projectId,
                userId,
                faker.number().randomNumber(),
                BigDecimal.valueOf(faker.number().randomNumber()),
                Currency.USDC,
                new Date(),
                List.of(Reward.Item.builder()
                        .id(faker.pokemon().name())
                        .number(faker.number().randomNumber())
                        .repoId(faker.number().randomNumber())
                        .type(Reward.Item.Type.PULL_REQUEST)
                        .build()
                )
        ));

        assertIsSaved(expected, rewardRepository);
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
        userRepository.save(UserEntity.builder()
                .id(userId)
                .githubUserId(1L)
                .createdAt(new Date())
                .lastSeenAt(new Date())
                .githubLogin(faker.rickAndMorty().character())
                .githubAvatarUrl(faker.internet().url())
                .githubEmail(faker.internet().emailAddress())
                .roles(new UserRole[]{UserRole.USER})
                .build());
        final UserPayoutSettings userPayoutSettings = UserPayoutSettings.builder()
                .aptosAddress(Aptos.accountAddress("0x01"))
                .starknetAddress(StarkNet.accountAddress("0x02"))
                .ethWallet(Ethereum.wallet("0x03"))
                .optimismAddress(Optimism.accountAddress("0x04"))
                .sepaAccount(UserPayoutSettings.SepaAccount.builder()
                        .bic(faker.hacker().abbreviation())
                        .accountNumber(OldAccountNumber.of("FR1014508000702139488771C56"))
                        .build())
                .build();

        // When
        postgresUserAdapter.savePayoutSettingsForUserId(userId, userPayoutSettings);
        final UserPayoutSettings payoutInformationById = postgresUserAdapter.getPayoutSettingsById(userId);

        // Then
        assertEquals(userPayoutSettings, payoutInformationById);

        final UserPayoutSettings userPayoutSettingsUpdated =
                postgresUserAdapter.savePayoutSettingsForUserId(userId, userPayoutSettings.toBuilder()
                        .ethWallet(null)
                        .aptosAddress(null)
                        .sepaAccount(null)
                        .build());
        assertNull(userPayoutSettingsUpdated.getEthWallet());
        assertNull(userPayoutSettingsUpdated.getAptosAddress());
        assertNull(userPayoutSettingsUpdated.getSepaAccount());
    }
}
