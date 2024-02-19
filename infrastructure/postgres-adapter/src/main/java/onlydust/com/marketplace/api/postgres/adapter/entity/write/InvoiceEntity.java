package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.PaymentRequestEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "invoices", schema = "accounting")
@TypeDef(name = "invoice_status", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Data
@Accessors(chain = true, fluent = true)
@NoArgsConstructor
public class InvoiceEntity {
    @Id
    @NonNull UUID id;
    @NonNull UUID billingProfileId;
    @NonNull String name;
    @NonNull ZonedDateTime createdAt;
    @NonNull ZonedDateTime dueAt;
    @Enumerated(EnumType.STRING)
    @Type(type = "invoice_status")
    @NonNull Status status;
    @NonNull BigDecimal taxRate;
    URL url;
    @Type(type = "jsonb")
    Data data;

    @OneToMany(mappedBy = "invoice", fetch = FetchType.EAGER)
    @NonNull Set<PaymentRequestEntity> rewards;

    public Invoice toDomain() {
        return new Invoice(
                Invoice.Id.of(id),
                BillingProfile.Id.of(billingProfileId),
                createdAt,
                dueAt,
                Invoice.Name.fromString(name),
                status.toDomain(),
                taxRate,
                data.personalInfo,
                data.companyInfo,
                data.bankAccount,
                data.wallets,
                data.rewards.stream().map(InvoiceRewardEntity::forInvoice).toList(),
                url
        );
    }

    public enum Status {
        DRAFT, PROCESSING, REJECTED, APPROVED;

        public static Status of(Invoice.Status status) {
            return switch (status) {
                case DRAFT -> DRAFT;
                case PROCESSING -> PROCESSING;
                case REJECTED -> REJECTED;
                case APPROVED -> APPROVED;
            };
        }

        public Invoice.Status toDomain() {
            return switch (this) {
                case DRAFT -> Invoice.Status.DRAFT;
                case PROCESSING -> Invoice.Status.PROCESSING;
                case REJECTED -> Invoice.Status.REJECTED;
                case APPROVED -> Invoice.Status.APPROVED;
            };
        }
    }

    public static InvoiceEntity of(final @NonNull Invoice invoice) {
        return new InvoiceEntity().id(invoice.id().value())
                .billingProfileId(invoice.billingProfileId().value())
                .name(invoice.name().toString())
                .createdAt(invoice.createdAt())
                .dueAt(invoice.dueAt())
                .status(Status.of(invoice.status()))
                .taxRate(invoice.taxRate())
                .url(invoice.url())
                .data(Data.of(invoice));
    }

    public record Data(Invoice.PersonalInfo personalInfo, Invoice.CompanyInfo companyInfo, Invoice.BankAccount bankAccount,
                       List<Invoice.Wallet> wallets, List<InvoiceRewardEntity> rewards) implements Serializable {

        public static Data of(final @NonNull Invoice invoice) {
            return new Data(
                    invoice.personalInfo().orElse(null),
                    invoice.companyInfo().orElse(null),
                    invoice.bankAccount().orElse(null),
                    invoice.wallets(),
                    invoice.rewards().stream().map(InvoiceRewardEntity::of).toList()
            );
        }
    }
}
