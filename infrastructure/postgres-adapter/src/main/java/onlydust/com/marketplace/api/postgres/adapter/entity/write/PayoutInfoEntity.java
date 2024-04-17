package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.WalletTypeEnumEntity;
import onlydust.com.marketplace.kernel.model.bank.BankAccount;
import onlydust.com.marketplace.kernel.model.blockchain.Aptos;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.model.blockchain.Optimism;
import onlydust.com.marketplace.kernel.model.blockchain.StarkNet;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.nonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(schema = "accounting", name = "payout_infos")
@EntityListeners(AuditingEntityListener.class)
public class PayoutInfoEntity {

    @Id
    UUID billingProfileId;
    @OneToOne
    @JoinColumn(name = "billingProfileId", insertable = false, updatable = false)
    BillingProfileEntity billingProfile;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "billingProfileId", referencedColumnName = "billingProfileId")
    @Builder.Default
    Set<WalletEntity> wallets = new HashSet<>();
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "billingProfileId", referencedColumnName = "billingProfileId")
    BankAccountEntity bankAccount;

    @CreationTimestamp
    @Column(name = "tech_created_at", nullable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Date createdAt;
    @UpdateTimestamp
    @Column(name = "tech_updated_at", nullable = false)
    @EqualsAndHashCode.Exclude
    private Date updatedAt;

    public void addWallets(final WalletEntity walletEntity) {
        this.wallets.add(walletEntity);
    }

    public PayoutInfo toDomain() {
        PayoutInfo payoutInfo = PayoutInfo.builder().build();
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

    public static PayoutInfoEntity toEntity(final BillingProfile.Id billingProfileId, final PayoutInfo payoutInfo) {
        PayoutInfoEntity entity = PayoutInfoEntity.builder()
                .billingProfileId(billingProfileId.value())
                .build();
        if (nonNull(payoutInfo.getBankAccount())) {
            entity = entity.toBuilder()
                    .bankAccount(BankAccountEntity.builder()
                            .bic(payoutInfo.getBankAccount().bic())
                            .number(payoutInfo.getBankAccount().accountNumber())
                            .billingProfileId(billingProfileId.value())
                            .build())
                    .build();
        }
        if (nonNull(payoutInfo.getAptosAddress())) {
            entity.addWallets(WalletEntity.builder()
                    .address(payoutInfo.getAptosAddress().toString())
                    .type(WalletTypeEnumEntity.address)
                    .billingProfileId(billingProfileId.value())
                    .network(NetworkEnumEntity.aptos)
                    .build());
        }
        if (nonNull(payoutInfo.getOptimismAddress())) {
            entity.addWallets(WalletEntity.builder()
                    .address(payoutInfo.getOptimismAddress().toString())
                    .type(WalletTypeEnumEntity.address)
                    .billingProfileId(billingProfileId.value())
                    .network(NetworkEnumEntity.optimism)
                    .build());
        }
        if (nonNull(payoutInfo.getStarknetAddress())) {
            entity.addWallets(WalletEntity.builder()
                    .address(payoutInfo.getStarknetAddress().toString())
                    .type(WalletTypeEnumEntity.address)
                    .billingProfileId(billingProfileId.value())
                    .network(NetworkEnumEntity.starknet)
                    .build());
        }
        if (nonNull(payoutInfo.getEthWallet())) {
            entity.addWallets(WalletEntity.builder()
                    .address(payoutInfo.getEthWallet().asString())
                    .type(payoutInfo.getEthWallet().accountAddress().isPresent() ?
                            WalletTypeEnumEntity.address : WalletTypeEnumEntity.name)
                    .billingProfileId(billingProfileId.value())
                    .network(NetworkEnumEntity.ethereum)
                    .build());
        }
        return entity;
    }
}
