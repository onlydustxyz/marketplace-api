package onlydust.com.marketplace.api.postgres.adapter.it.repository;

import com.vladmihalcea.hibernate.type.json.internal.JacksonUtil;
import onlydust.com.marketplace.api.domain.model.UserPayoutSettings;
import onlydust.com.marketplace.api.domain.model.UserRole;
import onlydust.com.marketplace.api.domain.model.bank.AccountNumber;
import onlydust.com.marketplace.api.domain.view.UserRewardView;
import onlydust.com.marketplace.api.postgres.adapter.PostgresUserAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserRewardViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.CryptoUsdQuotesEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.PaymentEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.PaymentRequestEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProjectVisibilityEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.it.AbstractPostgresIT;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.CryptoUsdQuotesRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.PaymentRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.PaymentRequestRepository;
import onlydust.com.marketplace.kernel.model.blockchain.Aptos;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.model.blockchain.Optimism;
import onlydust.com.marketplace.kernel.model.blockchain.StarkNet;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CustomUserRewardRepositoryIT extends AbstractPostgresIT {
    private static UUID userId = UUID.randomUUID();
    private static Long githubUserId = faker.random().nextLong();
    private static UUID projectId = UUID.randomUUID();
    @Autowired
    UserRepository userRepository;
    @Autowired
    PaymentRequestRepository paymentRequestRepository;
    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    CustomUserRewardRepository customUserRewardRepository;
    @Autowired
    CryptoUsdQuotesRepository cryptoUsdQuotesRepository;
    @Autowired
    PostgresUserAdapter postgresUserAdapter;
    @Autowired
    CustomUserPayoutInfoRepository customUserPayoutInfoRepository;
    @Autowired
    UserBillingProfileTypeRepository userBillingProfileTypeRepository;
    @Autowired
    IndividualBillingProfileRepository individualBillingProfileRepository;
    @Autowired
    CompanyBillingProfileRepository companyBillingProfileRepository;

    @Test
    @Order(1)
    void should_return_user_rewards_given_a_user_without_payout_info_and_with_rewards() {
        // Given
        userRepository.save(
                UserEntity.builder()
                        .id(userId)
                        .githubUserId(githubUserId)
                        .githubLogin(faker.name().username())
                        .githubAvatarUrl(faker.internet().avatar())
                        .githubEmail(faker.internet().emailAddress())
                        .roles(new UserRole[]{UserRole.USER})
                        .lastSeenAt(new Date())
                        .build()
        );
        projectRepository.save(
                ProjectEntity.builder()
                        .id(projectId)
                        .name(faker.pokemon().name())
                        .shortDescription(faker.pokemon().location())
                        .longDescription(faker.harryPotter().location())
                        .telegramLink(faker.internet().url())
                        .logoUrl(faker.internet().avatar())
                        .hiring(false)
                        .rank(0)
                        .visibility(ProjectVisibilityEnumEntity.PUBLIC)
                        .ignorePullRequests(false)
                        .ignoreIssues(false)
                        .ignoreCodeReviews(false)
                        .build());
        cryptoUsdQuotesRepository.saveAll(List.of(
                new CryptoUsdQuotesEntity(CurrencyEnumEntity.eth, BigDecimal.valueOf(1000), new Date()),
                new CryptoUsdQuotesEntity(CurrencyEnumEntity.usdc, BigDecimal.valueOf(1.01), new Date()),
                new CryptoUsdQuotesEntity(CurrencyEnumEntity.lords, BigDecimal.valueOf(0.33), new Date()),
                new CryptoUsdQuotesEntity(CurrencyEnumEntity.apt, BigDecimal.valueOf(0.55), new Date()),
                new CryptoUsdQuotesEntity(CurrencyEnumEntity.op, BigDecimal.valueOf(10), new Date())));

        final UUID rewardPaid = UUID.randomUUID();
        paymentRequestRepository.saveAll(List.of(
                new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(), githubUserId, new Date(),
                        BigDecimal.valueOf(10000), null, 1, projectId, CurrencyEnumEntity.usd),
                new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(), githubUserId, new Date(),
                        BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.eth),
                new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(), githubUserId, new Date(),
                        BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.lords),
                new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(), githubUserId, new Date(),
                        BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.usdc),
                new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(), githubUserId, new Date(),
                        BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.apt),
                new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(), githubUserId, new Date(),
                        BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.op),
                new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(), githubUserId, new Date(),
                        BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.strk),
                new PaymentRequestEntity(rewardPaid, UUID.randomUUID(), githubUserId, new Date(), BigDecimal.ONE,
                        null, 1, projectId, CurrencyEnumEntity.strk)));
        paymentRepository.save(new PaymentEntity(UUID.randomUUID(), BigDecimal.ONE, "STRK",
                JacksonUtil.toJsonNode("{}"), rewardPaid, new Date()));

        // When
        List<UserRewardViewEntity> viewEntities = customUserRewardRepository.getViewEntities(userId,
                List.of(), List.of(), null, null,
                UserRewardView.SortBy.amount, SortDirection.desc, 0, 100);

        // Then
        assertEquals(8, viewEntities.size());
        assertThat(viewEntities.stream().map(UserRewardViewEntity::getCurrency).toList()).containsExactly(
                CurrencyEnumEntity.usd,
                CurrencyEnumEntity.eth,
                CurrencyEnumEntity.op,
                CurrencyEnumEntity.usdc,
                CurrencyEnumEntity.apt,
                CurrencyEnumEntity.lords,
                CurrencyEnumEntity.strk,
                CurrencyEnumEntity.strk
        );
        assertThat(viewEntities.stream().map(UserRewardViewEntity::getStatus).toList()).containsExactly(
                "PENDING_VERIFICATION",
                "PENDING_VERIFICATION",
                "PENDING_VERIFICATION",
                "PENDING_VERIFICATION",
                "PENDING_VERIFICATION",
                "PENDING_VERIFICATION",
                "PENDING_VERIFICATION",
                "COMPLETE"
        );

        userBillingProfileTypeRepository.save(UserBillingProfileTypeEntity.builder()
                .userId(userId)
                .billingProfileType(UserBillingProfileTypeEntity.BillingProfileTypeEntity.INDIVIDUAL)
                .build());

        final UUID billingProfileId = UUID.randomUUID();
        individualBillingProfileRepository.save(IndividualBillingProfileEntity.builder()
                .id(billingProfileId)
                .userId(userId)
                .verificationStatus(VerificationStatusEntity.UNDER_REVIEW)
                .build());

        viewEntities = customUserRewardRepository.getViewEntities(userId,
                List.of(), List.of(), null, null,
                UserRewardView.SortBy.amount, SortDirection.desc, 0, 100);

        assertThat(viewEntities.stream().map(UserRewardViewEntity::getStatus).toList()).containsExactly(
                "PENDING_VERIFICATION",
                "PENDING_VERIFICATION",
                "PENDING_VERIFICATION",
                "PENDING_VERIFICATION",
                "PENDING_VERIFICATION",
                "PENDING_VERIFICATION",
                "PENDING_VERIFICATION",
                "COMPLETE"
        );

        individualBillingProfileRepository.save(IndividualBillingProfileEntity.builder()
                .id(billingProfileId)
                .userId(userId)
                .verificationStatus(VerificationStatusEntity.VERIFIED)
                .build());

        viewEntities = customUserRewardRepository.getViewEntities(userId,
                List.of(), List.of(), null, null,
                UserRewardView.SortBy.amount, SortDirection.desc, 0, 100);

        assertThat(viewEntities.stream().map(UserRewardViewEntity::getStatus).toList()).containsExactly(
                "MISSING_PAYOUT_INFO",
                "MISSING_PAYOUT_INFO",
                "MISSING_PAYOUT_INFO",
                "MISSING_PAYOUT_INFO",
                "MISSING_PAYOUT_INFO",
                "MISSING_PAYOUT_INFO",
                "MISSING_PAYOUT_INFO",
                "COMPLETE"
        );


    }


    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GivenAValidIndividual {

        private static UUID individualUserId = UUID.randomUUID();
        private static Long individualIgithubUserId = faker.random().nextLong();

        @Test
        @Order(1)
        void should_return_user_rewards_given_a_user_with_only_stark_wallet_and_valid_contact_info() {
            // Given
            userRepository.save(
                    UserEntity.builder()
                            .id(individualUserId)
                            .githubUserId(individualIgithubUserId)
                            .githubLogin(faker.name().username())
                            .githubAvatarUrl(faker.internet().avatar())
                            .githubEmail(faker.internet().emailAddress())
                            .roles(new UserRole[]{UserRole.USER})
                            .lastSeenAt(new Date())
                            .build()
            );
            userBillingProfileTypeRepository.save(UserBillingProfileTypeEntity.builder()
                    .billingProfileType(UserBillingProfileTypeEntity.BillingProfileTypeEntity.INDIVIDUAL)
                    .userId(individualUserId)
                    .build());
            individualBillingProfileRepository.save(IndividualBillingProfileEntity.builder()
                    .verificationStatus(VerificationStatusEntity.VERIFIED)
                    .userId(individualUserId)
                    .id(UUID.randomUUID())
                    .build());
            postgresUserAdapter.savePayoutSettingsForUserId(individualUserId,
                    UserPayoutSettings.builder().starknetAddress(StarkNet.accountAddress("0x01")).build());
            paymentRequestRepository.saveAll(List.of(new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(),
                            individualIgithubUserId, new Date(), BigDecimal.valueOf(10000), null, 1, projectId,
                            CurrencyEnumEntity.usd),
                    new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(), individualIgithubUserId, new Date(),
                            BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.eth),
                    new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(), individualIgithubUserId, new Date(),
                            BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.apt),
                    new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(), individualIgithubUserId, new Date(),
                            BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.op),
                    new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(), individualIgithubUserId, new Date(),
                            BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.strk),
                    new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(), individualIgithubUserId, new Date(),
                            BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.lords),
                    new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(), individualIgithubUserId, new Date(),
                            BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.usdc)
            ));


            // When
            final List<UserRewardViewEntity> viewEntities =
                    customUserRewardRepository.getViewEntities(individualUserId,
                            List.of(), List.of(), null, null,
                            UserRewardView.SortBy.amount,
                            SortDirection.desc, 0, 100);


            // Then
            assertEquals(7, viewEntities.size());
            assertThat(viewEntities.stream().map(UserRewardViewEntity::getCurrency).toList()).containsExactly(
                    CurrencyEnumEntity.usd,
                    CurrencyEnumEntity.eth,
                    CurrencyEnumEntity.op,
                    CurrencyEnumEntity.usdc,
                    CurrencyEnumEntity.apt,
                    CurrencyEnumEntity.lords,
                    CurrencyEnumEntity.strk
            );
            assertThat(viewEntities.stream().map(UserRewardViewEntity::getStatus).toList()).containsExactly(
                    "MISSING_PAYOUT_INFO",
                    "MISSING_PAYOUT_INFO",
                    "MISSING_PAYOUT_INFO",
                    "MISSING_PAYOUT_INFO",
                    "MISSING_PAYOUT_INFO",
                    "MISSING_PAYOUT_INFO",
                    "PROCESSING"
            );
        }

        @Test
        @Order(2)
        void should_return_user_rewards_given_a_user_with_only_op_wallet_and_valid_contact_info() {
            // Given
            postgresUserAdapter.savePayoutSettingsForUserId(individualUserId,
                    UserPayoutSettings.builder()
                            .optimismAddress(Optimism.accountAddress("0x01")).build());
            List<UserRewardViewEntity> viewEntities = customUserRewardRepository.getViewEntities(individualUserId,
                    List.of(), List.of(), null, null,
                    UserRewardView.SortBy.amount,
                    SortDirection.desc, 0, 100);
            paymentRepository.save(new PaymentEntity(UUID.randomUUID(), BigDecimal.ONE, "USD",
                    JacksonUtil.toJsonNode("{}"), viewEntities.get(1).getId(), new Date()));

            // When
            viewEntities =
                    customUserRewardRepository.getViewEntities(individualUserId,
                            List.of(), List.of(), null, null,
                            UserRewardView.SortBy.amount,
                            SortDirection.desc, 0, 100);

            // Then
            assertEquals(7, viewEntities.size());
            assertThat(viewEntities.stream().map(UserRewardViewEntity::getCurrency).toList()).containsExactly(
                    CurrencyEnumEntity.usd,
                    CurrencyEnumEntity.eth,
                    CurrencyEnumEntity.op,
                    CurrencyEnumEntity.usdc,
                    CurrencyEnumEntity.apt,
                    CurrencyEnumEntity.lords,
                    CurrencyEnumEntity.strk
            );
            assertThat(viewEntities.stream().map(UserRewardViewEntity::getStatus).toList()).containsExactly(
                    "MISSING_PAYOUT_INFO",
                    "COMPLETE",
                    "LOCKED",
                    "MISSING_PAYOUT_INFO",
                    "MISSING_PAYOUT_INFO",
                    "MISSING_PAYOUT_INFO",
                    "MISSING_PAYOUT_INFO"
            );
        }

        @Test
        @Order(3)
        void should_return_user_rewards_given_a_user_with_only_apt_wallet_and_valid_contact_info() {
            // Given
            postgresUserAdapter.savePayoutSettingsForUserId(individualUserId,
                    UserPayoutSettings.builder()
                            .aptosAddress(Aptos.accountAddress("0x01")).build());

            // When
            final List<UserRewardViewEntity> viewEntities =
                    customUserRewardRepository.getViewEntities(individualUserId,
                            List.of(), List.of(), null, null,
                            UserRewardView.SortBy.amount,
                            SortDirection.desc, 0, 100);

            // Then
            assertEquals(7, viewEntities.size());
            assertThat(viewEntities.stream().map(UserRewardViewEntity::getCurrency).toList()).containsExactly(
                    CurrencyEnumEntity.usd,
                    CurrencyEnumEntity.eth,
                    CurrencyEnumEntity.op,
                    CurrencyEnumEntity.usdc,
                    CurrencyEnumEntity.apt,
                    CurrencyEnumEntity.lords,
                    CurrencyEnumEntity.strk
            );
            assertThat(viewEntities.stream().map(UserRewardViewEntity::getStatus).toList()).containsExactly(
                    "MISSING_PAYOUT_INFO",
                    "COMPLETE",
                    "MISSING_PAYOUT_INFO",
                    "MISSING_PAYOUT_INFO",
                    "PROCESSING",
                    "MISSING_PAYOUT_INFO",
                    "MISSING_PAYOUT_INFO"
            );
        }

        @Test
        @Order(4)
        void should_return_user_rewards_given_a_user_with_only_eth_wallet_and_valid_contact_info() {
            // Given
            postgresUserAdapter.savePayoutSettingsForUserId(individualUserId,
                    UserPayoutSettings.builder().ethWallet(Ethereum.wallet("0x01")).build());

            // When
            final List<UserRewardViewEntity> viewEntities =
                    customUserRewardRepository.getViewEntities(individualUserId,
                            List.of(), List.of(), null, null,
                            UserRewardView.SortBy.amount,
                            SortDirection.desc, 0, 100);

            // Then
            assertEquals(7, viewEntities.size());
            assertThat(viewEntities.stream().map(UserRewardViewEntity::getCurrency).toList()).containsExactly(
                    CurrencyEnumEntity.usd,
                    CurrencyEnumEntity.eth,
                    CurrencyEnumEntity.op,
                    CurrencyEnumEntity.usdc,
                    CurrencyEnumEntity.apt,
                    CurrencyEnumEntity.lords,
                    CurrencyEnumEntity.strk
            );
            assertThat(viewEntities.stream().map(UserRewardViewEntity::getStatus).toList()).containsExactly(
                    "MISSING_PAYOUT_INFO",
                    "COMPLETE",
                    "MISSING_PAYOUT_INFO",
                    "PROCESSING",
                    "MISSING_PAYOUT_INFO",
                    "PROCESSING",
                    "MISSING_PAYOUT_INFO"
            );
        }
    }


    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GivenAValidCompany {

        private static final UUID companyUserId = UUID.randomUUID();
        private static final Long companyGithubUserId = faker.random().nextLong();

        @Test
        @Order(1)
        void should_return_user_rewards_given_a_user_with_only_eth_wallet_for_valid_company() {
            // Given
            userRepository.save(
                    UserEntity.builder()
                            .id(companyUserId)
                            .githubUserId(companyGithubUserId)
                            .githubLogin(faker.name().username())
                            .githubAvatarUrl(faker.internet().avatar())
                            .githubEmail(faker.internet().emailAddress())
                            .roles(new UserRole[]{UserRole.USER})
                            .lastSeenAt(new Date())
                            .build()
            );
            userBillingProfileTypeRepository.save(UserBillingProfileTypeEntity.builder()
                    .userId(companyUserId)
                    .billingProfileType(UserBillingProfileTypeEntity.BillingProfileTypeEntity.COMPANY)
                    .build());
            companyBillingProfileRepository.save(CompanyBillingProfileEntity.builder()
                    .id(UUID.randomUUID())
                    .userId(companyUserId)
                    .verificationStatus(VerificationStatusEntity.VERIFIED)
                    .build());
            postgresUserAdapter.savePayoutSettingsForUserId(companyUserId,
                    UserPayoutSettings.builder().ethWallet(Ethereum.wallet("vitalik.eth")).build());
            paymentRequestRepository.saveAll(List.of(new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(),
                            companyGithubUserId, new Date(), BigDecimal.valueOf(10000), null, 1, projectId,
                            CurrencyEnumEntity.usd),
                    new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(), companyGithubUserId, new Date(),
                            BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.eth),
                    new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(), companyGithubUserId, new Date(),
                            BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.apt),
                    new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(), companyGithubUserId, new Date(),
                            BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.op),
                    new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(), companyGithubUserId, new Date(),
                            BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.strk),
                    new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(), companyGithubUserId, new Date(),
                            BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.lords),
                    new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(), companyGithubUserId, new Date(),
                            BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.usdc)
            ));

            // When
            final List<UserRewardViewEntity> viewEntities = customUserRewardRepository.getViewEntities(companyUserId,
                    List.of(), List.of(), null, null,
                    UserRewardView.SortBy.amount, SortDirection.desc, 0, 100);

            // Then
            assertEquals(7, viewEntities.size());
            assertThat(viewEntities.stream().map(UserRewardViewEntity::getCurrency).toList()).containsExactly(
                    CurrencyEnumEntity.usd,
                    CurrencyEnumEntity.eth,
                    CurrencyEnumEntity.op,
                    CurrencyEnumEntity.usdc,
                    CurrencyEnumEntity.apt,
                    CurrencyEnumEntity.lords,
                    CurrencyEnumEntity.strk
            );
            assertThat(viewEntities.stream().map(UserRewardViewEntity::getStatus).toList()).containsExactly(
                    "MISSING_PAYOUT_INFO",
                    "PENDING_INVOICE",
                    "MISSING_PAYOUT_INFO",
                    "PENDING_INVOICE",
                    "MISSING_PAYOUT_INFO",
                    "PENDING_INVOICE",
                    "MISSING_PAYOUT_INFO"
            );
        }

        @Test
        @Order(2)
        void should_return_user_rewards_given_a_user_with_only_usdc_wallet_for_valid_company_and_paid_rewards() {
            // Given
            List<UserRewardViewEntity> viewEntities = customUserRewardRepository.getViewEntities(companyUserId,
                    List.of(), List.of(), null, null,
                    UserRewardView.SortBy.amount, SortDirection.desc, 0, 100);
            paymentRepository.save(new PaymentEntity(UUID.randomUUID(), BigDecimal.ONE, "STRK",
                    JacksonUtil.toJsonNode("{}"), viewEntities.get(0).getId(), new Date()));
            final UserRewardViewEntity userRewardViewEntity = viewEntities.get(1);
            final PaymentRequestEntity paymentRequestEntity =
                    paymentRequestRepository.findById(userRewardViewEntity.getId()).orElseThrow();
            paymentRequestEntity.setInvoiceReceivedAt(new Date());
            paymentRequestRepository.save(paymentRequestEntity);
            paymentRepository.save(new PaymentEntity(UUID.randomUUID(), BigDecimal.ONE, "STRK",
                    JacksonUtil.toJsonNode("{}"), viewEntities.get(3).getId(), new Date()));

            // When
            viewEntities = customUserRewardRepository.getViewEntities(companyUserId,
                    List.of(), List.of(), null, null,
                    UserRewardView.SortBy.amount,
                    SortDirection.desc, 0, 100);
            // Then
            assertEquals(7, viewEntities.size());
            assertThat(viewEntities.stream().map(UserRewardViewEntity::getCurrency).toList()).containsExactly(
                    CurrencyEnumEntity.usd,
                    CurrencyEnumEntity.eth,
                    CurrencyEnumEntity.op,
                    CurrencyEnumEntity.usdc,
                    CurrencyEnumEntity.apt,
                    CurrencyEnumEntity.lords,
                    CurrencyEnumEntity.strk
            );
            assertThat(viewEntities.stream().map(UserRewardViewEntity::getStatus).toList()).containsExactly(
                    "COMPLETE",
                    "PROCESSING",
                    "MISSING_PAYOUT_INFO",
                    "COMPLETE",
                    "MISSING_PAYOUT_INFO",
                    "PENDING_INVOICE",
                    "MISSING_PAYOUT_INFO"
            );
        }

        @Test
        @Order(3)
        void should_return_user_rewards_given_a_user_with_only_banking_account_for_valid_company() {
            // Given
            postgresUserAdapter.savePayoutSettingsForUserId(companyUserId,
                    UserPayoutSettings.builder().sepaAccount(UserPayoutSettings.SepaAccount.builder().bic(faker.random().hex())
                            .accountNumber(AccountNumber.of("FR1014508000702139488771C56")).build()).build());

            // When
            final List<UserRewardViewEntity> viewEntities = customUserRewardRepository.getViewEntities(companyUserId,
                    List.of(), List.of(), null, null,
                    UserRewardView.SortBy.amount, SortDirection.desc, 0, 100);

            // Then
            assertEquals(7, viewEntities.size());
            assertThat(viewEntities.stream().map(UserRewardViewEntity::getCurrency).toList()).containsExactly(
                    CurrencyEnumEntity.usd,
                    CurrencyEnumEntity.eth,
                    CurrencyEnumEntity.op,
                    CurrencyEnumEntity.usdc,
                    CurrencyEnumEntity.apt,
                    CurrencyEnumEntity.lords,
                    CurrencyEnumEntity.strk
            );
            assertThat(viewEntities.stream().map(UserRewardViewEntity::getStatus).toList()).containsExactly(
                    "COMPLETE",
                    "MISSING_PAYOUT_INFO",
                    "MISSING_PAYOUT_INFO",
                    "COMPLETE",
                    "MISSING_PAYOUT_INFO",
                    "MISSING_PAYOUT_INFO",
                    "MISSING_PAYOUT_INFO"
            );
        }

        @Test
        @Order(5)
        void should_complete_lords_payment() {
            // Given
            final UUID lordsPaymentRequestId = customUserRewardRepository.getViewEntities(companyUserId,
                            List.of(), List.of(), null, null,
                            UserRewardView.SortBy.amount, SortDirection.desc, 0, 100)
                    .get(5).getId();
            paymentRepository.save(new PaymentEntity(UUID.randomUUID(), BigDecimal.ONE, "LORDS",
                    JacksonUtil.toJsonNode("{}"), lordsPaymentRequestId, new Date()));

            // When
            final List<UserRewardViewEntity> viewEntities = customUserRewardRepository.getViewEntities(companyUserId,
                    List.of(), List.of(), null, null,
                    UserRewardView.SortBy.amount, SortDirection.desc, 0, 100);

            // Then
            assertEquals(7, viewEntities.size());
            assertThat(viewEntities.stream().map(UserRewardViewEntity::getCurrency).toList()).containsExactly(
                    CurrencyEnumEntity.usd,
                    CurrencyEnumEntity.eth,
                    CurrencyEnumEntity.op,
                    CurrencyEnumEntity.usdc,
                    CurrencyEnumEntity.apt,
                    CurrencyEnumEntity.lords,
                    CurrencyEnumEntity.strk
            );
            assertThat(viewEntities.stream().map(UserRewardViewEntity::getStatus).toList()).containsExactly(
                    "COMPLETE",
                    "MISSING_PAYOUT_INFO",
                    "MISSING_PAYOUT_INFO",
                    "COMPLETE",
                    "MISSING_PAYOUT_INFO",
                    "COMPLETE",
                    "MISSING_PAYOUT_INFO"
            );
        }

        @Test
        @Order(6)
        void should_complete_usdc_payment() {
            // Given
            final UUID lordsPaymentRequestId = customUserRewardRepository.getViewEntities(companyUserId,
                            List.of(), List.of(), null, null,
                            UserRewardView.SortBy.amount, SortDirection.desc, 0, 100)
                    .get(6).getId();
            paymentRepository.save(new PaymentEntity(UUID.randomUUID(), BigDecimal.ONE, "USDC",
                    JacksonUtil.toJsonNode("{}"), lordsPaymentRequestId, new Date()));

            // When
            final List<UserRewardViewEntity> viewEntities = customUserRewardRepository.getViewEntities(companyUserId,
                    List.of(), List.of(), null, null,
                    UserRewardView.SortBy.amount, SortDirection.desc, 0, 100);

            // Then
            assertEquals(7, viewEntities.size());
            assertThat(viewEntities.stream().map(UserRewardViewEntity::getCurrency).toList()).containsExactly(
                    CurrencyEnumEntity.usd,
                    CurrencyEnumEntity.eth,
                    CurrencyEnumEntity.op,
                    CurrencyEnumEntity.usdc,
                    CurrencyEnumEntity.apt,
                    CurrencyEnumEntity.lords,
                    CurrencyEnumEntity.strk
            );
            assertThat(viewEntities.stream().map(UserRewardViewEntity::getStatus).toList()).containsExactly(
                    "COMPLETE",
                    "MISSING_PAYOUT_INFO",
                    "MISSING_PAYOUT_INFO",
                    "COMPLETE",
                    "MISSING_PAYOUT_INFO",
                    "COMPLETE",
                    "COMPLETE"
            );
        }
    }


    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ShouldReturnPendingInvoiceReward {
        private static final Long githubUserId = faker.random().nextLong();
        private static final UUID userId = UUID.randomUUID();
        private static final UUID projectId = UUID.randomUUID();

        @Test
        @Order(1)
        void should_return_one_reward() {
            // Given
            userRepository.save(
                    UserEntity.builder()
                            .id(userId)
                            .githubUserId(githubUserId)
                            .githubLogin(faker.name().username())
                            .githubAvatarUrl(faker.internet().avatar())
                            .githubEmail(faker.internet().emailAddress())
                            .roles(new UserRole[]{UserRole.USER})
                            .lastSeenAt(new Date())
                            .build()
            );
            projectRepository.save(
                    ProjectEntity.builder()
                            .id(projectId)
                            .name(faker.pokemon().name())
                            .shortDescription(faker.pokemon().location())
                            .longDescription(faker.harryPotter().location())
                            .telegramLink(faker.internet().url())
                            .logoUrl(faker.internet().avatar())
                            .hiring(false)
                            .rank(0)
                            .visibility(ProjectVisibilityEnumEntity.PUBLIC)
                            .ignorePullRequests(false)
                            .ignoreIssues(false)
                            .ignoreCodeReviews(false)
                            .build());
            postgresUserAdapter.savePayoutSettingsForUserId(userId,
                    UserPayoutSettings.builder().ethWallet(Ethereum.wallet("vitalik.eth"))
                            .sepaAccount(UserPayoutSettings.SepaAccount.builder()
                                    .bic(faker.random().hex()).accountNumber(AccountNumber.of(
                                            "ES6621000418401234567891")).build())
                            .build());

            userBillingProfileTypeRepository.save(UserBillingProfileTypeEntity.builder()
                    .userId(userId)
                    .billingProfileType(UserBillingProfileTypeEntity.BillingProfileTypeEntity.COMPANY)
                    .build());
            companyBillingProfileRepository.save(CompanyBillingProfileEntity.builder()
                    .verificationStatus(VerificationStatusEntity.VERIFIED)
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .build());
            final UUID completedReward = UUID.randomUUID();
            final UUID pendingInvoiceRewardIdUsdc = UUID.randomUUID();
            final UUID pendingInvoiceRewardIdLords = UUID.randomUUID();
            paymentRequestRepository.saveAll(List.of(
                    new PaymentRequestEntity(pendingInvoiceRewardIdUsdc, UUID.randomUUID(),
                            githubUserId, new Date(), BigDecimal.valueOf(10000), null, 1, projectId,
                            CurrencyEnumEntity.usd),
                    new PaymentRequestEntity(completedReward, UUID.randomUUID(), githubUserId, new Date(),
                            BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.eth),
                    new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(), githubUserId, new Date(),
                            BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.apt),
                    new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(), githubUserId, new Date(),
                            BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.op),
                    new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(), githubUserId, new Date(),
                            BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.strk),
                    new PaymentRequestEntity(pendingInvoiceRewardIdLords, UUID.randomUUID(), githubUserId, new Date(),
                            BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.lords)
            ));
            paymentRepository.save(new PaymentEntity(UUID.randomUUID(), BigDecimal.ONE, "STRK",
                    JacksonUtil.toJsonNode("{}"), completedReward, new Date()));

            // When
            final List<UserRewardViewEntity> pendingInvoicesViewEntities =
                    customUserRewardRepository.getPendingInvoicesViewEntities(githubUserId);

            // Then
            assertThat(pendingInvoicesViewEntities.stream().map(UserRewardViewEntity::getCurrency).toList())
                    .containsExactlyInAnyOrder(CurrencyEnumEntity.usd, CurrencyEnumEntity.lords);
            assertThat(pendingInvoicesViewEntities.stream().map(UserRewardViewEntity::getId).toList())
                    .containsExactlyInAnyOrder(pendingInvoiceRewardIdUsdc, pendingInvoiceRewardIdLords);
            assertEquals("PENDING_INVOICE", pendingInvoicesViewEntities.get(0).getStatus());
            assertEquals("PENDING_INVOICE", pendingInvoicesViewEntities.get(1).getStatus());
        }

        @Test
        @Order(2)
        void should_return_no_reward() {
            // Given
            postgresUserAdapter.savePayoutSettingsForUserId(userId,
                    UserPayoutSettings.builder().ethWallet(Ethereum.wallet("vitalik.eth")).build());
            userBillingProfileTypeRepository.save(UserBillingProfileTypeEntity.builder()
                    .billingProfileType(UserBillingProfileTypeEntity.BillingProfileTypeEntity.INDIVIDUAL)
                    .userId(userId)
                    .build());
            // When
            final List<UserRewardViewEntity> pendingInvoicesViewEntities =
                    customUserRewardRepository.getPendingInvoicesViewEntities(githubUserId);

            // Then
            assertEquals(0, pendingInvoicesViewEntities.size());
        }
    }


}
