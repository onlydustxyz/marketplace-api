package onlydust.com.marketplace.api.stellar;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.retry.RetryException;
import org.springframework.retry.annotation.Retryable;
import org.stellar.sdk.*;
import org.stellar.sdk.requests.ErrorResponse;
import org.stellar.sdk.requests.sorobanrpc.SorobanRpcErrorResponse;
import org.stellar.sdk.responses.sorobanrpc.GetTransactionResponse;
import org.stellar.sdk.xdr.SCVal;

import java.io.IOException;
import java.util.Optional;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;
import static org.stellar.sdk.AbstractTransaction.MIN_BASE_FEE;
import static org.stellar.sdk.InvokeHostFunctionOperation.invokeContractFunctionOperationBuilder;

@Accessors(fluent = true)
public class StellarClient {
    private final @NonNull SorobanServer sorobanServer;
    private final @NonNull String accountId;

    @Getter(lazy = true)
    private final @NonNull TransactionBuilderAccount account = getAccount();

    public StellarClient(Properties properties) {
        sorobanServer = new SorobanServer(properties.sorobanBaseUri);
        accountId = properties.accountId;
    }

    private TransactionBuilderAccount getAccount() {
        try {
            assert sorobanServer != null;
            return sorobanServer.getAccount(accountId);
        } catch (AccountNotFoundException e) {
            throw internalServerError("Account not found", e);
        } catch (SorobanRpcErrorResponse | IOException e) {
            throw internalServerError("Error while fetching account", e);
        }
    }

    @Retryable(retryFor = {RetryException.class})
    public Optional<GetTransactionResponse> transaction(String hash) {
        try {
            final var response = sorobanServer.getTransaction(hash);
            return switch (response.getStatus()) {
                case NOT_FOUND -> Optional.empty();
                case SUCCESS -> Optional.of(response);
                case FAILED -> throw internalServerError("Error while fetching transaction");
            };
        } catch (ErrorResponse | SorobanRpcErrorResponse e) {
            throw internalServerError("Error while fetching transaction", e);
        } catch (IOException e) {
            throw new RetryException("Error while fetching transaction", e);
        }
    }

    @Retryable(retryFor = {RetryException.class})
    public SCVal call(final @NonNull String contractId, final @NonNull String method) {
        try {
            final var transaction = sorobanServer.simulateTransaction(new TransactionBuilder(account(), Network.PUBLIC)
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
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Properties {
        String sorobanBaseUri;
        String accountId;
    }
}

