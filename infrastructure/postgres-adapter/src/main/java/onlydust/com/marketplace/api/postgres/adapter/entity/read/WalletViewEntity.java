package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.WalletTypeEnumEntity;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.model.blockchain.aptos.AptosAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetAccountAddress;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;
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
@EqualsAndHashCode
@Data
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "wallets", schema = "accounting")
@IdClass(WalletViewEntity.PrimaryKey.class)
@EntityListeners(AuditingEntityListener.class)
@Immutable
public class WalletViewEntity {
    @Id
    UUID billingProfileId;
    @Id
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "network")
    NetworkEnumEntity network;

    @Column(name = "address", nullable = false)
    String address;
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "wallet_type", nullable = false)
    WalletTypeEnumEntity type;

    @CreationTimestamp
    @Column(name = "tech_created_at", nullable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Date createdAt;
    @UpdateTimestamp
    @Column(name = "tech_updated_at", nullable = false)
    @EqualsAndHashCode.Exclude
    private Date updatedAt;


    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        UUID billingProfileId;
        NetworkEnumEntity network;
    }

    public static WalletViewEntity ethereum(@NonNull BillingProfile.Id billingProfileId, @NonNull WalletLocator wallet) {
        return WalletViewEntity.builder()
                .billingProfileId(billingProfileId.value())
                .network(NetworkEnumEntity.ethereum)
                .address(wallet.asString())
                .type(wallet.accountAddress().isPresent() ? WalletTypeEnumEntity.address : WalletTypeEnumEntity.name)
                .build();
    }

    public WalletLocator ethereum() {
        assert network == NetworkEnumEntity.ethereum;
        return switch (type) {
            case address -> new WalletLocator(Ethereum.accountAddress(address));
            case name -> new WalletLocator(Ethereum.name(address));
        };
    }

    public static WalletViewEntity optimism(@NonNull BillingProfile.Id billingProfileId, @NonNull EvmAccountAddress address) {
        return WalletViewEntity.builder()
                .billingProfileId(billingProfileId.value())
                .network(NetworkEnumEntity.optimism)
                .address(address.toString())
                .type(WalletTypeEnumEntity.address)
                .build();
    }

    public EvmAccountAddress optimism() {
        assert network == NetworkEnumEntity.optimism;
        return new EvmAccountAddress(address);
    }

    public static WalletViewEntity aptos(@NonNull BillingProfile.Id billingProfileId, @NonNull AptosAccountAddress address) {
        return WalletViewEntity.builder()
                .billingProfileId(billingProfileId.value())
                .network(NetworkEnumEntity.aptos)
                .address(address.toString())
                .type(WalletTypeEnumEntity.address)
                .build();
    }

    public AptosAccountAddress aptos() {
        assert network == NetworkEnumEntity.aptos;
        return new AptosAccountAddress(address);
    }

    public static WalletViewEntity starknet(@NonNull BillingProfile.Id billingProfileId, @NonNull StarknetAccountAddress address) {
        return WalletViewEntity.builder()
                .billingProfileId(billingProfileId.value())
                .network(NetworkEnumEntity.starknet)
                .address(address.toString())
                .type(WalletTypeEnumEntity.address)
                .build();
    }

    public StarknetAccountAddress starknet() {
        assert network == NetworkEnumEntity.starknet;
        return new StarknetAccountAddress(address);
    }
}
