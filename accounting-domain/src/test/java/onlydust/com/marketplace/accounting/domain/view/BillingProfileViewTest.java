package onlydust.com.marketplace.accounting.domain.view;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

public class BillingProfileViewTest {

    @Test
    void should_prevent_npe_on_subject() {
        // Given
        final BillingProfileView build = BillingProfileView.builder().build();

        // When
        final String subject = build.subject();

        // Then
        assertNull(subject);
    }
}
