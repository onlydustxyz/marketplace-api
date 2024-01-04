package onlydust.com.marketplace.api.postgres.adapter.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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
}
