package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "invoices", schema = "accounting")
@TypeDef(name = "invoice_status", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Data
@Accessors(chain = true, fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceEntity {
    @Id
    @NonNull UUID id;
    @NonNull UUID billingProfileId;
    @NonNull String number;
    @NonNull UUID createdBy;

    @NonNull ZonedDateTime createdAt;
    @Enumerated(EnumType.STRING)
    @Type(type = "invoice_status")
    @NonNull Status status;
    @NonNull BigDecimal amount;
    @NonNull UUID currencyId;
    URL url;
    String originalFileName;
    String rejectionReason;

    @Type(type = "jsonb")
    Data data;

    public Invoice toDomain() {
        return new Invoice(
                Invoice.Id.of(id),
                data.billingProfileSnapshot(),
                UserId.of(createdBy),
                createdAt,
                data.dueAt,
                Invoice.Number.fromString(number),
                status.toDomain(),
                data.rewards.stream().map(InvoiceRewardEntity::forInvoice).toList(),
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
                .status(Status.of(invoice.status()))
                .url(invoice.url())
                .amount(invoice.totalAfterTax().getValue())
                .currencyId(invoice.totalAfterTax().getCurrency().id().value())
                .originalFileName(invoice.originalFileName())
                .rejectionReason(invoice.rejectionReason())
                .data(Data.of(invoice));
    }

    public enum Status {
        DRAFT, TO_REVIEW, REJECTED, APPROVED, PAID;

        public static Status of(Invoice.Status status) {
            return switch (status) {
                case DRAFT -> DRAFT;
                case TO_REVIEW -> TO_REVIEW;
                case REJECTED -> REJECTED;
                case APPROVED -> APPROVED;
                case PAID -> PAID;
            };
        }

        public Invoice.Status toDomain() {
            return switch (this) {
                case DRAFT -> Invoice.Status.DRAFT;
                case TO_REVIEW -> Invoice.Status.TO_REVIEW;
                case REJECTED -> Invoice.Status.REJECTED;
                case APPROVED -> Invoice.Status.APPROVED;
                case PAID -> Invoice.Status.PAID;
            };
        }
    }

    public record Data(@NonNull ZonedDateTime dueAt,
                       @NonNull BigDecimal taxRate,
                       Invoice.BillingProfileSnapshot billingProfileSnapshot,
                       List<InvoiceRewardEntity> rewards
    ) implements Serializable {

        public static Data of(final @NonNull Invoice invoice) {
            return new Data(
                    invoice.dueAt(),
                    invoice.taxRate(),
                    invoice.billingProfileSnapshot(),
                    invoice.rewards().stream().map(InvoiceRewardEntity::of).toList()
            );
        }
    }
}
