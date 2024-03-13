package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.Data;
import onlydust.com.marketplace.accounting.domain.model.BatchPayment;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.view.MoneyView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BatchPaymentEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NetworkEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.util.*;

@Entity
@TypeDef(name = "batch_payment_status", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "network", typeClass = PostgreSQLEnumType.class)
@Data
public class BatchPaymentDetailsViewEntity {

    @Id
    UUID id;
    String transactionHash;
    String csv;
    @Enumerated(EnumType.STRING)
    @Type(type = "network")
    NetworkEnumEntity network;
    @Type(type = "batch_payment_status")
    @Enumerated(EnumType.STRING)
    BatchPaymentEntity.Status status;
    Date techCreatedAt;
    @Type(type = "jsonb")
    List<MoneyView> moneys;
    @Type(type = "jsonb")
    List<UUID> rewardIds;

    public BatchPayment toDomain() {

        final Map<String, MoneyView> moneyMapToCurrency = new HashMap<>();
        for (MoneyView money : moneys) {
            final String currencyCode = money.currencyCode();
            if (moneyMapToCurrency.containsKey(currencyCode)) {
                final MoneyView moneyView = moneyMapToCurrency.get(currencyCode);
                moneyMapToCurrency.replace(currencyCode, moneyView.toBuilder()
                        .dollarsEquivalent(moneyView.dollarsEquivalent().add(money.dollarsEquivalent()))
                        .amount(moneyView.amount().add(money.amount()))
                        .build()
                );
            } else {
                moneyMapToCurrency.put(currencyCode, money);
            }
        }

        return BatchPayment.builder()
                .createdAt(techCreatedAt)
                .csv(this.csv)
                .id(BatchPayment.Id.of(this.id))
                .rewardIds(this.rewardIds.stream().map(RewardId::of).toList())
                .moneys(moneyMapToCurrency.values().stream().toList())
                .network(this.network.toNetwork())
                .transactionHash(this.transactionHash)
                .status(switch (this.status) {
                    case PAID -> BatchPayment.Status.PAID;
                    case TO_PAY -> BatchPayment.Status.TO_PAY;
                })
                .build();
    }
}
