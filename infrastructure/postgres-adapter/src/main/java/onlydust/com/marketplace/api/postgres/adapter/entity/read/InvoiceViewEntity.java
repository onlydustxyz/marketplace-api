package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.InvoiceView;
import onlydust.com.marketplace.accounting.domain.model.Money;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.accounting.domain.view.UserView;
import onlydust.com.marketplace.api.postgres.adapter.entity.json.InvoiceInnerData;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "invoices", schema = "accounting")
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@NoArgsConstructor(force = true)
@Accessors(fluent = true)
@Immutable
public class InvoiceViewEntity {
    @Id
    @NonNull
    UUID id;
    @NonNull
    UUID billingProfileId;
    @NonNull
    String number;
    @NonNull
    ZonedDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "invoice_status")
    @NonNull
    Invoice.Status status;
    @NonNull
    BigDecimal amount;
    URL url;
    String originalFileName;
    String rejectionReason;

    @JdbcTypeCode(SqlTypes.JSON)
    @NonNull
    InvoiceInnerData data;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdBy")
    @NonNull
    UserViewEntity createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currencyId")
    @NonNull
    CurrencyViewEntity currency;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoiceId")
    @NonNull
    List<RewardViewEntity> rewards;

    @NonNull
    private UserView getCreatedBy() {
        return new UserView(
                createdBy.githubUserId(),
                createdBy.login(),
                URI.create(createdBy.avatarUrl()),
                createdBy.email(),
                UserId.of(createdBy.id()),
                createdBy.profile() == null ? createdBy.login() :
                        createdBy.profile().firstName() + " " + createdBy.profile().lastName());
    }

    public InvoiceView toView() {
        return new InvoiceView(
                Invoice.Id.of(id),
                data.billingProfileSnapshot(),
                getCreatedBy(),
                createdAt,
                Money.of(amount, currency.toDomain()),
                data.dueAt(),
                Invoice.Number.fromString(number),
                status,
                rewards.stream().map(RewardViewEntity::toShortView).toList(),
                url,
                originalFileName,
                rejectionReason
        );
    }
}
