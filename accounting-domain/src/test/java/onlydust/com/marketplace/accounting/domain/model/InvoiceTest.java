package onlydust.com.marketplace.accounting.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceTest {
    @Test
    void should_compute_id() {
        assertThat(Invoice.Id.of(1, "Doe", "John").value()).isEqualTo("OD-DOE-JOHN-001");
        assertThat(Invoice.Id.of(2, "Doe", "John").value()).isEqualTo("OD-DOE-JOHN-002");
        assertThat(Invoice.Id.of(1, "A peu près", "Jean-Michel").value()).isEqualTo("OD-APEUPRES-JEANMICHEL-001");
        assertThat(Invoice.Id.of(1, "OnlyDust").value()).isEqualTo("OD-ONLYDUST-001");
        assertThat(Invoice.Id.of(123456, "OnlyDust").value()).isEqualTo("OD-ONLYDUST-123456");
        assertThat(Invoice.Id.of(1, "Caisse d'Épargne").value()).isEqualTo("OD-CAISSEDEPARGNE-001");
    }
}