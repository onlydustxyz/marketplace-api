package onlydust.com.marketplace.cli;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook;
import onlydust.com.marketplace.accounting.domain.service.CachedAccountBookProvider;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ReceiptEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.SponsorAccountEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.SponsorAccountTransactionsEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.SponsorAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static onlydust.com.marketplace.api.postgres.adapter.entity.write.SponsorAccountTransactionsEntity.TransactionType.SPEND;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
@Slf4j
@Profile("cli")
public class PennylaneMigrationRecovery implements CommandLineRunner {
    private final RewardRepository rewardRepository;
    private final SponsorAccountRepository sponsorAccountRepository;
    private final CachedAccountBookProvider accountBookProvider;
    private final static StopWatch stopWatch = new StopWatch();

    @Override
    @Transactional
    public void run(String... args) {
        if (args.length == 0 || !args[0].equals("pennylane_migration_recovery")) return;

        clearSpendTransactions();
        reprocessRewards();

        LOGGER.info(stopWatch.prettyPrint(TimeUnit.SECONDS));
    }

    private void reprocessRewards() {
        stopWatch.start("Reprocessing paid rewards");
        rewardRepository.findAllComplete().forEach(this::reprocess);
        stopWatch.stop();
    }

    private void reprocess(final RewardEntity reward) {
        LOGGER.info("Reprocessing reward {}", reward.id());

        if (reward.receipts().size() != 1)
            throw internalServerError("Reward %s has not exactly one receipt".formatted(reward.id()));

        final var accountBookState = accountBookProvider.get(reward.currency().toDomain()).state();

        final var receipt = reward.receipts().stream().findFirst().get();

        accountBookState.transferredAmountPerOrigin(AccountBook.AccountId.of(RewardId.of(reward.id())))
                .forEach((accountId, amount) -> {
                    final var sponsorAccountId = accountId.sponsorAccountId();
                    final var sponsorAccount = sponsorAccount(sponsorAccountId);

                    if (networkOf(sponsorAccount).equals(receipt.network()))
                        sponsorAccount.getTransactions().add(createSpendTransaction(sponsorAccountId, receipt, amount));
                });
    }

    private SponsorAccountTransactionsEntity createSpendTransaction(final SponsorAccount.Id accountId,
                                                                    final ReceiptEntity receipt,
                                                                    final PositiveAmount amount) {
        return new SponsorAccountTransactionsEntity(
                UUID.randomUUID(),
                accountId.value(),
                receipt.createdAt().toInstant().atZone(ZoneOffset.UTC),
                SPEND,
                receipt.network(),
                receipt.transactionReference(),
                amount.getValue(),
                receipt.thirdPartyName(),
                receipt.thirdPartyAccountNumber());
    }

    private NetworkEnumEntity networkOf(SponsorAccountEntity sponsorAccount) {
        return sponsorAccount.getTransactions().stream().map(SponsorAccountTransactionsEntity::getNetwork).findFirst().get();
    }

    private SponsorAccountEntity sponsorAccount(SponsorAccount.Id id) {
        return sponsorAccountRepository.findById(id.value())
                .orElseThrow(() -> notFound("Sponsor account not found: " + id));
    }

    private void clearSpendTransactions() {
        stopWatch.start("Clear spend transactions");
        LOGGER.info("Clearing spend transactions");
        sponsorAccountRepository.findAll()
                .forEach(sponsorAccount -> sponsorAccount.getTransactions().removeIf(t -> t.getType() == SPEND));
        stopWatch.stop();
    }
}
