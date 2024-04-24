package onlydust.com.marketplace.api.infura.adapters;

import com.swmansion.starknet.data.types.Call;
import com.swmansion.starknet.data.types.Felt;
import com.swmansion.starknet.data.types.StarknetChainId;
import com.swmansion.starknet.provider.Provider;
import com.swmansion.starknet.provider.rpc.JsonRpcProvider;
import onlydust.com.marketplace.accounting.domain.port.out.WalletValidator;
import onlydust.com.marketplace.api.infura.InfuraClient;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetAccountAddress;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

public class StarknetInfuraAccountValidatorAdapter implements WalletValidator<StarknetAccountAddress> {
    private static final Felt SRC6_INTERFACE_ID = new Felt(new BigInteger("1270010605630597976495846281167968799381097569185364931397797212080166453709"));
    Provider provider;

    public StarknetInfuraAccountValidatorAdapter(final InfuraClient.Properties properties) {
        provider = new JsonRpcProvider("%s/%s".formatted(properties.getBaseUri(), properties.getApiKey()), StarknetChainId.MAINNET);
    }

    @Override
    public boolean isValid(StarknetAccountAddress wallet) {
        try {
            return provider.callContract(new Call(Felt.fromHex(wallet.toString()), "supports_interface", List.of(SRC6_INTERFACE_ID)))
                    .sendAsync()
                    .thenApply(r -> r.get(0).decString().equals("1"))
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            throw internalServerError("Unable to validate StarkNet account address %s".formatted(wallet), e);
        }
    }
}
