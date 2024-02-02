package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.Amount;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.NetworkEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
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
    @NonNull UUID id;

    @NonNull UUID ledgerId;

    @Enumerated(javax.persistence.EnumType.STRING)
    @Type(type = "network")
    @NonNull NetworkEnumEntity network;

    @NonNull BigDecimal amount;

    ZonedDateTime lockedUntil;

    public SponsorAccount.Transaction toTransaction() {
        return new SponsorAccount.Transaction(SponsorAccount.Transaction.Id.of(id), Amount.of(amount), network.toNetwork(), lockedUntil);
    }

    public static LedgerTransactionEntity of(SponsorAccount.Id ledgerId, SponsorAccount.Transaction transaction) {
        return LedgerTransactionEntity.builder()
                .id(transaction.id().value())
                .ledgerId(ledgerId.value())
                .amount(transaction.amount().getValue())
                .network(NetworkEnumEntity.of(transaction.network()))
                .lockedUntil(transaction.lockedUntil())
                .build();
    }
}

