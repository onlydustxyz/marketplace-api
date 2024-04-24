package onlydust.com.marketplace.api.infrastructure.aptosrpc.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.port.out.WalletValidator;
import onlydust.com.marketplace.api.infrastructure.aptosrpc.RpcClient;
import onlydust.com.marketplace.kernel.model.blockchain.aptos.AptosAccountAddress;

@AllArgsConstructor
public class AptosAccountValidatorAdapter implements WalletValidator<AptosAccountAddress> {
    private final RpcClient client;

    @Override
    public boolean isValid(AptosAccountAddress wallet) {
        return client.getAccount(wallet.toString()).isPresent();
    }
}
