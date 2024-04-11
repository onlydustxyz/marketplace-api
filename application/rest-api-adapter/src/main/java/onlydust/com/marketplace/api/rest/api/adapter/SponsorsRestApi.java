package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.SponsorFacadePort;
import onlydust.com.marketplace.api.contract.SponsorsApi;
import onlydust.com.marketplace.api.contract.model.SponsorDetailsResponse;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.SponsorMapper;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Tags(@Tag(name = "Sponsors"))
@AllArgsConstructor
@Slf4j
public class SponsorsRestApi implements SponsorsApi {
    private final SponsorFacadePort sponsorFacadePort;
    private final AccountingFacadePort accountingFacadePort;
    private final AuthenticatedAppUserService authenticatedAppUserService;

    @Override
    public ResponseEntity<SponsorDetailsResponse> getSponsor(UUID sponsorId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var sponsor = sponsorFacadePort.getSponsor(UserId.of(authenticatedUser.getId()), SponsorId.of(sponsorId))
                .orElseThrow(() -> OnlyDustException.notFound("Sponsor %s not found".formatted(sponsorId)));
        final var sponsorAccountStatements = accountingFacadePort.getSponsorAccounts(SponsorId.of(sponsorId));
        return ResponseEntity.ok(SponsorMapper.mapToSponsorDetailsResponse(sponsor, sponsorAccountStatements));

    }
}
