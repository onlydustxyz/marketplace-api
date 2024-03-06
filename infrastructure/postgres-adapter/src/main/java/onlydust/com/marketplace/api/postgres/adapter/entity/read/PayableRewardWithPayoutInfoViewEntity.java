package onlydust.com.marketplace.api.postgres.adapter.entity.read;


import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.accounting.domain.view.MoneyView;
import onlydust.com.marketplace.accounting.domain.view.PayableRewardWithPayoutInfoView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Entity
@TypeDef(name = "currency", typeClass = PostgreSQLEnumType.class)
public class PayableRewardWithPayoutInfoViewEntity {

    @Id
    UUID rewardId;
    @Enumerated(EnumType.STRING)
    @Type(type = "currency")
    CurrencyEnumEntity currency;
    String starknetAddress;
    String ethereumAddress;
    BigDecimal dollarsEquivalent;
    BigDecimal amount;
    String currencyName;
    String currencyCode;
    String currencyLogoUrl;

    public PayableRewardWithPayoutInfoView toDomain() {
        return PayableRewardWithPayoutInfoView.builder()
                .id(this.rewardId)
                .wallet(switch (this.currency) {
                    case lords, usdc -> new Invoice.Wallet(Network.ETHEREUM, this.ethereumAddress);
                    case strk -> new Invoice.Wallet(Network.STARKNET, this.starknetAddress);
                    default -> null;
                })
                .money(MoneyView.builder()
                        .amount(this.amount)
                        .dollarsEquivalent(this.dollarsEquivalent)
                        .currencyLogoUrl(this.currencyLogoUrl)
                        .currencyName(this.currencyName)
                        .currencyCode(this.currencyCode)
                        .build())
                .build();
    }
}
