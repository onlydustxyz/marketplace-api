package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ContributionsApi;
import onlydust.com.marketplace.api.contract.model.ContributionDetailsResponse;
import onlydust.com.marketplace.api.domain.port.input.ContributionFacadePort;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.ContributionMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tags(@Tag(name = "Me"))
@AllArgsConstructor
public class ContributionsRestApi implements ContributionsApi {
    private final ContributionFacadePort contributionsFacadePort;

    @Override
    public ResponseEntity<ContributionDetailsResponse> getContribution(String contributionId) {
        return contributionsFacadePort.getContribution(contributionId)
                .map(contribution -> ResponseEntity.ok(ContributionMapper.mapContributionDetails(contribution)))
                .orElse(ResponseEntity.notFound().build());
    }
}
