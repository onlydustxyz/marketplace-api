package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.Amount;
import onlydust.com.marketplace.accounting.domain.model.Ledger;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.NetworkEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor(force = true)
@TypeDef(name = "network", typeClass = PostgreSQLEnumType.class)
@Value
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "ledger_transactions", schema = "accounting")
public class LedgerTransactionEntity {
    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    @NonNull BigInteger id;

    @NonNull UUID ledgerId;

    @Enumerated(javax.persistence.EnumType.STRING)
    @Type(type = "network")
    @NonNull NetworkEnumEntity network;

    @NonNull BigDecimal amount;

    ZonedDateTime lockedUntil;

    public Ledger.Transaction toTransaction() {
        return new Ledger.Transaction(Amount.of(amount), network.toNetwork(), lockedUntil);
    }

    public static LedgerTransactionEntity of(Ledger.Transaction transaction) {
        return LedgerTransactionEntity.builder()
                .amount(transaction.amount().getValue())
                .network(NetworkEnumEntity.of(transaction.network()))
                .lockedUntil(transaction.lockedUntil())
                .build();
    }
}

