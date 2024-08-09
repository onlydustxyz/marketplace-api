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
    private ZonedDateTime timestamp;
    private SponsorAccount.Id sponsorAccountId;
    private ProjectId projectId;
    private RewardId rewardId;
    private Payment.Id paymentId;
    private Amount amount;
    private Currency.Id currencyId;

    public static AccountBookTransactionProjection of(final @NonNull ZonedDateTime timestamp,
                                                      final @NonNull Currency.Id currencyId,
                                                      final @NonNull AccountBook.Transaction transaction) {
        return new AccountBookTransactionProjection()
                .timestamp(timestamp)
                .amount(transaction.amount())
                .currencyId(currencyId)
                .with(transaction.path());
    }

    public static AccountBookTransactionProjection of(final @NonNull ZonedDateTime now,
                                                      final @NonNull Currency.Id currencyId,
                                                      final @NonNull AccountBook.AccountId accountId) {
        return new AccountBookTransactionProjection()
                .timestamp(now)
                .currencyId(currencyId)
                .with(accountId);
    }

    private AccountBookTransactionProjection with(final @NonNull List<AccountBook.AccountId> path) {
        path.forEach(this::with);
        return this;
    }

    private AccountBookTransactionProjection with(final @NonNull AccountBook.AccountId accountId) {
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
        
        return this;
    }
}
