package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

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
