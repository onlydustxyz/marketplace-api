package onlydust.com.marketplace.accounting.domain.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.Hash;

import java.math.BigInteger;

@AllArgsConstructor
@Value
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ERC20 {
    @EqualsAndHashCode.Include
    Blockchain blockchain;
    @EqualsAndHashCode.Include
    Hash address;
    String name;
    String symbol;
    Integer decimals;
    BigInteger totalSupply;
}
