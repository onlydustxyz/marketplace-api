package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ContributionsApi;
import onlydust.com.marketplace.api.contract.model.ContributionPatchRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tags(@Tag(name = "Contributions"))
@AllArgsConstructor
@Profile("api")
public class ContributionsRestApi implements ContributionsApi {
    @Override
    public ResponseEntity<Void> patchContribution(String contributionId, ContributionPatchRequest contributionPatchRequest) {
        // TODO
        return ResponseEntity.noContent().build();
    }
}
