package onlydust.com.marketplace.api.infura.adapters;

import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.port.out.WalletValidator;
import onlydust.com.marketplace.api.infura.Web3Client;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Name;
import org.web3j.ens.EnsResolver;

import java.math.BigInteger;

import static java.math.BigInteger.ZERO;

@Slf4j
public class EthWeb3EnsValidatorAdapter extends Web3Client implements WalletValidator<Name> {
    private final EnsResolver resolver;

    public EthWeb3EnsValidatorAdapter(Properties properties) {
        super(properties);
        this.resolver = new EnsResolver(web3j);
    }

    @Override
    public boolean isValid(Name wallet) {
        final var address = resolver.resolve(wallet.toString());
        return new BigInteger(address.substring(2), 16).compareTo(ZERO) > 0;
    }
}
