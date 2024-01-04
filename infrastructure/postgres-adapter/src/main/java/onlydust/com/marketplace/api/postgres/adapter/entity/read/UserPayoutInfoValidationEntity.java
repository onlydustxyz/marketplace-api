package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.vladmihalcea.hibernate.type.array.EnumArrayType;
import com.vladmihalcea.hibernate.type.array.internal.AbstractArrayType;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Entity
@TypeDef(
    name = "currency[]",
    typeClass = EnumArrayType.class,
    defaultForType = CurrencyEnumEntity[].class,
    parameters = {
        @Parameter(
            name = AbstractArrayType.SQL_ARRAY_TYPE,
            value = "public.currency"
        )
    }
)
public class UserPayoutInfoValidationEntity {

  @Id
  @Column(name = "user_id")
  UUID userId;

  @Type(type = "currency[]")
  @Column(name = "payment_requests_currencies", nullable = false, columnDefinition = "public.currency[]")
  CurrencyEnumEntity[] paymentRequestsCurrencies;
}
