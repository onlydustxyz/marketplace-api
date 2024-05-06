package onlydust.com.marketplace.api.slack.mapper;

import onlydust.com.marketplace.project.domain.model.User;
import onlydust.com.marketplace.project.domain.view.ProjectDetailsView;

import static onlydust.com.marketplace.api.slack.mapper.FrontUrlHelper.getMarketplaceFrontendUrlFromEnvironment;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

public interface UserAppliedOnProjectEventMapper {

    String BLOCK = """
            [{
            			"type": "section",
            			"text": {
            				"type": "mrkdwn",
            				"text": ":star: *<%s|%s>* is interested by project *<%s|%s>* :star:"
            			}
            		}]""";


    static String mapToSlackBlock(User user, ProjectDetailsView projectDetailsView, String environment) {
        final String marketplaceFrontendUrl = getMarketplaceFrontendUrlFromEnvironment(environment);
        final String publicProfileUrl = marketplaceFrontendUrl + "u/" + user.getGithubLogin();
        final String projectUrl = marketplaceFrontendUrl + "p/" + projectDetailsView.getSlug();
        return BLOCK.formatted(
                publicProfileUrl, user.getGithubLogin(),
                projectUrl, projectDetailsView.getName()
        );
    }


}
