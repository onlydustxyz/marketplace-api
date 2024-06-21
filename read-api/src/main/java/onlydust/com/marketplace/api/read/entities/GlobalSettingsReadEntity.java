package onlydust.com.marketplace.api.read.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.time.ZonedDateTime;

@Entity
@Value
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "global_settings", schema = "public")
public class GlobalSettingsReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false)
    Integer id;

    @Column(name = "terms_and_conditions_latest_version_date", nullable = false)
    ZonedDateTime termsAndConditionsLatestVersionDate;

    @Column(name = "invoice_mandate_latest_version_date", nullable = false)
    ZonedDateTime invoiceMandateLatestVersionDate;
}
