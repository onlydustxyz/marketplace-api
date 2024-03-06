package onlydust.com.marketplace.api.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

@Slf4j
@AllArgsConstructor
public class MakeWebhookHttpClient {
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final Config config;

    public <RequestBody> void post(final RequestBody requestBody) {
        post(requestBody, config.getUrl(), null);
    }

    public <RequestBody> void post(final RequestBody requestBody, URI url, final String apiKey) {
        try {
            url = isNull(apiKey) ? url : new URI(url.toString() + "?api-key=%s".formatted(apiKey));
            final HttpResponse<byte[]> httpResponse =
                    httpClient.send(HttpRequest.newBuilder().uri(url)
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofByteArray(objectMapper.writeValueAsBytes(requestBody)))
                            .build(), HttpResponse.BodyHandlers.ofByteArray());

            final int statusCode = httpResponse.statusCode();
            if (statusCode >= 400) {
                throw OnlyDustException.internalServerError(format("Error (status %d) when calling webhook %s",
                        statusCode, config.getUrl()));
            }
        } catch (JsonProcessingException e) {
            throw internalServerError("Fail to serialize request", e);
        } catch (IOException | InterruptedException e) {
            throw internalServerError("Fail send request to webhook %s".formatted(config.getUrl()), e);
        } catch (URISyntaxException e) {
            throw internalServerError("Invalid webhook url", e);
        }
    }


}
