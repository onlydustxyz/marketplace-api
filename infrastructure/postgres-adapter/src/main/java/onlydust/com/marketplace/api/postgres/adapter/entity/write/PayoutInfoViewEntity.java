package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import io.hypersistence.utils.hibernate.type.array.EnumArrayType;
import io.hypersistence.utils.hibernate.type.array.internal.AbstractArrayType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.accounting.domain.view.PayoutInfoView;
import onlydust.com.marketplace.kernel.model.bank.BankAccount;
import onlydust.com.marketplace.kernel.model.blockchain.Aptos;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.model.blockchain.Optimism;
import onlydust.com.marketplace.kernel.model.blockchain.StarkNet;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Entity
public class PayoutInfoViewEntity {
    @Id
    UUID billingProfileId;

    @Type(
            value = EnumArrayType.class,
            parameters = @Parameter(
                    name = AbstractArrayType.SQL_ARRAY_TYPE,
                    value = "public.network"
            )
    )
    @Column(name = "networks", columnDefinition = "public.network[]")
    NetworkEnumEntity[] networks;

    @OneToMany
    @JoinColumn(name = "billingProfileId", referencedColumnName = "billingProfileId")
    @Builder.Default
    Set<WalletEntity> wallets = new HashSet<>();

    @OneToOne
    @JoinColumn(name = "billingProfileId", referencedColumnName = "billingProfileId")
    BankAccountEntity bankAccount;

    public PayoutInfoView toDomain() {
        PayoutInfoView payoutInfo = PayoutInfoView.builder()
                .requiredNetworksForRewards(isNull(this.networks) ? Set.of() :
                        Arrays.stream(this.networks).filter(Objects::nonNull).map(NetworkEnumEntity::toNetwork).collect(Collectors.toSet()))
                .build();
        if (nonNull(this.getBankAccount())) {
            payoutInfo = payoutInfo.toBuilder()
                    .bankAccount(BankAccount.builder()
                            .bic(this.getBankAccount().getBic())
                            .accountNumber(this.getBankAccount().getNumber())
                            .build())
                    .build();
        }
        if (!this.getWallets().isEmpty()) {
            for (WalletEntity wallet : this.getWallets()) {
                switch (wallet.getNetwork()) {
                    case ethereum -> {
                        payoutInfo = switch (wallet.getType()) {
                            case address -> payoutInfo.toBuilder()
                                    .ethWallet(new WalletLocator(Ethereum.accountAddress(wallet.getAddress())))
                                    .build();
                            case name -> payoutInfo.toBuilder()
                                    .ethWallet(new WalletLocator(Ethereum.name(wallet.getAddress())))
                                    .build();
                        };
                    }
                    case aptos -> payoutInfo = payoutInfo.toBuilder()
                            .aptosAddress(Aptos.accountAddress(wallet.getAddress()))
                            .build();
                    case starknet -> payoutInfo = payoutInfo.toBuilder()
                            .starknetAddress(StarkNet.accountAddress(wallet.getAddress()))
                            .build();
                    case optimism -> payoutInfo = payoutInfo.toBuilder()
                            .optimismAddress(Optimism.accountAddress(wallet.getAddress()))
                            .build();
                }
            }
        }
        return payoutInfo;
    }
}
