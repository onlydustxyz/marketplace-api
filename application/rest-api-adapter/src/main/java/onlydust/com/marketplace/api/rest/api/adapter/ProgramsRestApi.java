package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.service.AccountingPermissionService;
import onlydust.com.marketplace.api.contract.ProgramsApi;
import onlydust.com.marketplace.api.contract.model.AllocateRequest;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.unauthorized;
import static org.springframework.http.ResponseEntity.noContent;

@RestController
@Tags(@Tag(name = "Sponsors"))
@AllArgsConstructor
@Slf4j
@Profile("api")
public class ProgramsRestApi implements ProgramsApi {
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final AccountingFacadePort accountingFacadePort;
    private final AccountingPermissionService accountingPermissionService;

    @Override
    public ResponseEntity<Void> grantBudgetToProject(UUID programId, AllocateRequest allocateRequest) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        if (!accountingPermissionService.isUserProgramLead(UserId.of(authenticatedUser.id()), SponsorId.of(programId)))
            throw unauthorized("User %s is not authorized to access program %s".formatted(authenticatedUser.id(), programId));

        accountingFacadePort.allocate(
                SponsorId.of(programId),
                ProjectId.of(allocateRequest.getProjectId()),
                PositiveAmount.of(allocateRequest.getAmount()),
                Currency.Id.of(allocateRequest.getCurrencyId()));

        return noContent().build();
    }
}
