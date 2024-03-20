package onlydust.com.marketplace.project.domain.model;

import onlydust.com.marketplace.project.domain.view.BillingProfileLinkView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

public class UserTest {

    @Test
    void should_return_company_billing_profile_where_i_am_admin() {
        // Given
        final User user = User.builder()
                .billingProfiles(List.of(
                        BillingProfileLinkView.builder()
                                .id(UUID.randomUUID())
                                .hasValidVerificationStatus(false)
                                .hasValidPayoutMethods(false)
                                .verificationStatus(BillingProfileLinkView.VerificationStatus.NOT_STARTED)
                                .role(BillingProfileLinkView.Role.ADMIN)
                                .type(BillingProfileLinkView.Type.INDIVIDUAL)
                                .build(),
                        BillingProfileLinkView.builder()
                                .id(UUID.randomUUID())
                                .hasValidVerificationStatus(false)
                                .hasValidPayoutMethods(false)
                                .verificationStatus(BillingProfileLinkView.VerificationStatus.NOT_STARTED)
                                .role(BillingProfileLinkView.Role.ADMIN)
                                .type(BillingProfileLinkView.Type.SELF_EMPLOYED)
                                .build(),
                        BillingProfileLinkView.builder()
                                .id(UUID.randomUUID())
                                .hasValidVerificationStatus(false)
                                .hasValidPayoutMethods(false)
                                .verificationStatus(BillingProfileLinkView.VerificationStatus.NOT_STARTED)
                                .role(BillingProfileLinkView.Role.ADMIN)
                                .type(BillingProfileLinkView.Type.COMPANY)
                                .build(),
                        BillingProfileLinkView.builder()
                                .id(UUID.randomUUID())
                                .hasValidVerificationStatus(false)
                                .hasValidPayoutMethods(false)
                                .verificationStatus(BillingProfileLinkView.VerificationStatus.NOT_STARTED)
                                .role(BillingProfileLinkView.Role.MEMBER)
                                .type(BillingProfileLinkView.Type.COMPANY)
                                .build()
                ))
                .build();

        // When
        final List<BillingProfileLinkView> companyAdminBillingProfile = user.getCompanyAdminBillingProfile();

        // Then
        Assertions.assertEquals(1, companyAdminBillingProfile.size());
        Assertions.assertEquals(BillingProfileLinkView.Role.ADMIN, companyAdminBillingProfile.get(0).role());
        Assertions.assertEquals(BillingProfileLinkView.Type.COMPANY, companyAdminBillingProfile.get(0).type());
    }
}
