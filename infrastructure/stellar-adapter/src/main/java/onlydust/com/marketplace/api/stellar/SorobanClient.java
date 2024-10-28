package onlydust.com.marketplace.api.stellar;

import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.Environment;
import org.springframework.retry.RetryException;
import org.springframework.retry.annotation.Retryable;
import org.stellar.sdk.*;
import org.stellar.sdk.requests.sorobanrpc.SorobanRpcErrorResponse;
import org.stellar.sdk.xdr.SCVal;

import java.io.IOException;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;
import static org.stellar.sdk.AbstractTransaction.MIN_BASE_FEE;
import static org.stellar.sdk.InvokeHostFunctionOperation.invokeContractFunctionOperationBuilder;

@Accessors(fluent = true)
public class SorobanClient {
    private final @NonNull SorobanServer server;
    private final @NonNull String accountId;
    private final @NonNull Network network;

    @Getter(lazy = true)
    private final @NonNull TransactionBuilderAccount account = getAccount();

    public SorobanClient(Properties properties) {
        server = new SorobanServer(properties.baseUri);
        accountId = properties.accountId;
        network = properties.environment == Environment.MAINNET ? Network.PUBLIC : Network.TESTNET;
    }

    private TransactionBuilderAccount getAccount() {
        try {
            assert server != null;
            return server.getAccount(accountId);
        } catch (AccountNotFoundException e) {
            throw internalServerError("Account not found", e);
        } catch (SorobanRpcErrorResponse | IOException e) {
            throw internalServerError("Error while fetching account", e);
        }
    }

    @Retryable(retryFor = {RetryException.class})
    public SCVal call(final @NonNull String contractId, final @NonNull String method) {
        try {
            final var transaction = server.simulateTransaction(new TransactionBuilder(account(), network)
                    .addOperation(invokeContractFunctionOperationBuilder(contractId, method, null).build())
                    .setTimeout(30)
                    .setBaseFee(MIN_BASE_FEE)
                    .build());

            return SCVal.fromXdrBase64(transaction.getResults().get(0).getXdr());
        } catch (SorobanRpcErrorResponse e) {
            throw internalServerError("Error while sending transaction", e);
        } catch (IOException e) {
            throw new RetryException("Error while sending transaction", e);
        }
    }

    @Data
    @Accessors(fluent = false)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Properties {
        String baseUri;
        String accountId;
        Environment environment;
    }
}
