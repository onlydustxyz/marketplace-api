package onlydust.com.marketplace.api.slack.mapper;

import onlydust.com.marketplace.project.domain.model.Project;

import static onlydust.com.marketplace.api.slack.mapper.FrontUrlHelper.getMarketplaceFrontendUrlFromEnvironment;

public interface UserAppliedOnProjectEventMapper {

    String BLOCK = """
            [{
            			"type": "section",
            			"text": {
            				"type": "mrkdwn",
            				"text": ":star: *<%s|%s>* is interested by project *<%s|%s>* :star:"
            			}
            		}]""";


    static String mapToSlackBlock(String userLogin, Project project, String environment) {
        final String marketplaceFrontendUrl = getMarketplaceFrontendUrlFromEnvironment(environment);
        final String publicProfileUrl = marketplaceFrontendUrl + "u/" + userLogin;
        final String projectUrl = marketplaceFrontendUrl + "p/" + project.getSlug();
        return BLOCK.formatted(
                publicProfileUrl, userLogin,
                projectUrl, project.getName()
        );
    }


}
