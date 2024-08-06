package onlydust.com.marketplace.api.stellar;

import onlydust.com.marketplace.accounting.domain.port.out.WalletValidator;
import onlydust.com.marketplace.kernel.model.blockchain.stellar.StellarAccountId;
import org.stellar.sdk.Address;

public class StellarAccountIdValidator implements WalletValidator<StellarAccountId> {
    @Override
    public boolean isValid(StellarAccountId wallet) {
        try {
            final var address = new Address(wallet.toString());
            return address.getAddressType() == Address.AddressType.ACCOUNT;
        } catch (Exception e) {
            return false;
        }
    }
}
