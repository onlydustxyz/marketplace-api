package onlydust.com.marketplace.kernel.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpMethod;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static java.lang.String.format;
import static java.net.http.HttpRequest.BodyPublishers.noBody;
import static java.net.http.HttpRequest.BodyPublishers.ofString;
import static java.util.Objects.isNull;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.*;

public abstract class HttpClient {
    private final java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    protected abstract HttpRequest.Builder builder();

    protected abstract URI uri(final String path);

    private <Body> HttpRequest buildRequest(final HttpMethod method,
                                            final String path,
                                            final Body body) throws JsonProcessingException {
        return builder()
                .uri(uri(path))
                .header("Content-Type", "application/json")
                .method(method.name(), isNull(body) ? noBody() : ofString(objectMapper.writeValueAsString(body)))
                .build();
    }

    public <RequestBody, ResponseBody> Optional<ResponseBody> send(final String path,
                                                                   final HttpMethod method,
                                                                   final RequestBody requestBody,
                                                                   final Class<ResponseBody> responseClass) {
        final var body = send(path, method, requestBody);
        return Void.class.isAssignableFrom(responseClass) ? Optional.empty() : body.map(b -> decode(b, responseClass));
    }

    public <RequestBody, ResponseBody> Optional<ResponseBody> send(final String path,
                                                                   final HttpMethod method,
                                                                   final RequestBody requestBody,
                                                                   final TypeReference<ResponseBody> typeRef) {
        final var body = send(path, method, requestBody);
        return body.map(b -> decode(b, typeRef));
    }

    private <RequestBody> Optional<byte[]> send(final String path,
                                                final HttpMethod method,
                                                final RequestBody requestBody) {
        try {
            final var request = buildRequest(method, path, requestBody);
            final var httpResponse = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            final int statusCode = httpResponse.statusCode();

            final var body = httpResponse.body();
            return switch (statusCode) {
                case 400 -> throw badRequest("Bad request error received from %s: %s".formatted(request, bodyString(body)));
                case 401 -> throw unauthorized("Unauthorized error received from %s: %s".formatted(request, bodyString(body)));
                case 403 -> throw forbidden("Forbidden error when calling %s: %s".formatted(request, bodyString(body)));
                case 204, 404 -> Optional.empty();
                case 200, 201, 202 -> Optional.of(body);
                default -> throw internalServerError(format("Unknown error (status %d) when calling %s: %s", statusCode, request, bodyString(body)));
            };
        } catch (JsonProcessingException e) {
            throw internalServerError("Fail to serialize request", e);
        } catch (IOException | InterruptedException e) {
            throw internalServerError("Fail send request", e);
        }
    }

    private <T> T decode(byte[] content, Class<T> className) {
        try {
            return objectMapper.readValue(content, className);
        } catch (IOException e) {
            throw internalServerError("Fail to deserialize response", e);
        }
    }

    private <T> T decode(byte[] content, TypeReference<T> typeRef) {
        try {
            return objectMapper.readValue(content, typeRef);
        } catch (IOException e) {
            throw internalServerError("Fail to deserialize response", e);
        }
    }
    
    private static String bodyString(final byte[] body) {
        return body != null ? new String(body, StandardCharsets.UTF_8) : "null";
    }
}
