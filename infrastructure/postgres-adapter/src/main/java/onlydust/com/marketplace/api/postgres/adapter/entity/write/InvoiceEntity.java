package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
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
    @NonNull ZonedDateTime createdAt;
    @Enumerated(EnumType.STRING)
    @Type(type = "invoice_status")
    @NonNull Status status;
    @NonNull BigDecimal amount;
    @ManyToOne
    @NonNull CurrencyEntity currency;
    URL url;
    String originalFileName;
    @Type(type = "jsonb")
    Data data;
    String rejectionReason;

    public Invoice toDomain() {
        return new Invoice(
                Invoice.Id.of(id),
                BillingProfile.Id.of(billingProfileId),
                createdAt,
                data.dueAt,
                Invoice.Number.fromString(number),
                status.toDomain(),
                data.taxRate,
                data.personalInfo,
                data.companyInfo,
                data.bankAccount,
                data.wallets,
                data.rewards.stream().map(InvoiceRewardEntity::forInvoice).toList(),
                url,
                originalFileName,
                rejectionReason
        );
    }

    public void updateWith(Invoice invoice) {
        this
                .billingProfileId(invoice.billingProfileId().value())
                .number(invoice.number().toString())
                .createdAt(invoice.createdAt())
                .status(Status.of(invoice.status()))
                .url(invoice.url())
                .amount(invoice.totalAfterTax().getValue())
                .currency(CurrencyEntity.of(invoice.totalAfterTax().getCurrency()))
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
                       Invoice.PersonalInfo personalInfo,
                       Invoice.CompanyInfo companyInfo,
                       Invoice.BankAccount bankAccount,
                       @NonNull List<Invoice.Wallet> wallets,
                       List<InvoiceRewardEntity> rewards
    ) implements Serializable {

        public static Data of(final @NonNull Invoice invoice) {
            return new Data(
                    invoice.dueAt(),
                    invoice.taxRate(),
                    invoice.personalInfo().orElse(null),
                    invoice.companyInfo().orElse(null),
                    invoice.bankAccount().orElse(null),
                    invoice.wallets(),
                    invoice.rewards().stream().map(InvoiceRewardEntity::of).toList()
            );
        }
    }
}
