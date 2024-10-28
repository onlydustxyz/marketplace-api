package onlydust.com.marketplace.api.stellar;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.Environment;
import org.springframework.retry.RetryException;
import org.springframework.retry.annotation.Retryable;
import org.stellar.sdk.Asset;
import org.stellar.sdk.Network;
import org.stellar.sdk.Server;
import org.stellar.sdk.requests.ErrorResponse;
import org.stellar.sdk.responses.operations.OperationResponse;

import java.io.IOException;
import java.util.List;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

@Accessors(fluent = true)
public class HorizonClient {
    private final @NonNull Server server;
    private final @NonNull Network network;

    public HorizonClient(Properties properties) {
        server = new Server(properties.baseUri);
        network = properties.environment == Environment.MAINNET ? Network.PUBLIC : Network.TESTNET;
    }

    @Retryable(retryFor = {RetryException.class})
    public List<OperationResponse> payments(String hash) {
        try {
            return server.payments().forTransaction(hash).execute().getRecords();
        } catch (ErrorResponse e) {
            throw internalServerError("Error while fetching transaction", e);
        } catch (IOException e) {
            throw new RetryException("Error while fetching transaction", e);
        }
    }

    @Retryable(retryFor = {RetryException.class})
    public String contractId(Asset asset) {
        try {
            return asset.getContractId(network);
        } catch (ErrorResponse e) {
            throw internalServerError("Error while fetching transaction", e);
        } catch (IOException e) {
            throw new RetryException("Error while fetching transaction", e);
        }
    }

    @Data
    @Accessors(fluent = false)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Properties {
        String baseUri;
        Environment environment;
    }
}
