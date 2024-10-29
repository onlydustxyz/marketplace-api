package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
import onlydust.com.marketplace.kernel.model.blockchain.*;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmContractAddress;
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
@IdClass(ERC20Entity.PrimaryKey.class)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ERC20Entity {
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

    public static ERC20Entity of(Currency.Id currencyId, ERC20 erc20) {
        return ERC20Entity.builder()
                .currencyId(currencyId.value())
                .blockchain(NetworkEnumEntity.of(erc20.getBlockchain()))
                .address(erc20.getAddress().toString())
                .name(erc20.getName())
                .symbol(erc20.getSymbol())
                .decimals(erc20.getDecimals())
                .totalSupply(erc20.getTotalSupply())
                .build();
    }

    public ERC20 toDomain() {
        return new ERC20(
                blockchain.toBlockchain(),
                switch (blockchain.toBlockchain()) {
                    case ETHEREUM, OPTIMISM -> Ethereum.contractAddress(address);
                    case STARKNET -> StarkNet.contractAddress(address);
                    case APTOS -> Aptos.coinType(address);
                    case STELLAR -> Stellar.contractAddress(address);
                    case NEAR -> throw new IllegalStateException("ERC20 are not supported on Near");
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

        public static PrimaryKey of(Blockchain blockchain, EvmContractAddress address) {
            return new PrimaryKey(NetworkEnumEntity.of(blockchain), address.toString());
        }
    }
}
