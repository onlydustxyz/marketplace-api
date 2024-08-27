package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.api.postgres.adapter.entity.json.InvoiceInnerData;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "invoices", schema = "accounting")
@Data
@Accessors(chain = true, fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceEntity {
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
    InvoiceInnerData data;

    public Invoice toDomain() {
        return new Invoice(
                Invoice.Id.of(id),
                data.billingProfileSnapshot(),
                UserId.of(createdBy),
                createdAt,
                data.dueAt(),
                Invoice.Number.fromString(number),
                status,
                data.rewards().stream().map(InvoiceRewardEntity::forInvoice).toList(),
                url,
                originalFileName,
                rejectionReason
        );
    }

    public static InvoiceEntity fromDomain(Invoice invoice) {
        final var invoiceEntity = new InvoiceEntity().id(invoice.id().value());
        invoiceEntity.updateWith(invoice);
        return invoiceEntity;
    }

    public void updateWith(Invoice invoice) {
        this
                .billingProfileId(invoice.billingProfileSnapshot().id().value())
                .number(invoice.number().toString())
                .createdBy(invoice.createdBy().value())
                .createdAt(invoice.createdAt())
                .status(invoice.status())
                .url(invoice.url())
                .amount(invoice.totalAfterTax().getValue())
                .currencyId(invoice.totalAfterTax().getCurrency().id().value())
                .originalFileName(invoice.originalFileName())
                .rejectionReason(invoice.rejectionReason())
                .data(InvoiceInnerData.of(invoice));
    }

}
