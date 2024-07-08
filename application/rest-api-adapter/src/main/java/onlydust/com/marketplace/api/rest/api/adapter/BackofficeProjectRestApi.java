package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import lombok.Data;
import onlydust.com.backoffice.api.contract.BackofficeProjectApi;
import onlydust.com.backoffice.api.contract.model.RewardContributorRequest;
import onlydust.com.marketplace.project.domain.port.input.AutomatedRewardFacadePort;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Tags(@Tag(name = "BackofficeProject"))
@AllArgsConstructor
@Profile("bo")
public class BackofficeProjectRestApi implements BackofficeProjectApi {

    private final AutomatedRewardFacadePort automatedRewardFacadePort;
    private final OnlydustBotProperties onlydustBotProperties;

    @Override
    public ResponseEntity<Void> rewardContributor(String projectSlug, RewardContributorRequest rewardContributorRequest) {
        automatedRewardFacadePort.createOtherWorkAndReward(projectSlug, onlydustBotProperties.getProjectLeadId(), rewardContributorRequest.getRepositoryName(),
                rewardContributorRequest.getReason(), rewardContributorRequest.getRecipientGithubLogin(), rewardContributorRequest.getCurrencyCode(),
                rewardContributorRequest.getAmount());
        return ResponseEntity.noContent().build();
    }

    @Data
    public static class OnlydustBotProperties {
        UUID projectLeadId;
    }
}
