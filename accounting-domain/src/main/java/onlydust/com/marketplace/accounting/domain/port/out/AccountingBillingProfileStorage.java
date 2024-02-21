package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.UserId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.CompanyBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.IndividualBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.SelfEmployedBillingProfile;

import java.time.ZonedDateTime;

public interface AccountingBillingProfileStorage {
    boolean isAdmin(UserId userId, BillingProfile.Id billingProfileId);

    void updateInvoiceMandateAcceptanceDate(BillingProfile.Id billingProfileId, ZonedDateTime now);

    void save(IndividualBillingProfile billingProfile);

    void save(SelfEmployedBillingProfile billingProfile);

    void save(CompanyBillingProfile billingProfile);

    void savePayoutPreference(BillingProfile.Id billingProfileId, UserId userId, ProjectId projectId);
}
