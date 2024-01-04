package onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class HasuraJwtPayloadTest {

  @Test
  void should_deserialize_claims_without_login() throws JsonProcessingException {
    final String json = """
        {
            "x-hasura-projectsLeaded": "{}",
            "x-hasura-githubUserId": "595505",
            "x-hasura-odAdmin": "false",
            "x-hasura-githubAccessToken": "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",
            "x-hasura-allowed-roles": [
              "me",
              "registered_user",
              "public"
            ],
            "x-hasura-default-role": "registered_user",
            "x-hasura-user-id": "50aa4318-141a-4027-8f74-c135d8d166b0",
            "x-hasura-user-is-anonymous": "false"
        }
        """;

    HasuraJwtPayload.HasuraClaims claims = new ObjectMapper().readValue(json, HasuraJwtPayload.HasuraClaims.class);

    assertThat(claims).isNotNull();
    assertThat(claims.getLogin()).isNull();
    assertThat(claims.getAvatarUrl()).isNull();
  }

  @Test
  void should_deserialize_claims_with_login() throws JsonProcessingException {
    final String json = """
        {
            "x-hasura-projectsLeaded": "{}",
            "x-hasura-githubUserId": "595505",
            "x-hasura-odAdmin": "false",
            "x-hasura-githubAccessToken": "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",
            "x-hasura-allowed-roles": [
              "me",
              "registered_user",
              "public"
            ],
            "x-hasura-default-role": "registered_user",
            "x-hasura-user-id": "50aa4318-141a-4027-8f74-c135d8d166b0",
            "x-hasura-user-is-anonymous": "false",
            "x-hasura-login": "foo",
            "x-hasura-avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4"
        }
        """;

    HasuraJwtPayload.HasuraClaims claims = new ObjectMapper().readValue(json, HasuraJwtPayload.HasuraClaims.class);

    assertThat(claims).isNotNull();
    assertThat(claims.getLogin()).isEqualTo("foo");
    assertThat(claims.getAvatarUrl()).isEqualTo("https://avatars.githubusercontent.com/u/595505?v=4");
  }
}