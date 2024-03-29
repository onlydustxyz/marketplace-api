package onlydust.com.marketplace.api.postgres.adapter.entity.read;

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
import org.hibernate.annotations.Type;

import javax.persistence.*;
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
    @NonNull UUID id;
    @NonNull UUID billingProfileId;
    @NonNull String number;
    @NonNull ZonedDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Type(type = "invoice_status")
    @NonNull InvoiceEntity.Status status;
    @NonNull BigDecimal amount;
    URL url;
    String originalFileName;
    String rejectionReason;

    @Type(type = "jsonb")
    @NonNull InvoiceEntity.Data data;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdBy")
    @NonNull UserViewEntity createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currencyId")
    @NonNull CurrencyEntity currency;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoiceId")
    @NonNull List<RewardViewEntity> rewards;

    @NonNull
    private UserView getCreatedBy() {
        return new UserView(
                createdBy.getGithubUserId(),
                createdBy.getGithubLogin(),
                URI.create(createdBy.getGithubAvatarUrl()),
                createdBy.getGithubEmail(),
                UserId.of(createdBy.getId()),
                createdBy.getProfile() == null ? createdBy.getGithubLogin() :
                        createdBy.getProfile().getFirstName() + " " + createdBy.getProfile().getLastName());
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
