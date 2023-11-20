package onlydust.com.marketplace.api.bootstrap.it;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;


@Slf4j
public class AbstractMarketplaceApiIT {

    protected static final Faker faker = new Faker();
    protected static final String PROJECTS_GET_CONTRIBUTION_BY_ID = "/api/v1/projects/%s/contributions/%s";
    protected static final String PROJECTS_GET_BY_ID = "/api/v1/projects";
    protected static final String PROJECTS_GET_BY_SLUG = "/api/v1/projects/slug";
    protected static final String PROJECTS_GET = "/api/v1/projects";
    protected static final String USERS_SEARCH_CONTRIBUTORS = "/api/v1/users/search";
    protected static final String PROJECTS_GET_CONTRIBUTORS = "/api/v1/projects/%s/contributors";
    protected static final String PROJECTS_REWARDS = "/api/v1/projects/%s/rewards";
    protected static final String PROJECTS_REWARD = "/api/v1/projects/%s/rewards/%s";
    protected static final String PROJECTS_GET_REWARD_ITEMS = "/api/v1/projects/%s/rewards/%s/reward-items";
    protected static final String PROJECTS_GET_REWARDABLE_ITEMS = "/api/v1/projects/%s/rewardable-items";
    protected static final String PROJECTS_POST_REWARDABLE_OTHER_WORK = "/api/v1/projects/%s/rewardable-items/other" +
                                                                        "-works";
    protected static final String PROJECTS_POST_REWARDABLE_OTHER_ISSUE = "/api/v1/projects/%s/rewardable-items/other" +
                                                                         "-issues";
    protected static final String PROJECTS_POST_REWARDABLE_OTHER_PR = "/api/v1/projects/%s/rewardable-items/other" +
                                                                      "-pull-requests";
    protected static final String PROJECTS_GET_BUDGETS = "/api/v1/projects/%s/budgets";
    protected static final String PROJECTS_POST = "/api/v1/projects";
    protected static final String PROJECTS_PUT = "/api/v1/projects/%s";
    protected static final String PROJECTS_IGNORED_CONTRIBUTIONS_PUT = "/api/v1/projects/%s/ignored-contributions";
    protected static final String ME_GET = "/api/v1/me";
    protected static final String ME_PATCH = "/api/v1/me";
    protected static final String ME_GET_PROFILE = "/api/v1/me/profile";
    protected static final String ME_PUT_PROFILE = "/api/v1/me/profile";
    protected static final String ME_PAYOUT_INFO = "/api/v1/me/payout-info";
    protected static final String ME_ACCEPT_PROJECT_LEADER_INVITATION = "/api/v1/me/project-leader-invitations/%s";
    protected static final String ME_APPLY_TO_PROJECT = "/api/v1/me/applications";
    protected static final String ME_GET_REWARDS = "/api/v1/me/rewards";
    protected static final String ME_GET_CONTRIBUTIONS = "/api/v1/me/contributions";
    protected static final String ME_GET_CONTRIBUTED_PROJECTS = "/api/v1/me/contributed-projects";
    protected static final String ME_GET_CONTRIBUTED_REPOS = "/api/v1/me/contributed-repos";
    protected static final String ME_GET_REWARD_TOTAL_AMOUNTS = "/api/v1/me/rewards/amounts";
    protected static final String ME_REWARDS_PENDING_INVOICE = "/api/v1/me/rewards/pending-invoice";
    protected static final String ME_REWARD = "/api/v1/me/rewards/%s";
    protected static final String ME_REWARD_ITEMS = "/api/v1/me/rewards/%s/reward-items";
    protected static final String USERS_GET = "/api/v1/users";
    protected static final String USERS_GET_BY_LOGIN = "/api/v1/users/login";
    protected static final String GITHUB_INSTALLATIONS_GET = "/api/v1/github/installations";
    protected static final String ME_GET_ORGANIZATIONS = "/api/v1/me/organizations";
    protected static final String EVENT_ON_CONTRIBUTIONS_CHANGE_POST = "/api/v1/events/on-contributions-change";

    protected URI getApiURI(final int port, final String path) {
        return UriComponentsBuilder.newInstance()
                .scheme("http")
                .host("localhost")
                .port(port)
                .path(path)
                .build()
                .toUri();
    }

    protected URI getApiURI(final int port, final String path, String paramName, String paramValue) {
        return UriComponentsBuilder.newInstance()
                .scheme("http")
                .host("localhost")
                .port(port)
                .path(path)
                .queryParam(paramName, paramValue)
                .build()
                .toUri();
    }

    protected URI getApiURI(final int port, final String path, final Map<String, String> params) {
        final UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host("localhost")
                .port(port)
                .path(path);
        params.forEach(uriComponentsBuilder::queryParam);
        return uriComponentsBuilder
                .build()
                .toUri();
    }

}
