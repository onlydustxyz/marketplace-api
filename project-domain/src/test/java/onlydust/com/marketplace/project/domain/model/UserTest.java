package onlydust.com.marketplace.project.domain.model;

import onlydust.com.marketplace.project.domain.view.BillingProfileLinkView;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class UserTest {

    @Test
    void should_return_company_billing_profile_where_i_am_admin() {
        // Given
        final var billingProfiles = List.of(
                BillingProfileLinkView.builder()
                        .id(UUID.randomUUID())
                        .role(BillingProfileLinkView.Role.ADMIN)
                        .build(),
                BillingProfileLinkView.builder()
                        .id(UUID.randomUUID())
                        .role(BillingProfileLinkView.Role.ADMIN)
                        .build(),
                BillingProfileLinkView.builder()
                        .id(UUID.randomUUID())
                        .role(BillingProfileLinkView.Role.ADMIN)
                        .build(),
                BillingProfileLinkView.builder()
                        .id(UUID.randomUUID())
                        .role(BillingProfileLinkView.Role.MEMBER)
                        .build()
        );
        final User user = User.builder()
                .billingProfiles(billingProfiles)
                .build();

        // When
        final var administratedBillingProfiles = user.getAdministratedBillingProfiles();

        // Then
        assertThat(administratedBillingProfiles).hasSize(3);
        assertThat(administratedBillingProfiles).doesNotContain(billingProfiles.get(3).id());
    }
}
