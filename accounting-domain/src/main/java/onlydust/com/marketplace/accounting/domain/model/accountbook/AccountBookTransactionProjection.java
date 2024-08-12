package onlydust.com.marketplace.accounting.domain.model.accountbook;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.*;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Accessors(fluent = true, chain = true)
public class AccountBookTransactionProjection {
    private @NonNull ZonedDateTime timestamp;
    private @NonNull AccountBookAggregate.Id accountBookId;
    private @NonNull AccountBook.Transaction.Type type;
    private SponsorAccount.Id sponsorAccountId;
    private ProjectId projectId;
    private RewardId rewardId;
    private Payment.Id paymentId;
    private @NonNull PositiveAmount amount;

    public static AccountBookTransactionProjection of(final @NonNull ZonedDateTime timestamp,
                                                      final @NonNull AccountBookAggregate.Id accountBookId,
                                                      final @NonNull AccountBook.Transaction transaction) {
        return new AccountBookTransactionProjection(timestamp, accountBookId, transaction.type(), transaction.amount())
                .with(transaction.path());
    }

    private AccountBookTransactionProjection with(final @NonNull List<AccountBook.AccountId> path) {
        path.forEach(this::with);
        return this;
    }

    private void with(final @NonNull AccountBook.AccountId accountId) {
        if (accountId.type() != null)
            switch (accountId.type()) {
                case SPONSOR_ACCOUNT:
                    sponsorAccountId(accountId.sponsorAccountId());
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
