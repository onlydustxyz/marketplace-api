package onlydust.com.marketplace.api.read.entities.accounting;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.backoffice.api.contract.model.DepositPageItemResponse;
import onlydust.com.backoffice.api.contract.model.DepositStatus;
import onlydust.com.marketplace.accounting.domain.model.Deposit;
import onlydust.com.marketplace.api.read.entities.currency.CurrencyReadEntity;
import onlydust.com.marketplace.api.read.entities.sponsor.SponsorReadEntity;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(name = "deposits", schema = "accounting")
@Immutable
@Accessors(fluent = true)
public class DepositReadEntity {
    @Id
    @NonNull
    UUID id;

    @NonNull
    @OneToOne(optional = false)
    TransferTransactionReadEntity transaction;

    @NonNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "sponsorId")
    SponsorReadEntity sponsor;

    @NonNull
    @ManyToOne(optional = false)
    CurrencyReadEntity currency;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    DepositStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    Deposit.BillingInformation billingInformation;

    public DepositPageItemResponse toBoPageItemResponse() {
        return new DepositPageItemResponse()
                .id(id)
                .sponsor(sponsor.toBoResponse())
                .transaction(transaction.toBoResponse())
                .currency(currency.toBoShortResponse())
                .status(status);
    }
}
