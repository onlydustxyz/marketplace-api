package onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Entity
@TypeDef(name = "currency", typeClass = PostgreSQLEnumType.class)
public class ProjectBudgetEntity {
    @EmbeddedId
    Id id;
    @Enumerated(EnumType.STRING)
    @Type(type = "currency")
    @Column(name = "currency", nullable = false)
    CurrencyEnumEntity currency;
    BigDecimal initialAmount;
    BigDecimal remainingAmount;
    BigDecimal spentAmount;
    BigDecimal initialAmountDollarsEquivalent;
    BigDecimal remainingAmountDollarsEquivalent;
    BigDecimal spentAmountDollarsEquivalent;

    @Embeddable
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class Id implements Serializable {
        UUID id;
        UUID projectId;
    }
}
