package onlydust.com.marketplace.accounting.domain.port.in;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ContractAddress;

public interface CurrencyFacadePort {
    Currency addERC20Support(final @NonNull Blockchain blockchain, final @NonNull ContractAddress tokenAddress);

    Currency addNativeCryptocurrencySupport(Currency.Code code, Integer decimals);

    Currency addIsoCurrencySupport(final @NonNull Currency.Code code);

    void refreshQuotes();

}
