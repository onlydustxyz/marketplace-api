package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeSponsorReadApi;
import onlydust.com.backoffice.api.contract.model.SponsorDetailsResponse;
import onlydust.com.backoffice.api.contract.model.SponsorPage;
import onlydust.com.marketplace.api.read.repositories.SponsorReadRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("bo")
public class BackofficeSponsorsReadApiPostgresAdapter implements BackofficeSponsorReadApi {
    private final SponsorReadRepository sponsorReadRepository;

    @Override
    public ResponseEntity<SponsorDetailsResponse> getSponsor(UUID sponsorId) {
        final var sponsor = sponsorReadRepository.findById(sponsorId)
                .orElseThrow(() -> notFound("Sponsor %s not found".formatted(sponsorId)));

        return ok(sponsor.toBoResponse());
    }

    @Override
    public ResponseEntity<SponsorPage> getSponsorPage(Integer pageIndex, Integer pageSize, String search) {
        return BackofficeSponsorReadApi.super.getSponsorPage(pageIndex, pageSize, search);
    }
}
