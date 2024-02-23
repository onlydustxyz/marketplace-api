package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.UserId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileView;
import onlydust.com.marketplace.accounting.domain.view.ShortBillingProfileView;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface BillingProfileStorage {
    boolean oldIsAdmin(UserId userId, BillingProfile.Id billingProfileId);

    void updateInvoiceMandateAcceptanceDate(BillingProfile.Id billingProfileId, ZonedDateTime now);

    void save(IndividualBillingProfile billingProfile);

    void save(SelfEmployedBillingProfile billingProfile);

    void save(CompanyBillingProfile billingProfile);

    void savePayoutPreference(BillingProfile.Id billingProfileId, UserId userId, ProjectId projectId);

    boolean isMandateAccepted(BillingProfile.Id billingProfileId);

    Optional<ShortBillingProfileView> findIndividualBillingProfileForUser(UserId ownerId);

    List<ShortBillingProfileView> findAllBillingProfilesForUser(UserId userId);

    boolean isUserMemberOf(BillingProfile.Id billingProfileId, UserId userId);

    boolean isAdmin(BillingProfile.Id billingProfileId, UserId userId);

    Optional<BillingProfileView> findById(BillingProfile.Id billingProfileId);

    Optional<PayoutInfo> findPayoutInfoByBillingProfile(BillingProfile.Id billingProfileId);

    void savePayoutInfoForBillingProfile(PayoutInfo payoutInfo, BillingProfile.Id billingProfileId);
}
