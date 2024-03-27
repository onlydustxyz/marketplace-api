package onlydust.com.marketplace.accounting.domain.port.out;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.InvoiceView;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.SortDirection;

import java.util.List;
import java.util.Optional;

public interface InvoiceStoragePort {

    void create(final @NonNull Invoice invoice);

    void update(final @NonNull Invoice invoice);

    void deleteDraftsOf(final @NonNull BillingProfile.Id billingProfileId);

    Page<InvoiceView> invoicesOf(final @NonNull BillingProfile.Id billingProfileId, final @NonNull Integer pageNumber, final @NonNull Integer pageSize,
                                 final @NonNull Invoice.Sort sort, final @NonNull SortDirection direction);

    Optional<InvoiceView> getView(final @NonNull Invoice.Id invoiceId);

    Optional<Invoice> get(final @NonNull Invoice.Id invoiceId);

    Page<Invoice> findAll(@NonNull List<Invoice.Id> ids, @NonNull List<Invoice.Status> statuses, @NonNull List<Currency.Id> currencyIds,
                          @NonNull List<BillingProfile.Type> billingProfileTypes, String search, @NonNull Integer pageIndex, @NonNull Integer pageSize);

    Optional<Invoice> invoiceOf(RewardId rewardId);

    int getNextSequenceNumber(BillingProfile.Id billingProfileId);

    List<Invoice.Reward> findRewards(List<RewardId> rewardIds);

    List<Invoice> getAll(List<Invoice.Id> invoiceIds);
}
