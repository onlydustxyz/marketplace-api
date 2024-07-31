package onlydust.com.marketplace.api.slack.mapper;

import onlydust.com.marketplace.accounting.domain.events.BillingProfileVerificationUpdated;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.project.domain.model.User;

import static java.util.Objects.nonNull;

public interface BillingProfileVerificationEventMapper {
    // Build blocks with https://app.slack.com/block-kit-builder/
    String SLACK_BLOCKS_TEMPLATE = """
            [
                    		{
                    			"type": "section",
                    			"text": {
                    				"type": "mrkdwn",
                    				"text": "%s"
                    			}
                    		},
                    		{
                    			"type": "divider"
                    		},
                    		{
                    			"type": "section",
                    			"text": {
                    				"type": "mrkdwn",
                    				"text": "*User id:* %s\\n*Github login:* %s\\n*Github user id*: %s\\n*Github email:* %s\\n"
                    			},
                    			"accessory": {
                    				"type": "image",
                    				"image_url": "%s",
                    				"alt_text": "computer thumbnail"
                    			}
                    		},
                    		{
                    			"type": "divider"
                    		},
                    		{
                    			"type": "section",
                    			"text": {
                    				"type": "mrkdwn",
                    				"text": "*Type:* %s\\n*New status:* %s\\n*VerificationId :* %s\\n*Sumsub raw review details :*\\n```%s```"
                    			}
                    		}
                    	]""";


    static String mapToSlackBlock(final BillingProfileVerificationUpdated billingProfileVerificationUpdated,
                                  final User user,
                                  final Boolean tagAllChannel) {
        String mainMessage = "*New %s event : <%s|Check on Sumsub>*".formatted(billingProfileVerificationUpdated.getType().name(),
                "https://cockpit.sumsub.com/checkus/#/applicant/%s/basicInfo?clientId=onlydust".formatted(billingProfileVerificationUpdated.getExternalApplicantId()));
        // Enabling notifications pinging all the channel only for closed KYC/KYB
        if (billingProfileVerificationUpdated.getVerificationStatus().equals(VerificationStatus.CLOSED) && tagAllChannel) {
            mainMessage = "<!channel> " + mainMessage;
        }
        return SLACK_BLOCKS_TEMPLATE.formatted(
                mainMessage,
                nonNull(billingProfileVerificationUpdated.getUserId()) ? billingProfileVerificationUpdated.getUserId().toString() : null,
                user.getGithubLogin(),
                nonNull(user.getGithubUserId()) ? user.getGithubUserId().toString() : null,
                user.getEmail(),
                user.getGithubAvatarUrl(),
                nonNull(billingProfileVerificationUpdated.getType()) ? billingProfileVerificationUpdated.getType().toString() : null,
                nonNull(billingProfileVerificationUpdated.getVerificationStatus()) ?
                        billingProfileVerificationUpdated.getVerificationStatus().name() : null,
                nonNull(billingProfileVerificationUpdated.getVerificationId()) ? billingProfileVerificationUpdated.getVerificationId().toString() : null,
                nonNull(billingProfileVerificationUpdated.getRawReviewDetails()) ? billingProfileVerificationUpdated.getRawReviewDetails().replace("\"",
                        "\\\"") : null
        );
    }
}
