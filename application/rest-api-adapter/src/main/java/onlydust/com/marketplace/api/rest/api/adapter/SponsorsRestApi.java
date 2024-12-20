package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Deposit;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.api.contract.SponsorsApi;
import onlydust.com.marketplace.api.contract.model.AllocateRequest;
import onlydust.com.marketplace.api.contract.model.PreviewDepositRequest;
import onlydust.com.marketplace.api.contract.model.PreviewDepositResponse;
import onlydust.com.marketplace.api.contract.model.UpdateDepositRequest;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.DepositMapper;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.project.domain.port.input.SponsorFacadePort;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.NetworkMapper.map;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.forbidden;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Tags(@Tag(name = "Sponsors"))
@AllArgsConstructor
@Slf4j
@Profile("api")
public class SponsorsRestApi implements SponsorsApi {
    private final SponsorFacadePort sponsorFacadePort;
    private final AccountingFacadePort accountingFacadePort;
    private final AuthenticatedAppUserService authenticatedAppUserService;

    @Override
    public ResponseEntity<Void> allocateBudgetToProgram(UUID sponsorId, AllocateRequest request) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        // Block Henri L.
        if (authenticatedUser.githubUserId().equals(22731646L)) {
            throw forbidden("You are not allowed to allocate a budget to a program");
        }
        final var sponsor = sponsorFacadePort.findById(authenticatedUser.id(), SponsorId.of(sponsorId))
                .orElseThrow(() -> notFound("Sponsor %s not found".formatted(sponsorId)));

        accountingFacadePort.allocate(
                SponsorId.of(sponsor.id().value()),
                ProgramId.of(request.getProgramId()),
                PositiveAmount.of(request.getAmount()),
                Currency.Id.of(request.getCurrencyId())
        );

        return noContent().build();
    }

    @Override
    public ResponseEntity<PreviewDepositResponse> previewDeposit(UUID sponsorId, PreviewDepositRequest request) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        final var deposit = accountingFacadePort.previewDeposit(
                authenticatedUser.id(),
                SponsorId.of(sponsorId),
                map(request.getNetwork()),
                request.getTransactionReference());

        return ok(new PreviewDepositResponse().id(deposit.id().value()));
    }

    @Override
    public ResponseEntity<Void> updateDeposit(UUID depositId, UpdateDepositRequest updateDepositRequest) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        accountingFacadePort.submitDeposit(
                authenticatedUser.id(),
                Deposit.Id.of(depositId),
                DepositMapper.fromBillingInformation(updateDepositRequest.getBillingInformation())
        );

        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> unallocateBudgetFromProgram(UUID sponsorId, AllocateRequest request) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var sponsor = sponsorFacadePort.findById(authenticatedUser.id(), SponsorId.of(sponsorId))
                .orElseThrow(() -> notFound("Sponsor %s not found".formatted(sponsorId)));

        accountingFacadePort.unallocate(
                ProgramId.of(request.getProgramId()),
                SponsorId.of(sponsor.id().value()),
                PositiveAmount.of(request.getAmount()),
                Currency.Id.of(request.getCurrencyId())
        );

        return noContent().build();
    }
}
