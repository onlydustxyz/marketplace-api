package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

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
