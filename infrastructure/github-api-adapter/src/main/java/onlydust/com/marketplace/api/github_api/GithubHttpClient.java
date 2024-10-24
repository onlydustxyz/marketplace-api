package onlydust.com.marketplace.api.github_api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.forbidden;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

@Slf4j
@AllArgsConstructor
public class GithubHttpClient {
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final Config config;

    public <T> T decode(byte[] data, Class<T> classType) {
        try {
            return objectMapper.readValue(data, classType);
        } catch (IOException e) {
            throw internalServerError("Unable to deserialize github response", e);
        }
    }

    public HttpResponse<byte[]> fetch(final URI uri, final String personalAccessToken) {
        LOGGER.debug("Fetching {}", uri);
        try {
            final var requestBuilder = HttpRequest.newBuilder().uri(uri).headers("Authorization",
                    "Bearer " + personalAccessToken).GET();
            return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofByteArray());
        } catch (IOException | InterruptedException e) {
            throw internalServerError("Unable to fetch github API:" + uri, e);
        }
    }

    public <ResponseBody> Optional<ResponseBody> get(String path, Class<ResponseBody> responseClass) {
        return get(path, responseClass, null);
    }

    public <ResponseBody> Optional<ResponseBody> get(String path, Class<ResponseBody> responseClass,
                                                     final String personalAccessToken) {
        final var httpResponse = fetch(buildURI(path), isNull(personalAccessToken) ? config.personalAccessToken :
                personalAccessToken);
        return switch (httpResponse.statusCode()) {
            case 200 -> Optional.of(decode(httpResponse.body(), responseClass));
            case 403, 404 -> Optional.empty();
            default -> {
                final String errorMessage = bodyString(httpResponse.body());
                throw internalServerError("Unable to fetch github API: %s, response body: %s".formatted(path, errorMessage), null);
            }
        };
    }

    private static String bodyString(byte[] body) {
        return body != null ? new String(body, StandardCharsets.UTF_8) : "null";
    }


    public <ResponseBody, RequestBody> Optional<ResponseBody> post(String path, final RequestBody requestBody, Class<ResponseBody> responseClass) {
        return post(path, requestBody, config.personalAccessToken, responseClass);
    }


    public <ResponseBody> Optional<ResponseBody> post(String path, @NonNull String personalAccessToken, Class<ResponseBody> responseClass) {
        return post(path, HttpRequest.BodyPublishers.noBody(), personalAccessToken, responseClass);
    }

    public <ResponseBody, RequestBody> Optional<ResponseBody> post(String path, final RequestBody requestBody,
                                                                   @NonNull String personalAccessToken, Class<ResponseBody> responseClass) {
        try {
            return fetch(HttpMethod.POST, path, HttpRequest.BodyPublishers.ofByteArray(objectMapper.writeValueAsBytes(requestBody)), personalAccessToken,
                    responseClass);
        } catch (JsonProcessingException e) {
            throw internalServerError("Fail to serialize request", e);
        }
    }

    public <ResponseBody, RequestBody> Optional<ResponseBody> patch(String path, final RequestBody requestBody,
                                                                    @NonNull String personalAccessToken, Class<ResponseBody> responseClass) {
        try {
            return fetch(HttpMethod.PATCH, path, HttpRequest.BodyPublishers.ofByteArray(objectMapper.writeValueAsBytes(requestBody)), personalAccessToken,
                    responseClass);
        } catch (JsonProcessingException e) {
            throw internalServerError("Fail to serialize request", e);
        }
    }

    public <ResponseBody, RequestBody> Optional<ResponseBody> delete(String path, final RequestBody requestBody,
                                                                     @NonNull String personalAccessToken, Class<ResponseBody> responseClass) {
        try {
            return fetch(HttpMethod.DELETE, path, HttpRequest.BodyPublishers.ofByteArray(objectMapper.writeValueAsBytes(requestBody)), personalAccessToken,
                    responseClass);
        } catch (JsonProcessingException e) {
            throw internalServerError("Fail to serialize request", e);
        }
    }


    public <ResponseBody> Optional<ResponseBody> delete(String path, @NonNull String personalAccessToken, Class<ResponseBody> responseClass) {
        return fetch(HttpMethod.DELETE, path, HttpRequest.BodyPublishers.noBody(), personalAccessToken, responseClass);
    }

    public <ResponseBody> Optional<ResponseBody> fetch(HttpMethod method, String path, final HttpRequest.BodyPublisher bodyPublisher,
                                                       @NonNull String personalAccessToken, Class<ResponseBody> responseClass) {
        try {
            final HttpResponse<byte[]> httpResponse =
                    httpClient.send(HttpRequest.newBuilder().uri(buildURI(path))
                            .headers("Authorization", "Bearer " + personalAccessToken)
                            .method(method.name(), bodyPublisher)
                            .build(), HttpResponse.BodyHandlers.ofByteArray());
            return switch (httpResponse.statusCode()) {
                case 200, 201, 204, 206 -> isNull(httpResponse.body()) || httpResponse.body().length == 0 ? Optional.empty() :
                        Optional.ofNullable(decode(httpResponse.body(), responseClass));
                case 301, 302, 307, 308 -> {
                    final var location = httpResponse.headers().firstValue("Location")
                            .orElseThrow(() -> internalServerError("%d status received without Location header".formatted(httpResponse.statusCode())));
                    yield fetch(method, URI.create(location).getPath(), bodyPublisher, personalAccessToken, responseClass);
                }
                case 404 -> Optional.empty();
                case 401, 403 ->
                        throw forbidden("Unauthorized access to Github API: %s for error message %s".formatted(path, deserializeErrorMessage(httpResponse)));
                default ->
                        throw internalServerError("Unable to fetch github API: %s for error message %s".formatted(path, deserializeErrorMessage(httpResponse)));
            };
        } catch (IOException | InterruptedException e) {
            throw internalServerError("Fail send request", e);
        }
    }

    private String deserializeErrorMessage(final HttpResponse<byte[]> httpResponse) {
        try {
            return decode(httpResponse.body(), JsonNode.class).toString();
        } catch (Exception e) {
            LOGGER.error("Error while deserializing error message from Github response");
            return "No error message from Github";
        }
    }

    public final URI buildURI(String path) {
        String baseUri = config.baseUri;
        if (config.baseUri.endsWith("/")) {
            baseUri = config.baseUri.substring(0, config.baseUri.length() - 1);
        }
        return URI.create(baseUri + path);
    }

    @ToString
    @Data
    public static class Config {
        private String baseUri;
        private String personalAccessToken;
    }
}
