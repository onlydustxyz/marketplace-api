package onlydust.com.marketplace.api.postgres.adapter.it.repository;

import com.vladmihalcea.hibernate.type.json.internal.JacksonUtil;
import onlydust.com.marketplace.project.domain.model.UserPayoutSettings;
import onlydust.com.marketplace.project.domain.model.UserRole;
import onlydust.com.marketplace.api.postgres.adapter.PostgresUserAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.RewardViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.*;
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
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

public class CustomRewardRepositoryIT extends AbstractPostgresIT {

    @Autowired
    UserRepository userRepository;
    @Autowired
    PaymentRequestRepository paymentRequestRepository;
    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    CustomRewardRepository customRewardRepository;
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


    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GivenAnIndividual {

        static final UUID userId = UUID.randomUUID();
        static final Long githubUserId = faker.random().nextLong();
        static final UUID projectId = UUID.randomUUID();
        static final UUID rewardId = UUID.randomUUID();

        @Test
        @Order(1)
        void should_return_payout_info_missing_status_for_an_individual() {
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
            userBillingProfileTypeRepository.save(UserBillingProfileTypeEntity.builder()
                    .billingProfileType(UserBillingProfileTypeEntity.BillingProfileTypeEntity.INDIVIDUAL)
                    .userId(userId)
                    .build());
            individualBillingProfileRepository.save(IndividualBillingProfileEntity.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .verificationStatus(VerificationStatusEntity.VERIFIED)
                    .build());
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
            paymentRequestRepository.save(new PaymentRequestEntity(rewardId, UUID.randomUUID(), githubUserId,
                    new Date(), BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.lords));

            // When
            final RewardViewEntity userReward = customRewardRepository.findUserRewardViewEntityByd(rewardId);
            final RewardViewEntity projectReward = customRewardRepository.findProjectRewardViewEntityByd(rewardId);

            // Then
            Assertions.assertEquals("MISSING_PAYOUT_INFO", userReward.getStatus());
            Assertions.assertEquals("PENDING_CONTRIBUTOR", projectReward.getStatus());
        }

        @Test
        @Order(2)
        void should_return_processing_for_an_individual() {
            // Given
            postgresUserAdapter.savePayoutSettingsForUserId(userId,
                    UserPayoutSettings.builder()
                            .ethWallet(Ethereum.wallet("0x01"))
                            .build());

            // When
            final RewardViewEntity userReward = customRewardRepository.findUserRewardViewEntityByd(rewardId);
            final RewardViewEntity projectReward = customRewardRepository.findProjectRewardViewEntityByd(rewardId);

            // Then
            Assertions.assertEquals("PROCESSING", userReward.getStatus());
            Assertions.assertEquals("PROCESSING", projectReward.getStatus());
        }

        @Test
        @Order(3)
        void should_return_complete_for_an_individual() {
            // Given
            paymentRepository.save(new PaymentEntity(UUID.randomUUID(), BigDecimal.ONE, "STRK",
                    JacksonUtil.toJsonNode("{}"), rewardId, new Date()));

            // When
            final RewardViewEntity userReward = customRewardRepository.findUserRewardViewEntityByd(rewardId);
            final RewardViewEntity projectReward = customRewardRepository.findProjectRewardViewEntityByd(rewardId);

            // Then
            Assertions.assertEquals("COMPLETE", userReward.getStatus());
            Assertions.assertEquals("COMPLETE", projectReward.getStatus());
        }
    }


    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GivenACompany {

        static final UUID userId = UUID.randomUUID();
        static final Long githubUserId = faker.random().nextLong();
        static final UUID projectId = UUID.randomUUID();
        static final UUID rewardId = UUID.randomUUID();

        @Test
        @Order(1)
        void should_return_payout_info_missing_status() {
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
            userBillingProfileTypeRepository.save(UserBillingProfileTypeEntity.builder()
                    .userId(userId)
                    .billingProfileType(UserBillingProfileTypeEntity.BillingProfileTypeEntity.COMPANY)
                    .build());
            companyBillingProfileRepository.save(CompanyBillingProfileEntity.builder()
                    .userId(userId)
                    .verificationStatus(VerificationStatusEntity.VERIFIED)
                    .id(UUID.randomUUID())
                    .build());
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
            paymentRequestRepository.save(new PaymentRequestEntity(rewardId, UUID.randomUUID(), githubUserId,
                    new Date(), BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.lords));

            // When
            final RewardViewEntity userReward = customRewardRepository.findUserRewardViewEntityByd(rewardId);
            final RewardViewEntity projectReward = customRewardRepository.findProjectRewardViewEntityByd(rewardId);

            // Then
            Assertions.assertEquals("MISSING_PAYOUT_INFO", userReward.getStatus());
            Assertions.assertEquals("PENDING_CONTRIBUTOR", projectReward.getStatus());
        }

        @Test
        @Order(2)
        void should_return_pending_invoice_status() {
            // Given
            postgresUserAdapter.savePayoutSettingsForUserId(userId,
                    UserPayoutSettings.builder()
                            .ethWallet(Ethereum.wallet("0x01"))
                            .build());

            // When
            final RewardViewEntity userReward = customRewardRepository.findUserRewardViewEntityByd(rewardId);
            final RewardViewEntity projectReward = customRewardRepository.findProjectRewardViewEntityByd(rewardId);

            // Then
            Assertions.assertEquals("PENDING_INVOICE", userReward.getStatus());
            Assertions.assertEquals("PROCESSING", projectReward.getStatus());
        }


        @Test
        @Order(3)
        void should_return_processing_status() {
            // Given
            final PaymentRequestEntity paymentRequestEntity = paymentRequestRepository.findById(rewardId).orElseThrow();
            paymentRequestEntity.setInvoiceReceivedAt(new Date());
            paymentRequestRepository.save(paymentRequestEntity);

            // When
            final RewardViewEntity userReward = customRewardRepository.findUserRewardViewEntityByd(rewardId);
            final RewardViewEntity projectReward = customRewardRepository.findProjectRewardViewEntityByd(rewardId);

            // Then
            Assertions.assertEquals("PROCESSING", userReward.getStatus());
            Assertions.assertEquals("PROCESSING", projectReward.getStatus());
        }

        @Test
        @Order(4)
        void should_return_complete_status() {
            // Given
            paymentRepository.save(new PaymentEntity(UUID.randomUUID(), BigDecimal.ONE, "STRK",
                    JacksonUtil.toJsonNode("{}"), rewardId, new Date()));

            // When
            final RewardViewEntity userReward = customRewardRepository.findUserRewardViewEntityByd(rewardId);
            final RewardViewEntity projectReward = customRewardRepository.findProjectRewardViewEntityByd(rewardId);

            // Then
            Assertions.assertEquals("COMPLETE", userReward.getStatus());
            Assertions.assertEquals("COMPLETE", projectReward.getStatus());
        }
    }


}
