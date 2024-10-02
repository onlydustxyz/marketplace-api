package onlydust.com.marketplace.accounting.domain.model.accountbook;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Deposit;
import onlydust.com.marketplace.accounting.domain.model.Payment;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.kernel.model.SponsorId;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Accessors(fluent = true, chain = true)
public class AccountingTransactionProjection {
    private @NonNull UUID id;
    private @NonNull ZonedDateTime timestamp;
    private @NonNull Currency.Id currencyId;
    private @NonNull Type type;
    private @NonNull SponsorId sponsorId;
    private ProgramId programId;
    private ProjectId projectId;
    private RewardId rewardId;
    private Payment.Id paymentId;
    private @NonNull PositiveAmount amount;
    private Deposit.Status depositStatus;

    public enum Type {DEPOSIT, MINT, BURN, TRANSFER, REFUND}

    public static AccountingTransactionProjection of(final @NonNull ZonedDateTime timestamp,
                                                     final @NonNull Currency.Id currencyId,
                                                     final @NonNull SponsorId sponsorId,
                                                     final @NonNull AccountBook.Transaction transaction) {
        return new AccountingTransactionProjection(UUID.randomUUID(),
                timestamp,
                currencyId,
                switch (transaction.type()) {
                    case MINT -> Type.MINT;
                    case BURN -> Type.BURN;
                    case TRANSFER -> Type.TRANSFER;
                    case REFUND -> Type.REFUND;
                },
                sponsorId,
                transaction.amount()
        ).with(transaction.path());
    }

    public static AccountingTransactionProjection of(final @NonNull Deposit deposit) {
        return new AccountingTransactionProjection(deposit.id().value(),
                deposit.transaction().timestamp(),
                deposit.currency().id(),
                Type.DEPOSIT,
                deposit.sponsorId(),
                PositiveAmount.of(deposit.transaction().amount())
        ).depositStatus(deposit.status());
    }

    public static AccountingTransactionProjection merge(final AccountingTransactionProjection left, final @NonNull AccountingTransactionProjection right) {
        return left == null ? right : left.amount(left.amount().add(right.amount()));
    }

    private AccountingTransactionProjection with(final @NonNull List<AccountBook.AccountId> path) {
        path.forEach(this::with);
        return this;
    }

    private void with(final @NonNull AccountBook.AccountId accountId) {
        if (accountId.type() != null)
            switch (accountId.type()) {
                case SPONSOR_ACCOUNT:
                    break;
                case PROGRAM:
                    programId(accountId.programId());
                    break;
                case PROJECT:
                    projectId(accountId.projectId());
                    break;
                case REWARD:
                    rewardId(accountId.rewardId());
                    break;
                case PAYMENT:
                    paymentId(accountId.paymentId());
                    break;
            }
    }
}
