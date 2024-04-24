package onlydust.com.marketplace.accounting.domain.stubs;

import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.kernel.model.blockchain.Aptos;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.model.blockchain.Optimism;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetContractAddress;

import java.math.BigInteger;

public interface ERC20Tokens {
    ERC20 LORDS = new ERC20(Blockchain.ETHEREUM, Ethereum.contractAddress("0x686f2404e77Ab0d9070a46cdfb0B7feCDD2318b0"), "Lords", "LORDS", 18, BigInteger.TEN);
    ERC20 APT = new ERC20(Blockchain.APTOS, Aptos.coinType("0x1::aptos_coin::AptosCoin"), "Aptos Coin", "APT", 6, BigInteger.TEN);
    ERC20 OP = new ERC20(Blockchain.OPTIMISM, Optimism.contractAddress("0x4200000000000000000000000000000000000042"), "Optimism", "OP", 18, BigInteger.TEN);
    ERC20 ETH_USDC = new ERC20(Blockchain.ETHEREUM, Ethereum.contractAddress("0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48"), "USD Coin", "USDC", 6,
            BigInteger.TEN);
    ERC20 OP_USDC = new ERC20(Blockchain.OPTIMISM, Ethereum.contractAddress("0x0b2C639c533813f4Aa9D7837CAf62653d097Ff85"), "USD Coin", "USDC", 6,
            BigInteger.TEN);
    ERC20 STRK = new ERC20(Blockchain.ETHEREUM, Ethereum.contractAddress("0xCa14007Eff0dB1f8135f4C25B34De49AB0d42766"), "Starknet Token", "STRK", 18,
            BigInteger.TEN);
    ERC20 STARKNET_STRK = new ERC20(Blockchain.STARKNET, new StarknetContractAddress("0xCa14007Eff0dB1f8135f4C25B34De49AB0d42766"), "Starknet Token", "STRK",
            18, BigInteger.TEN);

    ERC20 STARKNET_ETH = new ERC20(Blockchain.STARKNET, new StarknetContractAddress("0x049d36570d4e46f48e99674bd3fcc84644ddd6b96f7c741b1562b82f9e004dc7"),
            "Ether", "ETH",
            18, BigInteger.TEN);
}
