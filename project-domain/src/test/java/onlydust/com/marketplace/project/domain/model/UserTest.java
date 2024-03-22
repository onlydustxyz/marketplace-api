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
                                .missingPayoutInfo(true)
                                .missingVerification(true)
                                .verificationStatus(BillingProfileLinkView.VerificationStatus.NOT_STARTED)
                                .role(BillingProfileLinkView.Role.ADMIN)
                                .type(BillingProfileLinkView.Type.INDIVIDUAL)
                                .build(),
                        BillingProfileLinkView.builder()
                                .id(UUID.randomUUID())
                                .missingPayoutInfo(false)
                                .missingVerification(true)
                                .verificationStatus(BillingProfileLinkView.VerificationStatus.NOT_STARTED)
                                .role(BillingProfileLinkView.Role.ADMIN)
                                .type(BillingProfileLinkView.Type.SELF_EMPLOYED)
                                .build(),
                        BillingProfileLinkView.builder()
                                .id(UUID.randomUUID())
                                .missingPayoutInfo(true)
                                .missingVerification(false)
                                .verificationStatus(BillingProfileLinkView.VerificationStatus.NOT_STARTED)
                                .role(BillingProfileLinkView.Role.ADMIN)
                                .type(BillingProfileLinkView.Type.COMPANY)
                                .build(),
                        BillingProfileLinkView.builder()
                                .id(UUID.randomUUID())
                                .missingPayoutInfo(false)
                                .missingVerification(false)
                                .verificationStatus(BillingProfileLinkView.VerificationStatus.NOT_STARTED)
                                .role(BillingProfileLinkView.Role.MEMBER)
                                .type(BillingProfileLinkView.Type.COMPANY)
                                .build()
                ))
                .build();

        // When
        final List<BillingProfileLinkView> administratedBillingProfile = user.getAdministratedBillingProfile();

        // Then
        Assertions.assertEquals(3, administratedBillingProfile.size());
        Assertions.assertEquals(BillingProfileLinkView.Role.ADMIN, administratedBillingProfile.get(0).role());
    }
}
