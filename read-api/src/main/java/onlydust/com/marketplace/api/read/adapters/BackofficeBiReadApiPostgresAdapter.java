package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeBiReadApi;
import onlydust.com.backoffice.api.contract.model.BOBiContributorListResponse;
import onlydust.com.marketplace.api.read.entities.bi.BOContributorBiReadEntity;
import onlydust.com.marketplace.api.read.repositories.BOContributorBiReadRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("bo")
public class BackofficeBiReadApiPostgresAdapter implements BackofficeBiReadApi {
    private final BOContributorBiReadRepository boContributorBiReadRepository;

    @Override
    public ResponseEntity<BOBiContributorListResponse> getBOBiContributors(List<String> contributorLogins) {
        final var contributors = boContributorBiReadRepository.findAll(contributorLogins);
        return ok(new BOBiContributorListResponse()
                .contributors(contributors.stream().map(BOContributorBiReadEntity::toBoDto).toList()));
    }
}
