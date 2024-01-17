package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.NetworkEnumEntity;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ContractAddress;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigInteger;

@Entity
@Table(name = "erc20", schema = "public")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true)
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@IdClass(ERC20Entity.PrimaryKey.class)
@TypeDef(name = "network", typeClass = PostgreSQLEnumType.class)
public class ERC20Entity {
    @Id
    @EqualsAndHashCode.Include
    @Type(type = "network")
    @Enumerated(EnumType.STRING)
    private @NonNull NetworkEnumEntity blockchain;
    @Id
    @EqualsAndHashCode.Include
    private @NonNull String address;
    private @NonNull String name;
    private @NonNull String symbol;
    private @NonNull Integer decimals;
    private @NonNull BigInteger totalSupply;

    public static ERC20Entity of(ERC20 erc20) {
        return ERC20Entity.builder()
                .blockchain(NetworkEnumEntity.of(erc20.blockchain()))
                .address(erc20.address().toString())
                .name(erc20.name())
                .symbol(erc20.symbol())
                .decimals(erc20.decimals())
                .totalSupply(erc20.totalSupply())
                .build();
    }

    public ERC20 toDomain() {
        return new ERC20(
                blockchain.toBlockchain(),
                Ethereum.contractAddress(address),
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

        public static PrimaryKey of(Blockchain blockchain, ContractAddress address) {
            return new PrimaryKey(NetworkEnumEntity.of(blockchain), address.toString());
        }
    }
}
