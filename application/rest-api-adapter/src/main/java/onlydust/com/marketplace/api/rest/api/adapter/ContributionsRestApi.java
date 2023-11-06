package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ContributionsApi;
import onlydust.com.marketplace.api.contract.model.ContributionDetailsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tags(@Tag(name = "Me"))
@AllArgsConstructor
public class ContributionsRestApi implements ContributionsApi {
    @Override
    public ResponseEntity<ContributionDetailsResponse> getContribution(String contributionId) {
        return ResponseEntity.notFound().build();
    }
}
