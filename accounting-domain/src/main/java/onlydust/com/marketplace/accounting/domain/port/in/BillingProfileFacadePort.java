package onlydust.com.marketplace.accounting.domain.port.in;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.UserId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.CompanyBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.IndividualBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.SelfEmployedBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.view.ShortBillingProfileView;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

public interface BillingProfileFacadePort {

    IndividualBillingProfile createIndividualBillingProfile(@NonNull UserId owner, @NonNull String name, Set<ProjectId> selectForProjects);

    SelfEmployedBillingProfile createSelfEmployedBillingProfile(@NonNull UserId owner, @NonNull String name, Set<ProjectId> selectForProjects);

    CompanyBillingProfile createCompanyBillingProfile(@NonNull UserId firstAdmin, @NonNull String name, Set<ProjectId> selectForProjects);

    Invoice previewInvoice(final @NonNull UserId userId, final @NonNull BillingProfile.Id billingProfileId, final @NonNull List<RewardId> rewardIds);

    Page<Invoice> invoicesOf(final @NonNull UserId userId, final @NonNull BillingProfile.Id billingProfileId, final @NonNull Integer pageNumber,
                             final @NonNull Integer pageSize);

    void uploadGeneratedInvoice(final @NonNull UserId userId, final @NonNull BillingProfile.Id billingProfileId, final @NonNull Invoice.Id invoiceId,
                                final @NonNull InputStream inputStream);

    void uploadExternalInvoice(final @NonNull UserId userId, final @NonNull BillingProfile.Id billingProfileId, final @NonNull Invoice.Id invoiceId,
                               final String fileName, final @NonNull InputStream inputStream);

    @NonNull InvoiceDownload downloadInvoice(final @NonNull UserId userId, final @NonNull BillingProfile.Id billingProfileId,
                                             final @NonNull Invoice.Id invoiceId);

    void updateInvoiceMandateAcceptanceDate(UserId userId, BillingProfile.Id billingProfileId);

    List<ShortBillingProfileView> getBillingProfilesForUser(UserId userId);
}
