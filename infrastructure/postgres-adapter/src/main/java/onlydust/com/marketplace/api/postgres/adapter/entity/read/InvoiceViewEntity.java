package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.InvoiceView;
import onlydust.com.marketplace.accounting.domain.model.Money;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.view.UserView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CurrencyEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.InvoiceEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "invoices", schema = "accounting")
@Value
@NoArgsConstructor(force = true)
@Accessors(fluent = true)
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
    @Type(PostgreSQLEnumType.class)
    @Column(columnDefinition = "invoice_status")
    @NonNull
    InvoiceEntity.Status status;
    @NonNull
    BigDecimal amount;
    URL url;
    String originalFileName;
    String rejectionReason;

    @JdbcTypeCode(SqlTypes.JSON)
    @NonNull
    InvoiceEntity.Data data;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdBy")
    @NonNull
    UserViewEntity createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currencyId")
    @NonNull
    CurrencyEntity currency;

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
                createdBy.githubEmail(),
                UserId.of(createdBy.id()),
                createdBy.profile() == null ? createdBy.login() :
                        createdBy.profile().getFirstName() + " " + createdBy.profile().getLastName());
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
                status.toDomain(),
                rewards.stream().map(RewardViewEntity::toShortView).toList(),
                url,
                originalFileName,
                rejectionReason
        );
    }
}
