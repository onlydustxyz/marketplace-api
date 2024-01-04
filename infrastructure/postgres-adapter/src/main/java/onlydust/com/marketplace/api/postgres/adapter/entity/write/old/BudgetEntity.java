package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import java.math.BigDecimal;
import java.util.UUID;
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
@Table(name = "budgets", schema = "public")
@TypeDef(name = "currency", typeClass = PostgreSQLEnumType.class)
public class BudgetEntity {

  @Id
  @Column(name = "id", nullable = false)
  UUID id;
  @Column(name = "initial_amount", nullable = false)
  BigDecimal initialAmount;
  @Column(name = "remaining_amount", nullable = false)
  BigDecimal remainingAmount;
  @Enumerated(EnumType.STRING)
  @Type(type = "currency")
  @Column(name = "currency", nullable = false)
  CurrencyEnumEntity currency;

}
