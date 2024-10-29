package onlydust.com.marketplace.api.near;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.JsonRpcMethod;
import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.ProxyUtil;
import com.syntifi.near.api.common.exception.NearException;
import com.syntifi.near.api.rpc.NearRpcObjectMapper;
import com.syntifi.near.api.rpc.jsonrpc4j.exception.NearExceptionResolver;
import com.syntifi.near.api.rpc.model.transaction.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.near.dto.TxExecutionStatus;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

public interface NearClient extends com.syntifi.near.api.rpc.NearClient {
    static NearClient create(Properties properties) throws NearException {
        try {
            final var objectMapper = NearRpcObjectMapper.INSTANCE;
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            final var client = new JsonRpcHttpClient(objectMapper, new URL(properties.baseUri), Map.of("Content-Type", "application/json"));
            client.setExceptionResolver(new NearExceptionResolver());
            return ProxyUtil.createClientProxy(NearClient.class.getClassLoader(), NearClient.class, client);
        } catch (MalformedURLException e) {
            throw internalServerError("Invalid URL", e);
        }
    }

    @JsonRpcMethod("tx")
    TransactionStatus getTransactionStatus(@JsonRpcParam("tx_hash") String txHash,
                                           @JsonRpcParam("sender_account_id") String senderAccountId,
                                           @JsonRpcParam("wait_until") TxExecutionStatus waitUntil) throws NearException;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class Properties {
        String baseUri;
    }
}
