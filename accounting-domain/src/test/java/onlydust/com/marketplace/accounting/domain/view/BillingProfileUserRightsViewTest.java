package onlydust.com.marketplace.accounting.domain.view;

import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BillingProfileUserRightsViewTest {

    @Test
    void should_return_can_delete() {
        assertEquals(true,
                BillingProfileUserRightsView.builder().role(BillingProfile.User.Role.ADMIN).billingProfileProcessingRewardsCount(0L).build().canDelete());
        assertEquals(false,
                BillingProfileUserRightsView.builder().role(BillingProfile.User.Role.MEMBER).billingProfileProcessingRewardsCount(0L).build().canDelete());
        assertEquals(false,
                BillingProfileUserRightsView.builder().role(BillingProfile.User.Role.ADMIN).billingProfileProcessingRewardsCount(1L).build().canDelete());
    }

    @Test
    void should_return_can_leave() {
        assertEquals(false,
                BillingProfileUserRightsView.builder().role(BillingProfile.User.Role.ADMIN).userProcessingRewardsCount(0L).build().
                        canLeave());
        assertEquals(true,
                BillingProfileUserRightsView.builder().role(BillingProfile.User.Role.MEMBER).userProcessingRewardsCount(0L).build()
                        .canLeave());
        assertEquals(false,
                BillingProfileUserRightsView.builder().role(BillingProfile.User.Role.MEMBER).userProcessingRewardsCount(1L).build()
                        .canLeave());
    }
}
