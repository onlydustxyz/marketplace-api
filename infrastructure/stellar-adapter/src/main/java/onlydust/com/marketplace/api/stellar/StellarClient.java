package onlydust.com.marketplace.api.stellar;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.retry.annotation.Retryable;
import org.stellar.sdk.Server;
import org.stellar.sdk.requests.ErrorResponse;
import org.stellar.sdk.responses.TransactionResponse;

import java.io.IOException;
import java.util.Optional;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

public class StellarClient {
    private final Server horizonServer;

    public StellarClient(Properties properties) {
        horizonServer = new Server(properties.getHorizonBaseUri());
    }

    @Retryable(retryFor = {IOException.class})
    public Optional<TransactionResponse> transaction(String hash) {
        try {
            return Optional.ofNullable(horizonServer.transactions().transaction(hash));
        } catch (ErrorResponse e) {
            if (e.getCode() == 404) return Optional.empty();
            throw internalServerError("Error while fetching transaction", e);
        } catch (IOException e) {
            throw internalServerError("Error while fetching transaction", e);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Properties {
        String horizonBaseUri;
    }
}
