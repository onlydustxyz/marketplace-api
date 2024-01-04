package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "payments", schema = "public")
public class PaymentEntity {

  @Id
  @Column(name = "id", nullable = false)
  UUID id;
  @Column(name = "amount", nullable = false)
  BigDecimal amount;
  @Column(name = "currency_code", nullable = false)
  String currencyCode;
  @Column(name = "receipt", nullable = false, columnDefinition = "jsonb")
  @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
  JsonNode receipt;
  @Column(name = "request_id", nullable = false)
  UUID requestId;
  @Column(name = "processed_at", nullable = false)
  Date processedAt;

}
