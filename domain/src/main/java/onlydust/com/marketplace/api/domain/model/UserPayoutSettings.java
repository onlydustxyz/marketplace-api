package onlydust.com.marketplace.api.domain.model;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.api.domain.model.bank.AccountNumber;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Wallet;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.AccountAddress;

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
            case Usd -> nonNull(this.sepaAccount) && this.sepaAccount.valid();
            case Eth, Lords, Usdc -> nonNull(this.ethWallet);
            case Apt -> nonNull(this.aptosAddress);
            case Op -> nonNull(this.optimismAddress);
            case Strk -> nonNull(this.starknetAddress);
        });
    }

    public boolean isMissingOptimismWallet() {
        return pendingPaymentsCurrencies.contains(Currency.Op) && isNull(this.optimismAddress);
    }

    public boolean isMissingAptosWallet() {
        return pendingPaymentsCurrencies.contains(Currency.Apt) && isNull(this.aptosAddress);
    }

    public boolean isMissingStarknetWallet() {
        return pendingPaymentsCurrencies.contains(Currency.Strk) && isNull(this.starknetAddress);
    }

    public boolean isMissingEthereumWallet() {
        return (pendingPaymentsCurrencies.contains(Currency.Eth)
                || pendingPaymentsCurrencies.contains(Currency.Lords)
                || pendingPaymentsCurrencies.contains(Currency.Usdc))
               && isNull(this.ethWallet);
    }

    public boolean isMissingSepaAccount() {
        return pendingPaymentsCurrencies.contains(Currency.Usd) && (isNull(this.sepaAccount) || !this.sepaAccount.valid());
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
