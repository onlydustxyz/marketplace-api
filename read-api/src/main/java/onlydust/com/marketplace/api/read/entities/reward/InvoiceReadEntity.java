package onlydust.com.marketplace.api.read.entities.reward;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.InvoiceRewardEntity;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@NoArgsConstructor(force = true)
@Table(name = "invoices", schema = "accounting")
@Accessors(fluent = true)
@Immutable
public class InvoiceReadEntity {
    @Id
    @NonNull
    UUID id;
    @NonNull
    UUID billingProfileId;
    @NonNull
    String number;
    @NonNull
    UUID createdBy;

    @NonNull
    ZonedDateTime createdAt;
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "invoice_status")
    @NonNull
    Invoice.Status status;
    @NonNull
    BigDecimal amount;
    @NonNull
    UUID currencyId;
    URL url;
    String originalFileName;
    String rejectionReason;

    @JdbcTypeCode(SqlTypes.JSON)
    Data data;

    public record Data(@NonNull ZonedDateTime dueAt,
                       @NonNull BigDecimal taxRate,
                       Invoice.BillingProfileSnapshot billingProfileSnapshot,
                       List<InvoiceRewardEntity> rewards
    ) {
    }
}
