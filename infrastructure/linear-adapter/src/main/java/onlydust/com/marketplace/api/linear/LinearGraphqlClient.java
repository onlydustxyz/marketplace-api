package onlydust.com.marketplace.api.linear;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

@AllArgsConstructor
public class LinearGraphqlClient {

  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;
  private final Config config;

  public <T> T decodeBody(byte[] data, Class<T> classType) {
    try {
      return objectMapper.readValue(data, classType);
    } catch (IOException e) {
      throw OnlyDustException.internalServerError("Unable to deserialize linear response", e);
    }
  }

  public <ResponseBody> Optional<ResponseBody> decodeResponse(HttpResponse<byte[]> response,
      Class<ResponseBody> responseClass) {
    return switch (response.statusCode()) {
      case 200, 201 -> Optional.of(decodeBody(response.body(), responseClass));
      case 403, 404, 422, 451 -> Optional.empty();
      default -> throw OnlyDustException.internalServerError("Received incorrect status (" + response.statusCode() + ") when fetching Linear API");
    };
  }

  private HttpResponse<byte[]> fetch(String method, URI uri, HttpRequest.BodyPublisher bodyPublisher) {
    try {
      final var request = HttpRequest.newBuilder()
          .uri(uri)
          .method(method, bodyPublisher)
          .header("Authorization", config.apiKey)
          .header("Content-Type", "application/json")
          .build();

      return httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
    } catch (IOException | InterruptedException e) {
      throw OnlyDustException.internalServerError("Unable to fetch Linear API", e);
    }
  }

  public <ResponseBody> Optional<ResponseBody> graphql(String query, Object variables,
      Class<ResponseBody> responseClass) {
    try {
      final var body = Map.of("query", query, "variables", variables);
      final var httpResponse = fetch("POST", URI.create(config.baseUri + "/graphql"),
          HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
      return decodeResponse(httpResponse, responseClass);
    } catch (JsonProcessingException e) {
      throw OnlyDustException.internalServerError("Unable to serialize graphql request body", e);
    }
  }

  @Data
  public static class Config {

    String baseUri;
    String apiKey;
    @Getter(AccessLevel.NONE)
    Map<Label.Keys, Label> labels;
    @Getter(AccessLevel.NONE)
    Map<Team.Keys, Team> teams;

    public Label label(Label.Keys key) {
      return labels.get(key);
    }

    public Team team(Team.Keys key) {
      return teams.get(key);
    }
  }

  @Data
  public static class Team {

    UUID id;
    @Getter(AccessLevel.NONE)
    Map<State.Keys, State> states;

    public State state(State.Keys key) {
      return states.get(key);
    }

    public enum Keys {Engineering}
  }

  @Data
  public static class State {

    UUID id;

    public enum Keys {Backlog}
  }

  @Data
  public static class Label {

    UUID id;

    public enum Keys {TechStuff}
  }
}
