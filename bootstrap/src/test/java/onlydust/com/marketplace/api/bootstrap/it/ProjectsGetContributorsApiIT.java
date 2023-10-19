package onlydust.com.marketplace.api.bootstrap.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import onlydust.com.marketplace.api.bootstrap.helper.HasuraJwtHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.AuthUserEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.AuthUserRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura.HasuraJwtPayload;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt.JwtSecret;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

@ActiveProfiles({"hasura_auth"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectsGetContributorsApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    AuthUserRepository authUserRepository;
    @Autowired
    JwtSecret jwtSecret;

    @Test
    void should_find_project_contributors_as_anonymous_user() {
        // Given
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_CONTRIBUTORS, projectId),
                        Map.of("page_index", "0", "page_size", "10000", "sort", "CONTRIBUTION_COUNT")))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(GET_PROJECT_CONTRIBUTORS_ANONYMOUS);
    }


    @Test
    void should_find_project_with_pagination() {
        // Given
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_CONTRIBUTORS, projectId),
                        Map.of("page_index", "0", "page_size", "3", "sort", "CONTRIBUTION_COUNT")))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(GET_PROJECT_CONTRIBUTORS_PAGE_0);

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_CONTRIBUTORS, projectId),
                        Map.of("page_index", "1", "page_size", "3", "sort", "CONTRIBUTION_COUNT")))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(GET_PROJECT_CONTRIBUTORS_PAGE_1);
    }

    @Test
    void should_find_project_contributors_as_project_lead() throws JsonProcessingException {
        // Given
        final AuthUserEntity pierre = authUserRepository.findByGithubUserId(16590657L).orElseThrow();
        final String jwt = HasuraJwtHelper.generateValidJwtFor(jwtSecret, HasuraJwtPayload.builder()
                .iss(jwtSecret.getIssuer())
                .claims(HasuraJwtPayload.HasuraClaims.builder()
                        .userId(pierre.getId())
                        .allowedRoles(List.of("me"))
                        .githubUserId(pierre.getGithubUserId())
                        .avatarUrl(pierre.getAvatarUrlAtSignup())
                        .login(pierre.getLoginAtSignup())
                        .build())
                .build());
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_CONTRIBUTORS, projectId),
                        Map.of("page_index", "0", "page_size", "10000", "sort", "CONTRIBUTION_COUNT")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(GET_PROJECT_CONTRIBUTORS_PROJECT_LEAD);
    }


    private static final String GET_PROJECT_CONTRIBUTORS_PAGE_0 = """
            {
                "totalPageNumber": 6,
                "totalItemNumber": 17,
                "hasMore": true,
                "contributors": [
                    {
                        "githubUserId": 43467246,
                        "login": "AnthonyBuisset",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                        "contributionCount": 853,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": null,
                        "pullRequestToReward": null,
                        "issueToReward": null,
                        "codeReviewToReward": null
                    },
                    {
                        "githubUserId": 595505,
                        "login": "ofux",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4",
                        "contributionCount": 537,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": null,
                        "pullRequestToReward": null,
                        "issueToReward": null,
                        "codeReviewToReward": null
                    },
                    {
                        "githubUserId": 4435377,
                        "login": "Bernardstanislas",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4",
                        "contributionCount": 363,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": null,
                        "pullRequestToReward": null,
                        "issueToReward": null,
                        "codeReviewToReward": null
                    }
                ]
            }""";

    private static final String GET_PROJECT_CONTRIBUTORS_PAGE_1 = """
            {
                "totalPageNumber": 6,
                "totalItemNumber": 17,
                "hasMore": true,
                "contributors": [
                    {
                        "githubUserId": 21149076,
                        "login": "oscarwroche",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                        "contributionCount": 211,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": null,
                        "pullRequestToReward": null,
                        "issueToReward": null,
                        "codeReviewToReward": null
                    },
                    {
                        "githubUserId": 34384633,
                        "login": "tdelabro",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/34384633?v=4",
                        "contributionCount": 143,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": null,
                        "pullRequestToReward": null,
                        "issueToReward": null,
                        "codeReviewToReward": null
                    },
                    {
                        "githubUserId": 16590657,
                        "login": "PierreOucif",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                        "contributionCount": 92,
                        "rewardCount": 6,
                        "earned": 6000,
                        "contributionToRewardCount": null,
                        "pullRequestToReward": null,
                        "issueToReward": null,
                        "codeReviewToReward": null
                    }
                ]
            }""";

    private static final String GET_PROJECT_CONTRIBUTORS_PROJECT_LEAD = """
            {
                "totalPageNumber": 1,
                "totalItemNumber": 17,
                "hasMore": false,
                "contributors": [
                    {
                        "githubUserId": 43467246,
                        "login": "AnthonyBuisset",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                        "contributionCount": 853,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": 762,
                        "pullRequestToReward": 396,
                        "issueToReward": 11,
                        "codeReviewToReward": 355
                    },
                    {
                        "githubUserId": 595505,
                        "login": "ofux",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4",
                        "contributionCount": 537,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": 531,
                        "pullRequestToReward": 215,
                        "issueToReward": 2,
                        "codeReviewToReward": 314
                    },
                    {
                        "githubUserId": 4435377,
                        "login": "Bernardstanislas",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4",
                        "contributionCount": 363,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": 360,
                        "pullRequestToReward": 105,
                        "issueToReward": 0,
                        "codeReviewToReward": 255
                    },
                    {
                        "githubUserId": 21149076,
                        "login": "oscarwroche",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                        "contributionCount": 211,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": 149,
                        "pullRequestToReward": 53,
                        "issueToReward": 0,
                        "codeReviewToReward": 96
                    },
                    {
                        "githubUserId": 34384633,
                        "login": "tdelabro",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/34384633?v=4",
                        "contributionCount": 143,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": 142,
                        "pullRequestToReward": 104,
                        "issueToReward": 0,
                        "codeReviewToReward": 38
                    },
                    {
                        "githubUserId": 16590657,
                        "login": "PierreOucif",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                        "contributionCount": 92,
                        "rewardCount": 6,
                        "earned": 6000,
                        "contributionToRewardCount": 72,
                        "pullRequestToReward": 1,
                        "issueToReward": 0,
                        "codeReviewToReward": 71
                    },
                    {
                        "githubUserId": 10922658,
                        "login": "alexbensimon",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/10922658?v=4",
                        "contributionCount": 44,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": 42,
                        "pullRequestToReward": 30,
                        "issueToReward": 0,
                        "codeReviewToReward": 12
                    },
                    {
                        "githubUserId": 31901905,
                        "login": "kaelsky",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                        "contributionCount": 42,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": 41,
                        "pullRequestToReward": 34,
                        "issueToReward": 0,
                        "codeReviewToReward": 7
                    },
                    {
                        "githubUserId": 10167015,
                        "login": "lechinoix",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/10167015?v=4",
                        "contributionCount": 36,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": 36,
                        "pullRequestToReward": 25,
                        "issueToReward": 0,
                        "codeReviewToReward": 11
                    },
                    {
                        "githubUserId": 5160414,
                        "login": "haydencleary",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4",
                        "contributionCount": 27,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": 27,
                        "pullRequestToReward": 7,
                        "issueToReward": 0,
                        "codeReviewToReward": 20
                    },
                    {
                        "githubUserId": 45264458,
                        "login": "abdelhamidbakhta",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/45264458?v=4",
                        "contributionCount": 21,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": 21,
                        "pullRequestToReward": 8,
                        "issueToReward": 0,
                        "codeReviewToReward": 13
                    },
                    {
                        "githubUserId": 98529704,
                        "login": "tekkac",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/98529704?v=4",
                        "contributionCount": 19,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": 19,
                        "pullRequestToReward": 6,
                        "issueToReward": 1,
                        "codeReviewToReward": 12
                    },
                    {
                        "githubUserId": 143011364,
                        "login": "pixelfact",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/143011364?v=4",
                        "contributionCount": 5,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": 5,
                        "pullRequestToReward": 2,
                        "issueToReward": 0,
                        "codeReviewToReward": 3
                    },
                    {
                        "githubUserId": 698957,
                        "login": "ltoussaint",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/698957?v=4",
                        "contributionCount": 2,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": 2,
                        "pullRequestToReward": 0,
                        "issueToReward": 0,
                        "codeReviewToReward": 2
                    },
                    {
                        "githubUserId": 112474158,
                        "login": "onlydust-contributor",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/112474158?v=4",
                        "contributionCount": 2,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": 2,
                        "pullRequestToReward": 1,
                        "issueToReward": 0,
                        "codeReviewToReward": 1
                    },
                    {
                        "githubUserId": 102823832,
                        "login": "SamuelKer",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/102823832?v=4",
                        "contributionCount": 1,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": 1,
                        "pullRequestToReward": 1,
                        "issueToReward": 0,
                        "codeReviewToReward": 0
                    },
                    {
                        "githubUserId": 129528947,
                        "login": "VeryDustyBot",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/129528947?v=4",
                        "contributionCount": 1,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": 1,
                        "pullRequestToReward": 0,
                        "issueToReward": 0,
                        "codeReviewToReward": 1
                    }
                ]
            }
            """;

    private static final String GET_PROJECT_CONTRIBUTORS_ANONYMOUS = """
            {
                "totalPageNumber": 1,
                "totalItemNumber": 17,
                "hasMore": false,
                "contributors": [
                    {
                        "githubUserId": 43467246,
                        "login": "AnthonyBuisset",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                        "contributionCount": 853,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": null,
                        "pullRequestToReward": null,
                        "issueToReward": null,
                        "codeReviewToReward": null
                    },
                    {
                        "githubUserId": 595505,
                        "login": "ofux",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4",
                        "contributionCount": 537,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": null,
                        "pullRequestToReward": null,
                        "issueToReward": null,
                        "codeReviewToReward": null
                    },
                    {
                        "githubUserId": 4435377,
                        "login": "Bernardstanislas",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4",
                        "contributionCount": 363,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": null,
                        "pullRequestToReward": null,
                        "issueToReward": null,
                        "codeReviewToReward": null
                    },
                    {
                        "githubUserId": 21149076,
                        "login": "oscarwroche",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                        "contributionCount": 211,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": null,
                        "pullRequestToReward": null,
                        "issueToReward": null,
                        "codeReviewToReward": null
                    },
                    {
                        "githubUserId": 34384633,
                        "login": "tdelabro",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/34384633?v=4",
                        "contributionCount": 143,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": null,
                        "pullRequestToReward": null,
                        "issueToReward": null,
                        "codeReviewToReward": null
                    },
                    {
                        "githubUserId": 16590657,
                        "login": "PierreOucif",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                        "contributionCount": 92,
                        "rewardCount": 6,
                        "earned": 6000,
                        "contributionToRewardCount": null,
                        "pullRequestToReward": null,
                        "issueToReward": null,
                        "codeReviewToReward": null
                    },
                    {
                        "githubUserId": 10922658,
                        "login": "alexbensimon",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/10922658?v=4",
                        "contributionCount": 44,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": null,
                        "pullRequestToReward": null,
                        "issueToReward": null,
                        "codeReviewToReward": null
                    },
                    {
                        "githubUserId": 31901905,
                        "login": "kaelsky",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                        "contributionCount": 42,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": null,
                        "pullRequestToReward": null,
                        "issueToReward": null,
                        "codeReviewToReward": null
                    },
                    {
                        "githubUserId": 10167015,
                        "login": "lechinoix",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/10167015?v=4",
                        "contributionCount": 36,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": null,
                        "pullRequestToReward": null,
                        "issueToReward": null,
                        "codeReviewToReward": null
                    },
                    {
                        "githubUserId": 5160414,
                        "login": "haydencleary",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4",
                        "contributionCount": 27,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": null,
                        "pullRequestToReward": null,
                        "issueToReward": null,
                        "codeReviewToReward": null
                    },
                    {
                        "githubUserId": 45264458,
                        "login": "abdelhamidbakhta",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/45264458?v=4",
                        "contributionCount": 21,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": null,
                        "pullRequestToReward": null,
                        "issueToReward": null,
                        "codeReviewToReward": null
                    },
                    {
                        "githubUserId": 98529704,
                        "login": "tekkac",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/98529704?v=4",
                        "contributionCount": 19,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": null,
                        "pullRequestToReward": null,
                        "issueToReward": null,
                        "codeReviewToReward": null
                    },
                    {
                        "githubUserId": 143011364,
                        "login": "pixelfact",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/143011364?v=4",
                        "contributionCount": 5,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": null,
                        "pullRequestToReward": null,
                        "issueToReward": null,
                        "codeReviewToReward": null
                    },
                    {
                        "githubUserId": 698957,
                        "login": "ltoussaint",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/698957?v=4",
                        "contributionCount": 2,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": null,
                        "pullRequestToReward": null,
                        "issueToReward": null,
                        "codeReviewToReward": null
                    },
                    {
                        "githubUserId": 112474158,
                        "login": "onlydust-contributor",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/112474158?v=4",
                        "contributionCount": 2,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": null,
                        "pullRequestToReward": null,
                        "issueToReward": null,
                        "codeReviewToReward": null
                    },
                    {
                        "githubUserId": 102823832,
                        "login": "SamuelKer",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/102823832?v=4",
                        "contributionCount": 1,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": null,
                        "pullRequestToReward": null,
                        "issueToReward": null,
                        "codeReviewToReward": null
                    },
                    {
                        "githubUserId": 129528947,
                        "login": "VeryDustyBot",
                        "avatarUrl": "https://avatars.githubusercontent.com/u/129528947?v=4",
                        "contributionCount": 1,
                        "rewardCount": 0,
                        "earned": null,
                        "contributionToRewardCount": null,
                        "pullRequestToReward": null,
                        "issueToReward": null,
                        "codeReviewToReward": null
                    }
                ]
            }
            """;
}
