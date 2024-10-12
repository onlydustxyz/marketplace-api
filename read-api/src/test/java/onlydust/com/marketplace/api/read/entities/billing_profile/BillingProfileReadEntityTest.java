package onlydust.com.marketplace.api.read.entities.billing_profile;

import onlydust.com.backoffice.api.contract.model.BillingProfileType;
import onlydust.com.marketplace.api.contract.model.BillingProfileCoworkerRole;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BillingProfileReadEntityTest {

    @Test
    void toShortResponse_with_admin_caller() {
        // Given
        final var entity = new BillingProfileReadEntity()
                .id(UUID.randomUUID())
                .type(BillingProfileType.COMPANY)
                .users(Set.of(
                        new AllBillingProfileUserReadEntity()
                                .githubUserId(123L)
                                .role(BillingProfileCoworkerRole.ADMIN)
                                .invitationAccepted(true)
                ))
                .stats(Set.of(new BillingProfileStatsReadEntity()
                        .invoiceableRewardCount(42))
                );

        // When
        final var dto = entity.toShortResponse(123L);

        // Then
        assertThat(dto.getRequestableRewardCount()).isEqualTo(42);
    }

    @Test
    void toShortResponse_with_invited_caller() {
        // Given
        final var entity = new BillingProfileReadEntity()
                .id(UUID.randomUUID())
                .type(BillingProfileType.COMPANY)
                .users(Set.of(
                        new AllBillingProfileUserReadEntity()
                                .githubUserId(123L)
                                .role(BillingProfileCoworkerRole.ADMIN)
                                .invitationAccepted(false)
                ))
                .stats(Set.of(new BillingProfileStatsReadEntity()
                        .invoiceableRewardCount(42))
                );

        // When
        final var dto = entity.toShortResponse(123L);

        // Then
        assertThat(dto.getRequestableRewardCount()).isEqualTo(0);
    }

    @Test
    void toShortResponse_with_member_caller() {
        // Given
        final var entity = new BillingProfileReadEntity()
                .id(UUID.randomUUID())
                .type(BillingProfileType.COMPANY)
                .users(Set.of(
                        new AllBillingProfileUserReadEntity()
                                .githubUserId(123L)
                                .role(BillingProfileCoworkerRole.MEMBER)
                                .invitationAccepted(true)
                ))
                .stats(Set.of(new BillingProfileStatsReadEntity()
                        .invoiceableRewardCount(42))
                );

        // When
        final var dto = entity.toShortResponse(123L);

        // Then
        assertThat(dto.getRequestableRewardCount()).isEqualTo(0);
    }
}