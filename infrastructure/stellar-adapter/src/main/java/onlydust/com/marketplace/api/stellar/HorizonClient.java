package onlydust.com.marketplace.api.stellar;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.springframework.retry.RetryException;
import org.springframework.retry.annotation.Retryable;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Server;
import org.stellar.sdk.requests.ErrorResponse;
import org.stellar.sdk.responses.AssetResponse;
import org.stellar.sdk.xdr.Asset;

import java.io.IOException;
import java.util.Optional;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

@Accessors(fluent = true)
public class HorizonClient {
    private final @NonNull Server server;

    public HorizonClient(Properties properties) {
        server = new Server(properties.baseUri);
    }

    @Retryable(retryFor = {RetryException.class})
    public Optional<AssetResponse> asset(Asset asset) {
        try {
            final var issuer = KeyPair.fromXdrPublicKey(switch (asset.getDiscriminant()) {
                case ASSET_TYPE_CREDIT_ALPHANUM4 -> asset.getAlphaNum4().getIssuer().getAccountID();
                case ASSET_TYPE_CREDIT_ALPHANUM12 -> asset.getAlphaNum12().getIssuer().getAccountID();
                default -> throw OnlyDustException.internalServerError("Unsupported asset type: %s".formatted(asset.getDiscriminant()));
            }).getAccountId();

            final var code = new String(switch (asset.getDiscriminant()) {
                case ASSET_TYPE_CREDIT_ALPHANUM4 -> asset.getAlphaNum4().getAssetCode().getAssetCode4();
                case ASSET_TYPE_CREDIT_ALPHANUM12 -> asset.getAlphaNum12().getAssetCode().getAssetCode12();
                default -> throw OnlyDustException.internalServerError("Unsupported asset type: %s".formatted(asset.getDiscriminant()));
            });

            return server.assets()
                    .assetIssuer(issuer)
                    .assetCode(code)
                    .limit(1)
                    .execute()
                    .getRecords()
                    .stream()
                    .findFirst();
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
    }
}
