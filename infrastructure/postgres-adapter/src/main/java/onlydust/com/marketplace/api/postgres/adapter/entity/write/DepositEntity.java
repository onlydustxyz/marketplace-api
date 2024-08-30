package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.accounting.domain.model.Deposit;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(name = "deposits", schema = "accounting")
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder(access = AccessLevel.PRIVATE)
@Accessors(fluent = true, chain = true)
public class DepositEntity {
    @Id
    UUID id;

    @NonNull
    @OneToOne(optional = false, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    TransferTransactionEntity transaction;

    @NonNull
    UUID sponsorId;

    @NonNull
    @ManyToOne(optional = false)
    CurrencyEntity currency;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    Deposit.Status status;

    @JdbcTypeCode(SqlTypes.JSON)
    Deposit.BillingInformation billingInformation;

    public static DepositEntity of(Deposit deposit) {
        return DepositEntity.builder()
                .id(deposit.id().value())
                .transaction(TransferTransactionEntity.of(deposit.transaction()))
                .sponsorId(deposit.sponsorId().value())
                .currency(CurrencyEntity.of(deposit.currency()))
                .status(deposit.status())
                .billingInformation(deposit.billingInformation())
                .build();
    }
}
