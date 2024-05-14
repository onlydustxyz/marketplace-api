package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
import onlydust.com.marketplace.kernel.model.blockchain.Aptos;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.model.blockchain.StarkNet;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.UUID;

@Entity
@Table(name = "erc20", schema = "public")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true)
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@IdClass(ERC20ViewEntity.PrimaryKey.class)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Immutable
public class ERC20ViewEntity {
    @Id
    @EqualsAndHashCode.Include
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "network")
    private @NonNull NetworkEnumEntity blockchain;
    @Id
    @EqualsAndHashCode.Include
    private @NonNull String address;
    private @NonNull UUID currencyId;
    private @NonNull String name;
    private @NonNull String symbol;
    private @NonNull Integer decimals;
    private @NonNull BigInteger totalSupply;

    public ERC20 toDomain() {
        return new ERC20(
                blockchain.toBlockchain(),
                switch (blockchain.toBlockchain()) {
                    case ETHEREUM, OPTIMISM -> Ethereum.contractAddress(address);
                    case STARKNET -> StarkNet.contractAddress(address);
                    case APTOS -> Aptos.coinType(address);
                },
                name,
                symbol,
                decimals,
                totalSupply
        );
    }

    @EqualsAndHashCode
    @AllArgsConstructor
    @Data
    @NoArgsConstructor(force = true)
    public static class PrimaryKey implements Serializable {
        final @NonNull NetworkEnumEntity blockchain;
        final @NonNull String address;
    }
}
