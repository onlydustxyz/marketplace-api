package onlydust.com.marketplace.api.infura.adapters;

import onlydust.com.marketplace.accounting.domain.port.out.WalletValidator;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetAccountAddress;

import static com.swmansion.starknet.data.ContractAddressCalculator.isChecksumAddressValid;

public class StarknetAccountValidatorAdapter implements WalletValidator<StarknetAccountAddress> {
    @Override
    public boolean isValid(StarknetAccountAddress address) {
        return !isCheckSummed(address) || isChecksumAddressValid(address.toString());
    }

    private boolean isCheckSummed(StarknetAccountAddress address) {
        return !address.toString().toLowerCase().equals(address.toString());
    }
}
