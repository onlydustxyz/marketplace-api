package onlydust.com.marketplace.api.slack.mapper;

import onlydust.com.marketplace.project.domain.model.User;
import onlydust.com.marketplace.project.domain.view.ProjectDetailsView;

import static onlydust.com.marketplace.api.slack.mapper.UserAppliedOnProjectEventMapper.getMarketplaceFrontendUrlFromEnvironment;

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

    static String mapToSlackBlock(User user, ProjectDetailsView projectDetailsView, String environment) {
        final String marketplaceFrontendUrl = getMarketplaceFrontendUrlFromEnvironment(environment);
        final String publicProfileUrl = marketplaceFrontendUrl + "u/" + user.getGithubLogin();
        final String projectUrl = marketplaceFrontendUrl + "p/" + projectDetailsView.getSlug();
        return BLOCK.formatted(
                projectUrl, projectDetailsView.getName(),
                publicProfileUrl, user.getGithubLogin()
        );
    }
}
