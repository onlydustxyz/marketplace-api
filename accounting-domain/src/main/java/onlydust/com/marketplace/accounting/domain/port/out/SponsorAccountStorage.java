package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.HistoricalTransaction;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.util.List;

public interface SponsorAccountStorage extends SponsorAccountProvider {
    void save(SponsorAccount... sponsorAccounts);

    void delete(SponsorAccount.Id sponsorAccountId, SponsorAccount.Transaction.Id transactionId);

    List<SponsorAccount> getSponsorAccounts(SponsorId sponsorId);

    Page<HistoricalTransaction> transactionsOf(SponsorId sponsorId, List<HistoricalTransaction.Type> types, Integer pageIndex, Integer pageSize);
}
