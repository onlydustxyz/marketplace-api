package onlydust.com.marketplace.accounting.domain.stubs;

import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.model.blockchain.Optimism;

import java.math.BigInteger;

public interface ERC20Tokens {
    ERC20 LORDS = new ERC20(Blockchain.ETHEREUM, Ethereum.contractAddress("0x686f2404e77Ab0d9070a46cdfb0B7feCDD2318b0"), "Lords", "LORDS", 18, BigInteger.TEN);
    ERC20 OP = new ERC20(Blockchain.ETHEREUM, Optimism.contractAddress("0x4200000000000000000000000000000000000042"), "Optimism", "OP", 18, BigInteger.TEN);
    ERC20 USDC = new ERC20(Blockchain.ETHEREUM, Ethereum.contractAddress("0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48"), "USD Coin", "USDC", 6, BigInteger.TEN);
}
