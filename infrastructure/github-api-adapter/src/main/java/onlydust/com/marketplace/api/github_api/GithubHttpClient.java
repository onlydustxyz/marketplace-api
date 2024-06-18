package onlydust.com.marketplace.api.github_api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Optional;

import static java.util.Objects.isNull;
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
            default -> throw internalServerError("Unable to fetch github API: " + path, null);
        };
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
            return post(path, HttpRequest.BodyPublishers.ofByteArray(objectMapper.writeValueAsBytes(requestBody)), personalAccessToken, responseClass);
        } catch (JsonProcessingException e) {
            throw internalServerError("Fail to serialize request", e);
        }
    }

    public <ResponseBody> Optional<ResponseBody> post(String path, final HttpRequest.BodyPublisher bodyPublisher,
                                                      @NonNull String personalAccessToken, Class<ResponseBody> responseClass) {
        try {
            final HttpResponse<byte[]> httpResponse =
                    httpClient.send(HttpRequest.newBuilder().uri(buildURI(path))
                            .headers("Authorization", "Bearer " + personalAccessToken)
                            .POST(bodyPublisher)
                            .build(), HttpResponse.BodyHandlers.ofByteArray());
            return switch (httpResponse.statusCode()) {
                case 200, 201, 204, 206 -> Optional.of(decode(httpResponse.body(), responseClass));
                case 301, 302, 307, 308 -> {
                    final var location = httpResponse.headers().firstValue("Location")
                            .orElseThrow(() -> internalServerError("%d status received without Location header".formatted(httpResponse.statusCode())));
                    yield post(URI.create(location).getPath(), bodyPublisher, personalAccessToken, responseClass);
                }
                case 403, 404 ->
                    // Should be considered as an internal server error because it happens due to wrong github PAT or
                    // rate limiting exceeded,
                    // but never due to a user action
                        throw internalServerError(deserializeErrorMessage(httpResponse));
                default -> throw internalServerError("Unable to fetch github API: " + path + " for " +
                                                     "error message " + deserializeErrorMessage(httpResponse), null);
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
