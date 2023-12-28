package onlydust.com.marketplace.api.postgres.adapter.it.repository;

import com.vladmihalcea.hibernate.type.json.internal.JacksonUtil;
import nl.garvelink.iban.IBAN;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation;
import onlydust.com.marketplace.api.domain.view.UserRewardView;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;
import onlydust.com.marketplace.api.postgres.adapter.PostgresUserAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserRewardViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProjectVisibilityEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.it.AbstractPostgresIT;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomUserPayoutInfoRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomUserRewardRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.AuthUserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.CryptoUsdQuotesRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.PaymentRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.PaymentRequestRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CustomUserRewardRepositoryIT extends AbstractPostgresIT {

    private static UUID userId = UUID.randomUUID();
    private static Long githubUserId = faker.random().nextLong();
    private static UUID projectId = UUID.randomUUID();
    @Autowired
    AuthUserRepository authUserRepository;
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

    @Test
    @Order(1)
    void should_return_user_rewards_given_a_user_without_payout_info_and_with_rewards() {
        // Given
        authUserRepository.save(new AuthUserEntity(userId, githubUserId, faker.rickAndMorty().location(), new Date(),
                faker.rickAndMorty().character(), faker.internet().url(), new Date(), false));
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
        cryptoUsdQuotesRepository.saveAll(List.of(new CryptoUsdQuotesEntity(CurrencyEnumEntity.eth,
                BigDecimal.valueOf(1000), new Date()), new CryptoUsdQuotesEntity(CurrencyEnumEntity.apt,
                BigDecimal.valueOf(100), new Date()), new CryptoUsdQuotesEntity(CurrencyEnumEntity.op,
                BigDecimal.valueOf(10), new Date())));
        final UUID rewardPaid = UUID.randomUUID();
        paymentRequestRepository.saveAll(List.of(new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(),
                        githubUserId, new Date(), BigDecimal.valueOf(10000), null, 1, projectId,
                        CurrencyEnumEntity.usd),
                new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(), githubUserId, new Date(),
                        BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.eth),
                new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(), githubUserId, new Date(),
                        BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.apt),
                new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(), githubUserId, new Date(),
                        BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.op),
                new PaymentRequestEntity(UUID.randomUUID(), UUID.randomUUID(), githubUserId, new Date(),
                        BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.strk),
                new PaymentRequestEntity(rewardPaid, UUID.randomUUID(), githubUserId, new Date(), BigDecimal.ONE,
                        null, 1, projectId, CurrencyEnumEntity.strk)));
        paymentRepository.save(new PaymentEntity(UUID.randomUUID(), BigDecimal.ONE, "STRK", JacksonUtil.toJsonNode(
                "{}"), rewardPaid, new Date()));

        // When
        final List<UserRewardViewEntity> viewEntities = customUserRewardRepository.getViewEntities(userId,
                List.of(), List.of(), null, null,
                UserRewardView.SortBy.amount, SortDirection.desc, 0, 100);

        // Then
        assertEquals(6, viewEntities.size());
        assertEquals(CurrencyEnumEntity.usd, viewEntities.get(0).getCurrency());
        assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(0).getStatus());
        assertEquals(CurrencyEnumEntity.eth, viewEntities.get(1).getCurrency());
        assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(1).getStatus());
        assertEquals(CurrencyEnumEntity.apt, viewEntities.get(2).getCurrency());
        assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(2).getStatus());
        assertEquals(CurrencyEnumEntity.op, viewEntities.get(3).getCurrency());
        assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(3).getStatus());
        assertEquals(CurrencyEnumEntity.strk, viewEntities.get(4).getCurrency());
        assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(4).getStatus());
        assertEquals(CurrencyEnumEntity.strk, viewEntities.get(5).getCurrency());
        assertEquals("COMPLETE", viewEntities.get(5).getStatus());
    }


    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GivenAValidIndividual {

        private static UUID individualIserId = UUID.randomUUID();
        private static Long individualIgithubUserId = faker.random().nextLong();

        @Test
        @Order(1)
        void should_return_user_rewards_given_a_user_with_only_stark_wallet_and_valid_contact_info() {
            // Given
            authUserRepository.save(new AuthUserEntity(individualIserId, individualIgithubUserId,
                    faker.rickAndMorty().location(), new Date(), faker.rickAndMorty().character(),
                    faker.internet().url(), new Date(), false));
            postgresUserAdapter.savePayoutInformationForUserId(individualIserId,
                    UserPayoutInformation.builder().person(UserPayoutInformation.Person.builder()
                                    .lastName(faker.name().lastName()).firstName(faker.name().firstName())
                                    .build())
                            .location(UserPayoutInformation.Location.builder()
                                    .address(faker.address().fullAddress())
                                    .city(faker.address().city())
                                    .postalCode(faker.address()
                                            .zipCode()).country(faker.address().country()).build())
                            .payoutSettings(UserPayoutInformation.PayoutSettings.builder()
                                    .starknetAddress(faker.random().hex()).build()).build());
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
                            BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.lords)
            ));


            // When
            final List<UserRewardViewEntity> viewEntities =
                    customUserRewardRepository.getViewEntities(individualIserId,
                            List.of(), List.of(), null, null,
                            UserRewardView.SortBy.amount,
                            SortDirection.desc, 0, 100);


            // Then
            assertEquals(6, viewEntities.size());
            assertEquals(CurrencyEnumEntity.usd, viewEntities.get(0).getCurrency());
            assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(0).getStatus());
            assertEquals(CurrencyEnumEntity.eth, viewEntities.get(1).getCurrency());
            assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(1).getStatus());
            assertEquals(CurrencyEnumEntity.apt, viewEntities.get(2).getCurrency());
            assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(2).getStatus());
            assertEquals(CurrencyEnumEntity.op, viewEntities.get(3).getCurrency());
            assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(3).getStatus());
            assertEquals(CurrencyEnumEntity.strk, viewEntities.get(4).getCurrency());
            assertEquals("PROCESSING", viewEntities.get(4).getStatus());
            assertEquals(CurrencyEnumEntity.lords, viewEntities.get(5).getCurrency());
            assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(5).getStatus());
        }

        @Test
        @Order(2)
        void should_return_user_rewards_given_a_user_with_only_op_wallet_and_valid_contact_info() {
            // Given
            postgresUserAdapter.savePayoutInformationForUserId(individualIserId,
                    UserPayoutInformation.builder()
                            .person(UserPayoutInformation.Person.builder().lastName(faker.name().lastName()).firstName(faker.name().firstName()).build())
                            .location(UserPayoutInformation.Location.builder().address(faker.address().fullAddress()).city(faker.address().city()).postalCode(faker.address().zipCode()).country(faker.address().country()).build())
                            .payoutSettings(UserPayoutInformation.PayoutSettings.builder()
                                    .optimismAddress(faker.random().hex()).build()).build());
            List<UserRewardViewEntity> viewEntities = customUserRewardRepository.getViewEntities(individualIserId,
                    List.of(), List.of(), null, null,
                    UserRewardView.SortBy.amount,
                    SortDirection.desc, 0, 100);
            paymentRepository.save(new PaymentEntity(UUID.randomUUID(), BigDecimal.ONE, "USD",
                    JacksonUtil.toJsonNode("{}"), viewEntities.get(1).getId(), new Date()));

            // When
            viewEntities =
                    customUserRewardRepository.getViewEntities(individualIserId,
                            List.of(), List.of(), null, null,
                            UserRewardView.SortBy.amount,
                            SortDirection.desc, 0, 100);

            // Then
            assertEquals(6, viewEntities.size());
            assertEquals(CurrencyEnumEntity.usd, viewEntities.get(0).getCurrency());
            assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(0).getStatus());
            assertEquals(CurrencyEnumEntity.eth, viewEntities.get(1).getCurrency());
            assertEquals("COMPLETE", viewEntities.get(1).getStatus());
            assertEquals(CurrencyEnumEntity.apt, viewEntities.get(2).getCurrency());
            assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(2).getStatus());
            assertEquals(CurrencyEnumEntity.op, viewEntities.get(3).getCurrency());
            assertEquals("PROCESSING", viewEntities.get(3).getStatus());
            assertEquals(CurrencyEnumEntity.strk, viewEntities.get(4).getCurrency());
            assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(4).getStatus());
            assertEquals(CurrencyEnumEntity.lords, viewEntities.get(5).getCurrency());
            assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(5).getStatus());
        }

        @Test
        @Order(3)
        void should_return_user_rewards_given_a_user_with_only_apt_wallet_and_valid_contact_info() {
            // Given
            postgresUserAdapter.savePayoutInformationForUserId(individualIserId,
                    UserPayoutInformation.builder().person(UserPayoutInformation.Person.builder().lastName(faker.name().lastName()).firstName(faker.name().firstName()).build()).location(UserPayoutInformation.Location.builder().address(faker.address().fullAddress()).city(faker.address().city()).postalCode(faker.address().zipCode()).country(faker.address().country()).build()).payoutSettings(UserPayoutInformation.PayoutSettings.builder().aptosAddress(faker.random().hex()).build()).build());

            // When
            final List<UserRewardViewEntity> viewEntities =
                    customUserRewardRepository.getViewEntities(individualIserId,
                            List.of(), List.of(), null, null,
                            UserRewardView.SortBy.amount,
                            SortDirection.desc, 0, 100);

            // Then
            assertEquals(6, viewEntities.size());
            assertEquals(CurrencyEnumEntity.usd, viewEntities.get(0).getCurrency());
            assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(0).getStatus());
            assertEquals(CurrencyEnumEntity.eth, viewEntities.get(1).getCurrency());
            assertEquals("COMPLETE", viewEntities.get(1).getStatus());
            assertEquals(CurrencyEnumEntity.apt, viewEntities.get(2).getCurrency());
            assertEquals("PROCESSING", viewEntities.get(2).getStatus());
            assertEquals(CurrencyEnumEntity.op, viewEntities.get(3).getCurrency());
            assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(3).getStatus());
            assertEquals(CurrencyEnumEntity.strk, viewEntities.get(4).getCurrency());
            assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(4).getStatus());
            assertEquals(CurrencyEnumEntity.lords, viewEntities.get(5).getCurrency());
            assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(5).getStatus());
        }

        @Test
        @Order(4)
        void should_return_user_rewards_given_a_user_with_only_eth_wallet_and_valid_contact_info() {
            // Given
            postgresUserAdapter.savePayoutInformationForUserId(individualIserId,
                    UserPayoutInformation.builder().person(UserPayoutInformation.Person.builder().lastName(faker.name().lastName()).firstName(faker.name().firstName()).build()).location(UserPayoutInformation.Location.builder().address(faker.address().fullAddress()).city(faker.address().city()).postalCode(faker.address().zipCode()).country(faker.address().country()).build()).payoutSettings(UserPayoutInformation.PayoutSettings.builder().ethAddress(faker.random().hex()).build()).build());

            // When
            final List<UserRewardViewEntity> viewEntities =
                    customUserRewardRepository.getViewEntities(individualIserId,
                            List.of(), List.of(), null, null,
                            UserRewardView.SortBy.amount,
                            SortDirection.desc, 0, 100);

            // Then
            assertEquals(6, viewEntities.size());
            assertEquals(CurrencyEnumEntity.usd, viewEntities.get(0).getCurrency());
            assertEquals("PROCESSING", viewEntities.get(0).getStatus());
            assertEquals(CurrencyEnumEntity.eth, viewEntities.get(1).getCurrency());
            assertEquals("COMPLETE", viewEntities.get(1).getStatus());
            assertEquals(CurrencyEnumEntity.apt, viewEntities.get(2).getCurrency());
            assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(2).getStatus());
            assertEquals(CurrencyEnumEntity.op, viewEntities.get(3).getCurrency());
            assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(3).getStatus());
            assertEquals(CurrencyEnumEntity.strk, viewEntities.get(4).getCurrency());
            assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(4).getStatus());
            assertEquals(CurrencyEnumEntity.lords, viewEntities.get(5).getCurrency());
            assertEquals("PROCESSING", viewEntities.get(5).getStatus());
        }
    }


    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GivenAValidCompany {

        private static final UUID companyUserId = UUID.randomUUID();
        private static final Long companyGithubUserId = faker.random().nextLong();

        @Test
        @Order(1)
        void should_return_user_rewards_given_a_user_with_only_usdc_wallet_for_valid_company() {
            // Given
            authUserRepository.save(new AuthUserEntity(companyUserId, companyGithubUserId,
                    faker.rickAndMorty().location(), new Date(), faker.rickAndMorty().character(),
                    faker.internet().url(), new Date(), false));
            postgresUserAdapter.savePayoutInformationForUserId(companyUserId,
                    UserPayoutInformation.builder().isACompany(true)
                            .company(UserPayoutInformation.Company.builder().name(faker.name().name())
                                    .owner(UserPayoutInformation.Person.builder().lastName(faker.name().lastName()).firstName(faker.name().firstName()).build())
                                    .identificationNumber(faker.number().digit()).build()).location(UserPayoutInformation.Location.builder().address(faker.address().fullAddress()).city(faker.address().city()).postalCode(faker.address().zipCode()).country(faker.address().country()).build())
                            .payoutSettings(UserPayoutInformation.PayoutSettings.builder()
                                    .ethName(faker.random().hex())
                                    .usdPreferredMethodEnum(UserPayoutInformation.UsdPreferredMethodEnum.CRYPTO).build()).build());
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
                            BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.lords)
            ));

            // When
            final List<UserRewardViewEntity> viewEntities = customUserRewardRepository.getViewEntities(companyUserId,
                    List.of(), List.of(), null, null,
                    UserRewardView.SortBy.amount, SortDirection.desc, 0, 100);

            // Then
            assertEquals(6, viewEntities.size());
            assertEquals(CurrencyEnumEntity.usd, viewEntities.get(0).getCurrency());
            assertEquals("PENDING_INVOICE", viewEntities.get(0).getStatus());
            assertEquals(CurrencyEnumEntity.eth, viewEntities.get(1).getCurrency());
            assertEquals("PENDING_INVOICE", viewEntities.get(1).getStatus());
            assertEquals(CurrencyEnumEntity.apt, viewEntities.get(2).getCurrency());
            assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(2).getStatus());
            assertEquals(CurrencyEnumEntity.op, viewEntities.get(3).getCurrency());
            assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(3).getStatus());
            assertEquals(CurrencyEnumEntity.strk, viewEntities.get(4).getCurrency());
            assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(4).getStatus());
            assertEquals(CurrencyEnumEntity.lords, viewEntities.get(5).getCurrency());
            assertEquals("PENDING_INVOICE", viewEntities.get(5).getStatus());
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
            assertEquals(6, viewEntities.size());
            assertEquals(CurrencyEnumEntity.usd, viewEntities.get(0).getCurrency());
            assertEquals("COMPLETE", viewEntities.get(0).getStatus());
            assertEquals(CurrencyEnumEntity.eth, viewEntities.get(1).getCurrency());
            assertEquals("PROCESSING", viewEntities.get(1).getStatus());
            assertEquals(CurrencyEnumEntity.apt, viewEntities.get(2).getCurrency());
            assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(2).getStatus());
            assertEquals(CurrencyEnumEntity.op, viewEntities.get(3).getCurrency());
            assertEquals("COMPLETE", viewEntities.get(3).getStatus());
            assertEquals(CurrencyEnumEntity.strk, viewEntities.get(4).getCurrency());
            assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(4).getStatus());
            assertEquals(CurrencyEnumEntity.lords, viewEntities.get(5).getCurrency());
            assertEquals("PENDING_INVOICE", viewEntities.get(5).getStatus());
        }

        @Test
        @Order(3)
        void should_return_user_rewards_given_a_user_with_only_banking_account_for_valid_company() {
            // Given
            postgresUserAdapter.savePayoutInformationForUserId(companyUserId,
                    UserPayoutInformation.builder().isACompany(true).company(UserPayoutInformation.Company.builder().name(faker.name().name()).owner(UserPayoutInformation.Person.builder().lastName(faker.name().lastName()).firstName(faker.name().firstName()).build()).identificationNumber(faker.number().digit()).build()).location(UserPayoutInformation.Location.builder().address(faker.address().fullAddress()).city(faker.address().city()).postalCode(faker.address().zipCode()).country(faker.address().country()).build()).payoutSettings(UserPayoutInformation.PayoutSettings.builder().sepaAccount(UserPayoutInformation.SepaAccount.builder().bic(faker.random().hex()).iban(IBAN.valueOf("FR1014508000702139488771C56")).build()).usdPreferredMethodEnum(UserPayoutInformation.UsdPreferredMethodEnum.FIAT).build()).build());

            // When
            final List<UserRewardViewEntity> viewEntities = customUserRewardRepository.getViewEntities(companyUserId,
                    List.of(), List.of(), null, null,
                    UserRewardView.SortBy.amount, SortDirection.desc, 0, 100);

            // Then
            assertEquals(6, viewEntities.size());
            assertEquals(CurrencyEnumEntity.usd, viewEntities.get(0).getCurrency());
            assertEquals("COMPLETE", viewEntities.get(0).getStatus());
            assertEquals(CurrencyEnumEntity.eth, viewEntities.get(1).getCurrency());
            assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(1).getStatus());
            assertEquals(CurrencyEnumEntity.apt, viewEntities.get(2).getCurrency());
            assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(2).getStatus());
            assertEquals(CurrencyEnumEntity.op, viewEntities.get(3).getCurrency());
            assertEquals("COMPLETE", viewEntities.get(3).getStatus());
            assertEquals(CurrencyEnumEntity.strk, viewEntities.get(4).getCurrency());
            assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(4).getStatus());
            assertEquals(CurrencyEnumEntity.lords, viewEntities.get(5).getCurrency());
            assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(5).getStatus());
        }

        @Test
        @Order(4)
        void should_return_user_rewards_payout_info_missing_given_missing_location() {
            // Given
            postgresUserAdapter.savePayoutInformationForUserId(companyUserId,
                    UserPayoutInformation.builder().isACompany(true)
                            .company(UserPayoutInformation.Company.builder().name(faker.name().name())
                                    .owner(UserPayoutInformation.Person.builder()
                                            .lastName(faker.name().lastName())
                                            .firstName(faker.name().firstName()).build())
                                    .identificationNumber(faker.number().digit()).build())
                            .payoutSettings(UserPayoutInformation.PayoutSettings.builder()
                                    .sepaAccount(UserPayoutInformation.SepaAccount.builder()
                                            .bic(faker.random().hex()).iban(IBAN.valueOf("FR1014508000702139488771C56"
                                            )).build())
                                    .aptosAddress(faker.random().hex())
                                    .starknetAddress(faker.random().hex())
                                    .aptosAddress(faker.random().hex())
                                    .ethAddress(faker.random().hex())
                                    .usdPreferredMethodEnum(UserPayoutInformation.UsdPreferredMethodEnum.FIAT)
                                    .build()).build());

            // When
            final List<UserRewardViewEntity> viewEntities = customUserRewardRepository.getViewEntities(companyUserId,
                    List.of(), List.of(), null, null,
                    UserRewardView.SortBy.amount, SortDirection.desc, 0, 100);

            // Then
            assertEquals(6, viewEntities.size());
            assertEquals(CurrencyEnumEntity.usd, viewEntities.get(0).getCurrency());
            assertEquals("COMPLETE", viewEntities.get(0).getStatus());
            assertEquals(CurrencyEnumEntity.apt, viewEntities.get(2).getCurrency());
            assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(2).getStatus());
            assertEquals(CurrencyEnumEntity.op, viewEntities.get(3).getCurrency());
            assertEquals("COMPLETE", viewEntities.get(3).getStatus());
            assertEquals(CurrencyEnumEntity.strk, viewEntities.get(4).getCurrency());
            assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(4).getStatus());
            assertEquals(CurrencyEnumEntity.eth, viewEntities.get(1).getCurrency());
            assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(1).getStatus());
            assertEquals(CurrencyEnumEntity.lords, viewEntities.get(5).getCurrency());
            assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(5).getStatus());
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
            assertEquals(6, viewEntities.size());
            assertEquals(CurrencyEnumEntity.usd, viewEntities.get(0).getCurrency());
            assertEquals("COMPLETE", viewEntities.get(0).getStatus());
            assertEquals(CurrencyEnumEntity.apt, viewEntities.get(2).getCurrency());
            assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(2).getStatus());
            assertEquals(CurrencyEnumEntity.op, viewEntities.get(3).getCurrency());
            assertEquals("COMPLETE", viewEntities.get(3).getStatus());
            assertEquals(CurrencyEnumEntity.strk, viewEntities.get(4).getCurrency());
            assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(4).getStatus());
            assertEquals(CurrencyEnumEntity.eth, viewEntities.get(1).getCurrency());
            assertEquals("MISSING_PAYOUT_INFO", viewEntities.get(1).getStatus());
            assertEquals(CurrencyEnumEntity.lords, viewEntities.get(5).getCurrency());
            assertEquals("COMPLETE", viewEntities.get(5).getStatus());
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
            authUserRepository.save(new AuthUserEntity(userId, githubUserId, faker.rickAndMorty().location(),
                    new Date(),
                    faker.rickAndMorty().character(), faker.internet().url(), new Date(), false));
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
            postgresUserAdapter.savePayoutInformationForUserId(userId,
                    UserPayoutInformation.builder().isACompany(true)
                            .company(UserPayoutInformation.Company.builder().name(faker.name().name())
                                    .owner(UserPayoutInformation.Person.builder().lastName(faker.name().lastName()).firstName(faker.name().firstName()).build())
                                    .identificationNumber(faker.number().digit()).build())
                            .location(UserPayoutInformation.Location.builder().address(faker.address().fullAddress()).city(faker.address().city()).postalCode(faker.address().zipCode()).country(faker.address().country()).build())
                            .payoutSettings(UserPayoutInformation.PayoutSettings.builder()
                                    .ethName(faker.random().hex())
                                    .usdPreferredMethodEnum(UserPayoutInformation.UsdPreferredMethodEnum.CRYPTO).build()).build());
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
            assertEquals(2, pendingInvoicesViewEntities.size());
            assertEquals("PENDING_INVOICE", pendingInvoicesViewEntities.get(0).getStatus());
            assertTrue(List.of(pendingInvoiceRewardIdUsdc, pendingInvoiceRewardIdLords, pendingInvoiceRewardIdUsdc)
                    .contains(pendingInvoicesViewEntities.get(0).getId()));
            assertEquals("PENDING_INVOICE", pendingInvoicesViewEntities.get(1).getStatus());
            assertTrue(List.of(pendingInvoiceRewardIdUsdc, pendingInvoiceRewardIdLords, pendingInvoiceRewardIdUsdc)
                    .contains(pendingInvoicesViewEntities.get(1).getId()));

        }

        @Test
        @Order(2)
        void should_return_no_reward() {
            // Given
            postgresUserAdapter.savePayoutInformationForUserId(userId,
                    UserPayoutInformation.builder().isACompany(true)
                            .company(UserPayoutInformation.Company.builder().name(faker.name().name())
                                    .owner(UserPayoutInformation.Person.builder().lastName(faker.name().lastName()).firstName(faker.name().firstName()).build())
                                    .identificationNumber(faker.number().digit()).build())
                            .payoutSettings(UserPayoutInformation.PayoutSettings.builder()
                                    .ethName(faker.random().hex())
                                    .usdPreferredMethodEnum(UserPayoutInformation.UsdPreferredMethodEnum.CRYPTO).build()).build());

            // When
            final List<UserRewardViewEntity> pendingInvoicesViewEntities =
                    customUserRewardRepository.getPendingInvoicesViewEntities(githubUserId);

            // Then
            assertEquals(0, pendingInvoicesViewEntities.size());
        }
    }


}
