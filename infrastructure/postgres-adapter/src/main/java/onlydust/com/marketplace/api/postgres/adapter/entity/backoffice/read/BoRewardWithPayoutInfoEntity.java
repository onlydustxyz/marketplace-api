package onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.view.RewardWithPayoutInfoView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.PayoutInfoEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardStatusDataEntity;

import javax.persistence.*;
import java.util.UUID;

import static java.util.Objects.isNull;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Entity
@Table(name = "rewards", schema = "public")
public class BoRewardWithPayoutInfoEntity {
    @Id
    UUID id;
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id", referencedColumnName = "rewardId", insertable = false, updatable = false)
    RewardStatusDataEntity rewardStatusData;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "invoice_id", referencedColumnName = "id", insertable = false, updatable = false)
    InvoiceEntity invoice;

    @Entity
    @Table(name = "invoices", schema = "accounting")
    @Data
    public static class InvoiceEntity {
        @Id
        UUID id;
        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "billingProfileId", referencedColumnName = "billingProfileId", insertable = false, updatable = false)
        PayoutInfoEntity payoutInfo;
    }

    public RewardWithPayoutInfoView toDomain() {
        return new RewardWithPayoutInfoView(
                RewardId.of(id),
                isNull(invoice) || isNull(invoice.payoutInfo) ? null : invoice.payoutInfo.toDomain(),
                isNull(rewardStatusData) ? null : rewardStatusData.usdConversionRate());
    }
}
