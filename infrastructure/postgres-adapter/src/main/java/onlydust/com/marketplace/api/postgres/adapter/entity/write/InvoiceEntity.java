package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.Money;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.PaymentRequestEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.math.BigDecimal;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "invoices", schema = "accounting")
@TypeDef(name = "invoice_status", typeClass = PostgreSQLEnumType.class)
@Data
@Accessors(chain = true, fluent = true)
@NoArgsConstructor
public class InvoiceEntity {
    @Id
    @NonNull UUID id;
    @NonNull UUID billingProfileId;
    @NonNull String name;
    @ManyToOne
    @NonNull CurrencyEntity currency;
    @NonNull BigDecimal totalAmount;
    @NonNull ZonedDateTime createdAt;
    @Enumerated(EnumType.STRING)
    @Type(type = "invoice_status")
    @NonNull Status status;
    @OneToMany(mappedBy = "invoice", fetch = FetchType.EAGER)
    @NonNull Set<PaymentRequestEntity> rewards;
    URL url;

    public Invoice toDomain() {
        return new Invoice(
                Invoice.Id.of(id),
                BillingProfile.Id.of(billingProfileId),
                Invoice.Name.fromString(name),
                createdAt,
                Money.of(totalAmount, currency.toDomain()),
                status.toDomain(),
                rewards.stream().map(r -> RewardId.of(r.getId())).collect(Collectors.toUnmodifiableSet()),
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
                .name(invoice.name().value())
                .currency(CurrencyEntity.of(invoice.totalAfterTax().getCurrency()))
                .totalAmount(invoice.totalAfterTax().getValue())
                .createdAt(invoice.createdAt())
                .status(Status.of(invoice.status()));
    }
}
