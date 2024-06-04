package onlydust.com.marketplace.api.postgres.adapter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.ZonedDateTime;

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
    private ZonedDateTime termsAndConditionsLatestVersionDate;

    @Column(name = "invoice_mandate_latest_version_date", nullable = false)
    private ZonedDateTime invoiceMandateLatestVersionDate;
}
