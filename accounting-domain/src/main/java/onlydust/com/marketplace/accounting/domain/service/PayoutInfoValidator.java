package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.accounting.domain.port.out.WalletValidator;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Name;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;

@AllArgsConstructor
public class PayoutInfoValidator {
    private final WalletValidator<Name> ensValidator;

    public void validate(PayoutInfo payoutInfo) {
        payoutInfo.ethWallet().ifPresent(this::validate);
    }

    private void validate(WalletLocator wallet) {
        wallet.ens().ifPresent(this::validate);
    }

    private void validate(Name ens) {
        if (!ensValidator.isValid(ens))
            throw badRequest("%s is not a valid ENS".formatted(ens.asString()));
    }
}
