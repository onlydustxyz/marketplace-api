package onlydust.com.marketplace.api.stellar.adapters;

import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.port.out.WalletValidator;
import onlydust.com.marketplace.kernel.model.blockchain.stellar.StellarAccountId;
import org.stellar.sdk.Address;

@Slf4j
public class StellarAccountIdValidator implements WalletValidator<StellarAccountId> {
    @Override
    public boolean isValid(StellarAccountId wallet) {
        try {
            final var address = new Address(wallet.toString());
            final var isValid = address.getAddressType() == Address.AddressType.ACCOUNT;
            if (!isValid) {
                LOGGER.warn("Invalid Stellar account id: {} (type = {})", wallet, address.getAddressType());
            }
            return isValid;
        } catch (Exception e) {
            LOGGER.warn("Invalid Stellar account id: {}", wallet, e);
            return false;
        }
    }
}
