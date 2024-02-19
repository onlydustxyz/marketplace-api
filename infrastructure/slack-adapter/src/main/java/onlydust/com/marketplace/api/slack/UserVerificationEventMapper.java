package onlydust.com.marketplace.api.slack;

import onlydust.com.marketplace.api.domain.model.VerificationStatus;
import onlydust.com.marketplace.api.domain.model.notification.BillingProfileUpdated;

import java.util.List;

import static java.util.Objects.nonNull;

public interface UserVerificationEventMapper {
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
                    				"text": "*Type:* %s\\n*New status:* %s\\n*BillingProfileId :* %s\\n*Sumsub raw review details :*\\n```%s```"
                    			}
                    		}
                    	]""";


    static String billingProfileUpdatedToSlackNotification(final BillingProfileUpdated billingProfileUpdated, final String environment) {
        String mainMessage = "*New %s event : <%s|Check on Sumsub>*".formatted(switch (billingProfileUpdated.getType()) {
                    case COMPANY -> "KYB";
                    case INDIVIDUAL -> "KYC";
                },
                "https://cockpit.sumsub.com/checkus/#/applicant/%s/basicInfo?clientId=onlydust".formatted(billingProfileUpdated.getExternalApplicantId()));
        // Enabling notifications pinging all the channel only for closed KYC/KYB on production
        // NB : even if statements on env are bad practices -_-
        if (billingProfileUpdated.getVerificationStatus().equals(VerificationStatus.CLOSED) && environment.equals("production")) {
            mainMessage = "<!channel> " + mainMessage;
        }
        return SLACK_BLOCKS_TEMPLATE.formatted(
                mainMessage,
                nonNull(billingProfileUpdated.getUserId()) ? billingProfileUpdated.getUserId().toString() : null,
                billingProfileUpdated.getGithubLogin(),
                nonNull(billingProfileUpdated.getGithubUserId()) ? billingProfileUpdated.getGithubUserId().toString() : null,
                billingProfileUpdated.getGithubUserEmail(),
                billingProfileUpdated.getGithubAvatarUrl(),
                nonNull(billingProfileUpdated.getType()) ? billingProfileUpdated.getType().toString() : null,
                nonNull(billingProfileUpdated.getVerificationStatus()) ? billingProfileUpdated.getVerificationStatus().toString() : null,
                nonNull(billingProfileUpdated.getBillingProfileId()) ? billingProfileUpdated.getBillingProfileId().toString() : null,
                nonNull(billingProfileUpdated.getRawReviewDetails()) ? billingProfileUpdated.getRawReviewDetails().replace("\"", "\\\"") : null
        );
    }
}
