package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import java.math.BigDecimal;
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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@TypeDef(name = "currency", typeClass = PostgreSQLEnumType.class)
@Entity
public class UserRewardTotalAmountEntity {

  @Id
  @Column(name = "id")
  Long id;
  @Column(name = "total")
  BigDecimal total;
  @Enumerated(EnumType.STRING)
  @Type(type = "currency")
  @Column(name = "currency")
  CurrencyEnumEntity currency;
  @Column(name = "dollars_equivalent")
  BigDecimal dollarsEquivalent;
}
