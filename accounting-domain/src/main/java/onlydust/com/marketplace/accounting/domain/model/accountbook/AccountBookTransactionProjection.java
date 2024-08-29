package onlydust.com.marketplace.accounting.domain.model.accountbook;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Payment;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.kernel.model.SponsorId;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Accessors(fluent = true, chain = true)
public class AccountBookTransactionProjection {
    private @NonNull ZonedDateTime timestamp;
    private @NonNull Currency.Id currencyId;
    private @NonNull AccountBook.Transaction.Type type;
    private @NonNull SponsorId sponsorId;
    private ProgramId programId;
    private ProjectId projectId;
    private RewardId rewardId;
    private Payment.Id paymentId;
    private @NonNull PositiveAmount amount;

    public static AccountBookTransactionProjection of(final @NonNull ZonedDateTime timestamp,
                                                      final @NonNull Currency.Id currencyId,
                                                      final @NonNull SponsorId sponsorId,
                                                      final @NonNull AccountBook.Transaction transaction) {
        return new AccountBookTransactionProjection(timestamp, currencyId, transaction.type(), sponsorId, transaction.amount())
                .with(transaction.path());
    }

    public static AccountBookTransactionProjection merge(final AccountBookTransactionProjection left, final @NonNull AccountBookTransactionProjection right) {
        return left == null ? right : left.amount(left.amount().add(right.amount()));
    }

    private AccountBookTransactionProjection with(final @NonNull List<AccountBook.AccountId> path) {
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
