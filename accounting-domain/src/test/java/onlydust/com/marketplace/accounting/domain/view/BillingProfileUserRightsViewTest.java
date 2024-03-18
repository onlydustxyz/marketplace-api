package onlydust.com.marketplace.accounting.domain.view;

import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BillingProfileUserRightsViewTest {

    @Test
    void should_return_can_delete() {
        assertEquals(true,
                BillingProfileUserRightsView.builder().role(BillingProfile.User.Role.ADMIN).hasBillingProfileSomeInvoices(false).build().canDelete());
        assertEquals(false,
                BillingProfileUserRightsView.builder().role(BillingProfile.User.Role.MEMBER).hasBillingProfileSomeInvoices(false).build().canDelete());
        assertEquals(false,
                BillingProfileUserRightsView.builder().role(BillingProfile.User.Role.ADMIN).hasBillingProfileSomeInvoices(true).build().canDelete());
    }

    @Test
    void should_return_can_leave() {
        assertEquals(false,
                BillingProfileUserRightsView.builder().role(BillingProfile.User.Role.ADMIN).hasUserSomeRewardsIncludedInInvoicesOnBillingProfile(false).build().
                        canLeave());
        assertEquals(true,
                BillingProfileUserRightsView.builder().role(BillingProfile.User.Role.MEMBER).hasUserSomeRewardsIncludedInInvoicesOnBillingProfile(false).build()
                        .canLeave());
        assertEquals(false,
                BillingProfileUserRightsView.builder().role(BillingProfile.User.Role.MEMBER).hasUserSomeRewardsIncludedInInvoicesOnBillingProfile(true).build()
                        .canLeave());
    }
}
