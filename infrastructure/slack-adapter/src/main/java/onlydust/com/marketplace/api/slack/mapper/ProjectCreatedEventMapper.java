package onlydust.com.marketplace.api.slack.mapper;

import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.model.User;

import static onlydust.com.marketplace.api.slack.mapper.FrontUrlHelper.getMarketplaceFrontendUrlFromEnvironment;

public interface ProjectCreatedEventMapper {

    String BLOCK = """
            [
            		{
            			"type": "section",
            			"text": {
            				"type": "mrkdwn",
            				"text": ":tada: Project *<%s|%s>* was created by *<%s|%s>* :tada:"
            			}
            		}
            	]""";

    static String mapToSlackBlock(User user, Project project, String environment) {
        final String marketplaceFrontendUrl = getMarketplaceFrontendUrlFromEnvironment(environment);
        final String publicProfileUrl = marketplaceFrontendUrl + "u/" + user.getGithubLogin();
        final String projectUrl = marketplaceFrontendUrl + "p/" + project.getSlug();
        return BLOCK.formatted(
                projectUrl, project.getName(),
                publicProfileUrl, user.getGithubLogin()
        );
    }
}
