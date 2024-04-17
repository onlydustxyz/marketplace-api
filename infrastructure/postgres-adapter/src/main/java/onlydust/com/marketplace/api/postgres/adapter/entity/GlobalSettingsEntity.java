package onlydust.com.marketplace.api.postgres.adapter.entity;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder(toBuilder = true)
@Table(name = "global_settings", schema = "public")
public class GlobalSettingsEntity {
    @Id
    @Column(name = "id", nullable = false)
    Integer id;

    @Column(name = "terms_and_conditions_latest_version_date", nullable = false)
    private Date termsAndConditionsLatestVersionDate;

    @Column(name = "invoice_mandate_latest_version_date", nullable = false)
    private Date invoiceMandateLatestVersionDate;

    public ZonedDateTime getInvoiceMandateLatestVersionDate() {
        return invoiceMandateLatestVersionDate.toInstant().atZone(ZoneOffset.UTC);
    }
}
