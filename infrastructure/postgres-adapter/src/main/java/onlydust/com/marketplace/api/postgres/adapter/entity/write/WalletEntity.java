package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.WalletTypeEnumEntity;
import onlydust.com.marketplace.kernel.model.blockchain.*;
import onlydust.com.marketplace.kernel.model.blockchain.aptos.AptosAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.stellar.StellarAccountId;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "wallets", schema = "accounting")
@IdClass(WalletEntity.PrimaryKey.class)
@EntityListeners(AuditingEntityListener.class)
public class WalletEntity {
    @Id
    @EqualsAndHashCode.Include
    UUID billingProfileId;

    @Id
    @EqualsAndHashCode.Include
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "network")
    NetworkEnumEntity network;

    @ManyToOne
    @JoinColumn(name = "billingProfileId", insertable = false, updatable = false)
    BillingProfileEntity billingProfile;

    @Column(name = "address", nullable = false)
    String address;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "wallet_type", nullable = false)
    WalletTypeEnumEntity type;

    @CreationTimestamp
    @Column(name = "tech_created_at", nullable = false, updatable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "tech_updated_at", nullable = false)
    private Date updatedAt;

    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        UUID billingProfileId;
        NetworkEnumEntity network;
    }

    public static WalletEntity ethereum(@NonNull BillingProfile.Id billingProfileId, @NonNull WalletLocator wallet) {
        return WalletEntity.builder()
                .billingProfileId(billingProfileId.value())
                .network(NetworkEnumEntity.ETHEREUM)
                .address(wallet.asString())
                .type(wallet.accountAddress().isPresent() ? WalletTypeEnumEntity.address : WalletTypeEnumEntity.name)
                .build();
    }

    public WalletLocator ethereum() {
        assert network == NetworkEnumEntity.ETHEREUM;
        return switch (type) {
            case address -> new WalletLocator(Ethereum.accountAddress(address));
            case name -> new WalletLocator(Ethereum.name(address));
        };
    }

    public static WalletEntity optimism(@NonNull BillingProfile.Id billingProfileId, @NonNull EvmAccountAddress address) {
        return WalletEntity.builder()
                .billingProfileId(billingProfileId.value())
                .network(NetworkEnumEntity.OPTIMISM)
                .address(address.toString())
                .type(WalletTypeEnumEntity.address)
                .build();
    }

    public EvmAccountAddress optimism() {
        assert network == NetworkEnumEntity.OPTIMISM;
        return Optimism.accountAddress(address);
    }

    public static WalletEntity aptos(@NonNull BillingProfile.Id billingProfileId, @NonNull AptosAccountAddress address) {
        return WalletEntity.builder()
                .billingProfileId(billingProfileId.value())
                .network(NetworkEnumEntity.APTOS)
                .address(address.toString())
                .type(WalletTypeEnumEntity.address)
                .build();
    }

    public AptosAccountAddress aptos() {
        assert network == NetworkEnumEntity.APTOS;
        return Aptos.accountAddress(address);
    }

    public static WalletEntity starknet(@NonNull BillingProfile.Id billingProfileId, @NonNull StarknetAccountAddress address) {
        return WalletEntity.builder()
                .billingProfileId(billingProfileId.value())
                .network(NetworkEnumEntity.STARKNET)
                .address(address.toString())
                .type(WalletTypeEnumEntity.address)
                .build();
    }

    public StarknetAccountAddress starknet() {
        assert network == NetworkEnumEntity.STARKNET;
        return StarkNet.accountAddress(address);
    }

    public static WalletEntity stellar(@NonNull BillingProfile.Id billingProfileId, @NonNull StellarAccountId accountId) {
        return WalletEntity.builder()
                .billingProfileId(billingProfileId.value())
                .network(NetworkEnumEntity.STELLAR)
                .address(accountId.toString())
                .type(WalletTypeEnumEntity.address)
                .build();
    }

    public StellarAccountId stellar() {
        assert network == NetworkEnumEntity.STELLAR;
        return Stellar.accountId(address);
    }
}
