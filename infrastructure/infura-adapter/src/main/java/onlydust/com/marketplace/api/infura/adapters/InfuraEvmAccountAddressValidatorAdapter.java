package onlydust.com.marketplace.api.infura.adapters;

import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.port.out.WalletValidator;
import onlydust.com.marketplace.api.infura.InfuraClient;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmAccountAddress;

import static org.web3j.crypto.Keys.toChecksumAddress;
import static org.web3j.crypto.WalletUtils.isValidAddress;

@Slf4j
public class InfuraEvmAccountAddressValidatorAdapter extends InfuraClient implements WalletValidator<EvmAccountAddress> {
    public InfuraEvmAccountAddressValidatorAdapter(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isValid(EvmAccountAddress accountAddress) {
        return isValid(accountAddress.toString());
    }

    private boolean isValid(String accountAddress) {
        return isValidAddress(accountAddress) && toChecksumAddress(accountAddress).equals(accountAddress);
    }
}
