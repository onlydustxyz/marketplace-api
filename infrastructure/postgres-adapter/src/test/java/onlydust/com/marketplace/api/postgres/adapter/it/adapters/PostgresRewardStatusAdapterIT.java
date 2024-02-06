package onlydust.com.marketplace.api.postgres.adapter.it.adapters;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.RewardStatus;
import onlydust.com.marketplace.api.domain.model.Reward;
import onlydust.com.marketplace.api.domain.model.UserRole;
import onlydust.com.marketplace.api.postgres.adapter.PostgresRewardStatusAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CurrencyEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectIdEntity;
import onlydust.com.marketplace.api.postgres.adapter.it.AbstractPostgresIT;
import onlydust.com.marketplace.api.postgres.adapter.repository.CurrencyRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectIdRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresRewardStatusAdapterIT extends AbstractPostgresIT {

    @Autowired
    RewardRepository rewardRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ProjectIdRepository projectIdRepository;
    @Autowired
    CurrencyRepository currencyRepository;
    @Autowired
    PostgresRewardStatusAdapter postgresRewardStatusAdapter;


    @Test
    void should_save_and_get_reward_status() {
        // Given
        final var eth = Currency.crypto("Ether", Currency.Code.of("ETH"), 18);
        currencyRepository.save(CurrencyEntity.of(eth));

        final var rewardId = createReward();

        final var rewardStatus = new RewardStatus(rewardId)
                .rewardCurrency(eth)
                .isIndividual(true)
                .kycbVerified(true)
                .usRecipient(true)
                .currentYearUsdTotal(PositiveAmount.of(1000L))
                .payoutInfoFilled(true)
                .sponsorHasEnoughFund(true)
                .unlockDate(ZonedDateTime.now())
                .paymentRequested(true)
                .invoiceApproved(true)
                .paid(true);

        // When
        postgresRewardStatusAdapter.save(rewardStatus);
        final var persistedRewardStatus = postgresRewardStatusAdapter.get(rewardStatus.rewardId()).orElseThrow();

        // Then
        assertThat(persistedRewardStatus.rewardId()).isEqualTo(rewardStatus.rewardId());
        assertThat(persistedRewardStatus.rewardCurrency()).isEqualTo(rewardStatus.rewardCurrency());
        assertThat(persistedRewardStatus.isIndividual()).isEqualTo(rewardStatus.isIndividual());
        assertThat(persistedRewardStatus.kycbVerified()).isEqualTo(rewardStatus.kycbVerified());
        assertThat(persistedRewardStatus.usRecipient()).isEqualTo(rewardStatus.usRecipient());
        assertThat(persistedRewardStatus.currentYearUsdTotal()).isEqualTo(rewardStatus.currentYearUsdTotal());
        assertThat(persistedRewardStatus.payoutInfoFilled()).isEqualTo(rewardStatus.payoutInfoFilled());
        assertThat(persistedRewardStatus.sponsorHasEnoughFund()).isEqualTo(rewardStatus.sponsorHasEnoughFund());
        assertThat(persistedRewardStatus.unlockDate()).isEqualToIgnoringSeconds(rewardStatus.unlockDate());
        assertThat(persistedRewardStatus.paymentRequested()).isEqualTo(rewardStatus.paymentRequested());
        assertThat(persistedRewardStatus.invoiceApproved()).isEqualTo(rewardStatus.invoiceApproved());
        assertThat(persistedRewardStatus.paid()).isEqualTo(rewardStatus.paid());
    }

    private RewardId createReward() {
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

        final RewardEntity reward = RewardEntity.of(new Reward(
                UUID.randomUUID(),
                projectId,
                userId,
                faker.number().randomNumber(),
                BigDecimal.valueOf(faker.number().randomNumber()),
                onlydust.com.marketplace.api.domain.model.Currency.Usdc,
                new Date(),
                null,
                List.of(Reward.Item.builder()
                        .id(faker.pokemon().name())
                        .number(faker.number().randomNumber())
                        .repoId(faker.number().randomNumber())
                        .type(Reward.Item.Type.PULL_REQUEST)
                        .build()
                )
        ));

        rewardRepository.save(reward);

        return RewardId.of(reward.getId());
    }
}