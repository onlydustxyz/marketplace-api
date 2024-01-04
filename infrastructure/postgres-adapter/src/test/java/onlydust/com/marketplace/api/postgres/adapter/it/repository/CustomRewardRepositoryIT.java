package onlydust.com.marketplace.api.postgres.adapter.it.repository;

import com.vladmihalcea.hibernate.type.json.internal.JacksonUtil;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation;
import onlydust.com.marketplace.api.domain.model.blockchain.Optimism;
import onlydust.com.marketplace.api.domain.model.blockchain.StarkNet;
import onlydust.com.marketplace.api.postgres.adapter.PostgresUserAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.RewardViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.AuthUserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.PaymentEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.PaymentRequestEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProjectVisibilityEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.it.AbstractPostgresIT;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomRewardRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomUserPayoutInfoRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.AuthUserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.CryptoUsdQuotesRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.PaymentRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.PaymentRequestRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

public class CustomRewardRepositoryIT extends AbstractPostgresIT {

  @Autowired
  AuthUserRepository authUserRepository;
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
          UserPayoutInformation.builder()
              .person(UserPayoutInformation.Person.builder()
                  .lastName(faker.name().lastName()).firstName(faker.name().firstName())
                  .build())
              .location(UserPayoutInformation.Location.builder()
                  .address(faker.address().fullAddress())
                  .city(faker.address().city())
                  .postalCode(faker.address()
                      .zipCode()).country(faker.address().country()).build())
              .build());
      paymentRequestRepository.save(new PaymentRequestEntity(rewardId, UUID.randomUUID(), githubUserId,
          new Date(), BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.strk));

      // When
      final RewardViewEntity userReward = customRewardRepository.findUserRewardViewEntityByd(rewardId);
      final RewardViewEntity projectReward = customRewardRepository.findProjectRewardViewEntityByd(rewardId);

      // Then
      Assertions.assertEquals("MISSING_PAYOUT_INFO", userReward.getStatus());
      Assertions.assertEquals("PROCESSING", projectReward.getStatus());
    }

    @Test
    @Order(2)
    void should_return_processing_for_an_individual() {
      // Given
      postgresUserAdapter.savePayoutInformationForUserId(userId,
          UserPayoutInformation.builder()
              .person(UserPayoutInformation.Person.builder()
                  .lastName(faker.name().lastName()).firstName(faker.name().firstName())
                  .build())
              .location(UserPayoutInformation.Location.builder()
                  .address(faker.address().fullAddress())
                  .city(faker.address().city())
                  .postalCode(faker.address()
                      .zipCode()).country(faker.address().country()).build())
              .payoutSettings(UserPayoutInformation.PayoutSettings.builder()
                  .starknetAddress(StarkNet.accountAddress("0x01"))
                  .build())
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
          UserPayoutInformation.builder()
              .isACompany(true)
              .company(UserPayoutInformation.Company.builder()
                  .owner(UserPayoutInformation.Person.builder()
                      .lastName(faker.name().lastName()).firstName(faker.name().firstName())
                      .build())
                  .identificationNumber(faker.number().digit())
                  .name(faker.name().name())
                  .build())
              .location(UserPayoutInformation.Location.builder()
                  .address(faker.address().fullAddress())
                  .city(faker.address().city())
                  .postalCode(faker.address()
                      .zipCode()).country(faker.address().country()).build())
              .build());
      paymentRequestRepository.save(new PaymentRequestEntity(rewardId, UUID.randomUUID(), githubUserId,
          new Date(), BigDecimal.ONE, null, 1, projectId, CurrencyEnumEntity.op));

      // When
      final RewardViewEntity userReward = customRewardRepository.findUserRewardViewEntityByd(rewardId);
      final RewardViewEntity projectReward = customRewardRepository.findProjectRewardViewEntityByd(rewardId);

      // Then
      Assertions.assertEquals("MISSING_PAYOUT_INFO", userReward.getStatus());
      Assertions.assertEquals("PROCESSING", projectReward.getStatus());
    }

    @Test
    @Order(2)
    void should_return_pending_invoice_status() {
      // Given
      postgresUserAdapter.savePayoutInformationForUserId(userId,
          UserPayoutInformation.builder()
              .company(UserPayoutInformation.Company.builder()
                  .owner(UserPayoutInformation.Person.builder()
                      .lastName(faker.name().lastName())
                      .firstName(faker.name().firstName())
                      .build())
                  .identificationNumber(faker.number().digit())
                  .name(faker.name().name())
                  .build())
              .isACompany(true)
              .location(UserPayoutInformation.Location.builder()
                  .address(faker.address().fullAddress())
                  .city(faker.address().city())
                  .postalCode(faker.address()
                      .zipCode()).country(faker.address().country()).build())
              .payoutSettings(UserPayoutInformation.PayoutSettings.builder()
                  .optimismAddress(Optimism.accountAddress("0x01"))
                  .build())
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
