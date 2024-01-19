package onlydust.com.marketplace.accounting.domain;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.port.out.ERC20Provider;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;

@AllArgsConstructor
public class ERC20ProviderFactory {
    private final ERC20Provider ethereumProvider;
    private final ERC20Provider optimismProvider;
    private final ERC20Provider starknetProvider;

    public ERC20Provider get(Blockchain blockchain) {
        return switch (blockchain) {
            case ETHEREUM -> ethereumProvider;
            case OPTIMISM -> optimismProvider;
            case STARKNET -> starknetProvider;
            default -> throw OnlyDustException.badRequest("ERC20 tokens on %s are not supported".formatted(blockchain.pretty()));
        };
    }
}
