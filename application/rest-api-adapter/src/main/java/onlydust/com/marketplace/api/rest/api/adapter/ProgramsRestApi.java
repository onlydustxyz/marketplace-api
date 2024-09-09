package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.api.contract.ProgramsApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.port.input.ProgramFacadePort;
import onlydust.com.marketplace.project.domain.service.PermissionService;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.unauthorized;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Tags(@Tag(name = "Sponsors"))
@AllArgsConstructor
@Slf4j
@Profile("api")
public class ProgramsRestApi implements ProgramsApi {
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final AccountingFacadePort accountingFacadePort;
    private final PermissionService permissionService;
    private final ProgramFacadePort programFacadePort;

    @Override
    public ResponseEntity<Void> grantBudgetToProject(UUID programId, GrantRequest request) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        if (!permissionService.isUserProgramLead(authenticatedUser.id(), ProgramId.of(programId)))
            throw unauthorized("User %s is not authorized to access program %s".formatted(authenticatedUser.id(), programId));

        accountingFacadePort.grant(
                ProgramId.of(programId),
                ProjectId.of(request.getProjectId()),
                PositiveAmount.of(request.getAmount()),
                Currency.Id.of(request.getCurrencyId()));

        return noContent().build();
    }

    @Override
    public ResponseEntity<CreateProgramResponse> createProgram(UUID sponsorId, CreateProgramRequest request) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        if (!permissionService.isUserSponsorLead(authenticatedUser.id(), SponsorId.of(sponsorId)))
            throw unauthorized("User %s is not authorized to create program for sponsor %s".formatted(authenticatedUser.id(), sponsorId));

        final var program = programFacadePort.create(request.getName(), SponsorId.of(sponsorId), request.getUrl(), request.getLogoUrl(),
                request.getLeadIds().stream().map(UserId::of).toList());

        return ok(new CreateProgramResponse().id(program.id().value()));
    }

    @Override
    public ResponseEntity<Void> updateProgram(UUID programId, UpdateProgramRequest request) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        if (!permissionService.isUserSponsorLeadOfProgram(authenticatedUser.id(), ProgramId.of(programId)) &&
            !permissionService.isUserProgramLead(authenticatedUser.id(), ProgramId.of(programId)))
            throw unauthorized("User %s is not authorized to modify program %s".formatted(authenticatedUser.id(), programId));

        programFacadePort.update(programId, request.getName(), request.getUrl(), request.getLogoUrl(),
                request.getLeadIds().stream().map(UserId::of).toList());

        return noContent().build();
    }

    @Override
    public ResponseEntity<UploadImageResponse> uploadProgramLogo(Resource image) {
        InputStream imageInputStream;
        try {
            imageInputStream = image.getInputStream();
        } catch (IOException e) {
            throw badRequest("Error while reading image data", e);
        }

        final var imageUrl = programFacadePort.uploadLogo(imageInputStream);

        return ResponseEntity.ok(new UploadImageResponse().url(imageUrl.toString()));
    }
}
