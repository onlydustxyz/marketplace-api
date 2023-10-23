package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.math.BigDecimal;

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
