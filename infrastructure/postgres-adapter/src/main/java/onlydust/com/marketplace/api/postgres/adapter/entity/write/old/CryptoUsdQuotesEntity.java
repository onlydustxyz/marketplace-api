package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "crypto_usd_quotes", schema = "public")
@TypeDef(name = "currency", typeClass = PostgreSQLEnumType.class)
public class CryptoUsdQuotesEntity {

  @Id
  @Column(name = "currency")
  @Type(type = "currency")
  @Enumerated(EnumType.STRING)
  CurrencyEnumEntity currency;
  @Column(name = "price", nullable = false)
  BigDecimal price;
  @Column(name = "updated_at", nullable = false)
  Date updatedAt;
}
