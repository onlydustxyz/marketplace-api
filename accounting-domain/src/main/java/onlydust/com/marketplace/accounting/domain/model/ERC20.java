package onlydust.com.marketplace.accounting.domain.model;

import onlydust.com.marketplace.kernel.model.blockchain.evm.ContractAddress;

import java.math.BigInteger;

public record ERC20(ContractAddress address, String name, String symbol, Integer decimals, BigInteger totalSupply) {
}
