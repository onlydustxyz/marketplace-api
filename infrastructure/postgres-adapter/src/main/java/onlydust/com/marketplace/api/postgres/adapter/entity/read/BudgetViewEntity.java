package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import java.math.BigDecimal;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@Data
@TypeDef(name = "currency", typeClass = PostgreSQLEnumType.class)
@Entity
public class BudgetViewEntity {

  @Id
  @Column(name = "id")
  UUID id;
  @Column(name = "initial_amount")
  BigDecimal initialAmount;
  @Column(name = "remaining_amount")
  BigDecimal remainingAmount;
  @Enumerated(EnumType.STRING)
  @Type(type = "currency")
  @Column(name = "currency")
  CurrencyEnumEntity currency;
  @Column(name = "initial_amount_dollars_equivalent")
  BigDecimal initialAmountDollarsEquivalent;
  @Column(name = "remaining_amount_dollars_equivalent")
  BigDecimal remainingAmountDollarsEquivalent;
  @Column(name = "dollars_conversion_rate")
  BigDecimal dollarsConversionRate;


}
