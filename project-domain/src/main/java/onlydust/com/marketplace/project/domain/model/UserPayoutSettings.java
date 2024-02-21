package onlydust.com.marketplace.project.domain.model;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Wallet;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.AccountAddress;
import onlydust.com.marketplace.project.domain.model.bank.AccountNumber;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Data
@Builder(toBuilder = true)
public class UserPayoutSettings {

    @Builder.Default
    List<Currency> pendingPaymentsCurrencies = new ArrayList<>();

    public boolean hasPendingPayments() {
        return !pendingPaymentsCurrencies.isEmpty();
    }

    public boolean hasValidPayoutSettings() {
        return nonNull(this) && pendingPaymentsCurrencies.stream().allMatch(currency -> switch (currency) {
            case USD -> nonNull(this.sepaAccount) && this.sepaAccount.valid();
            case ETH, LORDS, USDC -> nonNull(this.ethWallet);
            case APT -> nonNull(this.aptosAddress);
            case OP -> nonNull(this.optimismAddress);
            case STRK -> nonNull(this.starknetAddress);
        });
    }

    public boolean isMissingOptimismWallet() {
        return pendingPaymentsCurrencies.contains(Currency.OP) && isNull(this.optimismAddress);
    }

    public boolean isMissingAptosWallet() {
        return pendingPaymentsCurrencies.contains(Currency.APT) && isNull(this.aptosAddress);
    }

    public boolean isMissingStarknetWallet() {
        return pendingPaymentsCurrencies.contains(Currency.STRK) && isNull(this.starknetAddress);
    }

    public boolean isMissingEthereumWallet() {
        return (pendingPaymentsCurrencies.contains(Currency.ETH)
                || pendingPaymentsCurrencies.contains(Currency.LORDS)
                || pendingPaymentsCurrencies.contains(Currency.USDC))
               && isNull(this.ethWallet);
    }

    public boolean isMissingSepaAccount() {
        return pendingPaymentsCurrencies.contains(Currency.USD) && (isNull(this.sepaAccount) || !this.sepaAccount.valid());
    }


    public boolean isValid() {
        if (!this.hasPendingPayments()) {
            return true;
        }
        return hasValidPayoutSettings();
    }


    Wallet ethWallet;
    onlydust.com.marketplace.kernel.model.blockchain.evm.AccountAddress optimismAddress;
    onlydust.com.marketplace.kernel.model.blockchain.aptos.AccountAddress aptosAddress;
    AccountAddress starknetAddress;
    SepaAccount sepaAccount;

    @Data
    @Builder
    public static class SepaAccount {
        String bic;
        AccountNumber accountNumber;

        public boolean valid() {
            return nonNull(bic) && nonNull(accountNumber);
        }
    }
}
