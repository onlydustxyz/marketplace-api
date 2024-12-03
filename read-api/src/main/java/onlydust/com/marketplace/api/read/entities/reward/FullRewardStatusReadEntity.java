package onlydust.com.marketplace.api.read.entities.reward;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
import onlydust.com.marketplace.api.read.entities.billing_profile.BillingProfileReadEntity;
import onlydust.com.marketplace.api.read.entities.currency.CurrencyReadEntity;
import onlydust.com.marketplace.api.read.entities.project.ProjectLinkReadEntity;
import onlydust.com.marketplace.api.read.entities.user.AllUserReadEntity;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(name = "reward_statuses", schema = "accounting")
@NoArgsConstructor(force = true)
@Immutable
@Accessors(fluent = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class FullRewardStatusReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID rewardId;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "accounting.reward_status")
    @NonNull
    RewardStatus.Input status;

    @NonNull
    BigDecimal amount;
    @NonNull
    BigDecimal amountUsdEquivalent;
    @NonNull
    BigDecimal usdConversionRate;
    @NonNull
    ZonedDateTime requestedAt;
    @NonNull
    ZonedDateTime invoiceReceivedAt;
    @NonNull
    ZonedDateTime paidAt;

    @JdbcTypeCode(SqlTypes.ARRAY)
    NetworkEnumEntity[] networks;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "currencyId")
    @NonNull
    CurrencyReadEntity currency;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "projectId")
    @NonNull
    ProjectLinkReadEntity project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requestorId", referencedColumnName = "userId")
    @NonNull
    AllUserReadEntity requestor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipientId", referencedColumnName = "githubUserId", insertable = false, updatable = false)
    @NonNull
    AllUserReadEntity recipient;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoiceId")
    InvoiceReadEntity invoice;

    @OneToMany
    @JoinTable(schema = "accounting", name = "rewards_receipts",
            joinColumns = @JoinColumn(name = "reward_id"),
            inverseJoinColumns = @JoinColumn(name = "receipt_id"))
    List<ReceiptReadEntity> receipts;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billingProfileId")
    BillingProfileReadEntity billingProfile;

    @Formula("""
            (select jsonb_agg(coalesce(gpr.html_url, gcr.html_url, gi.html_url))
             from reward_items ri
                      left join indexer_exp.github_pull_requests gpr on gpr.contribution_uuid = ri.contribution_uuid
                      left join indexer_exp.github_code_reviews gcr on gcr.contribution_uuid = ri.contribution_uuid
                      left join indexer_exp.github_issues gi on gi.contribution_uuid = ri.contribution_uuid
             where ri.reward_id = reward_id
             group by ri.reward_id)
            """)
    @JdbcTypeCode(SqlTypes.JSON)
    Set<String> itemGithubUrls;

    @Formula("""
            (select jsonb_agg(distinct s.name)
             from sponsors s
                join accounting.all_transactions abt on abt.sponsor_id = s.id
             where abt.payment_id is null and abt.reward_id = reward_id
             group by abt.reward_id)
            """)
    @JdbcTypeCode(SqlTypes.JSON)
    Set<String> sponsorNames;

    @Formula("""
            (select jsonb_agg(distinct prog.name)
             from programs prog
                join accounting.all_transactions abt on abt.program_id = prog.id
             where abt.payment_id is null and abt.reward_id = reward_id
             group by abt.reward_id)
            """)
    @JdbcTypeCode(SqlTypes.JSON)
    Set<String> programNames;


    public List<String> transactionReferences() {
        return receipts == null ? List.of() : receipts.stream().map(ReceiptReadEntity::transactionReference).toList();
    }

    public List<String> paidToAccountNumbers() {
        return receipts == null ? List.of() : receipts.stream().map(ReceiptReadEntity::thirdPartyAccountNumber).toList();
    }

    public String prettyId() {
        return RewardId.of(rewardId).pretty();
    }
}
