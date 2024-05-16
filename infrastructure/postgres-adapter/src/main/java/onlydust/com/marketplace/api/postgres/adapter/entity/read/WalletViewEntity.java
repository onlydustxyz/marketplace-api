package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
@EqualsAndHashCode
@Data
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

    public WalletLocator ethereum() {
        assert network == NetworkEnumEntity.ethereum;
        return switch (type) {
            case address -> new WalletLocator(Ethereum.accountAddress(address));
            case name -> new WalletLocator(Ethereum.name(address));
        };
    }

    public EvmAccountAddress optimism() {
        assert network == NetworkEnumEntity.optimism;
        return new EvmAccountAddress(address);
    }

    public AptosAccountAddress aptos() {
        assert network == NetworkEnumEntity.aptos;
        return new AptosAccountAddress(address);
    }

    public StarknetAccountAddress starknet() {
        assert network == NetworkEnumEntity.starknet;
        return new StarknetAccountAddress(address);
    }
}
