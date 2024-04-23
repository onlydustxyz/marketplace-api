package onlydust.com.marketplace.accounting.domain.port.out;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.HistoricalTransaction;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.SortDirection;

import java.util.List;

public interface SponsorAccountStorage extends SponsorAccountProvider {
    void save(SponsorAccount... sponsorAccounts);

    void delete(SponsorAccount.Id sponsorAccountId, SponsorAccount.Transaction.Id transactionId);

    List<SponsorAccount> getSponsorAccounts(SponsorId sponsorId);

    Page<HistoricalTransaction> transactionsOf(@NonNull SponsorId sponsorId,
                                               @NonNull HistoricalTransaction.Filters filters,
                                               @NonNull Integer pageIndex,
                                               @NonNull Integer pageSize,
                                               @NonNull HistoricalTransaction.Sort sort,
                                               @NonNull SortDirection direction);

    List<SponsorAccount> find(SponsorId sponsorId, Currency.Id currencyId);
}
