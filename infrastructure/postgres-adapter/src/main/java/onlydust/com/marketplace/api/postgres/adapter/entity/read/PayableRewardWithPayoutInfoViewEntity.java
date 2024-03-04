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
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
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
    String starknetWalletAddress;
    String ethereumWalletAddress;
    BigDecimal dollarsEquivalent;
    BigDecimal amount;
    String currencyName;
    String currencyCode;
    String currencyLogoUrl;

    public PayableRewardWithPayoutInfoView toDomain() {
        return PayableRewardWithPayoutInfoView.builder()
                .id(this.rewardId)
                .wallet(switch (this.currency) {
                    case eth -> new Invoice.Wallet(Network.ETHEREUM, this.ethereumWalletAddress);
                    case strk -> new Invoice.Wallet(Network.STARKNET, this.starknetWalletAddress);
                    case lords -> new Invoice.Wallet(Network.ETHEREUM, this.ethereumWalletAddress);
                    case usdc -> new Invoice.Wallet(Network.ETHEREUM, this.ethereumWalletAddress);
                    default -> throw OnlyDustException.internalServerError("Currency %s not supported".formatted(this.currency.toDomain()));
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
