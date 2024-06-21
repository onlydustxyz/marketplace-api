package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadProjectApplicationsApi;
import onlydust.com.marketplace.api.contract.model.ProjectApplicationPageResponse;
import onlydust.com.marketplace.api.contract.model.ProjectApplicationPageSort;
import onlydust.com.marketplace.api.contract.model.ProjectApplicationResponse;
import onlydust.com.marketplace.api.read.entities.project.ApplicationReadEntity;
import onlydust.com.marketplace.api.read.repositories.ApplicationReadRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.project.domain.service.PermissionService;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.forbidden;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadProjectApplicationsApiPostgresAdapter implements ReadProjectApplicationsApi {

    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final PermissionService permissionService;
    private final ApplicationReadRepository applicationReadRepository;

    @Override
    public ResponseEntity<ProjectApplicationPageResponse> getProjectsApplications(UUID projectId,
                                                                                  Long issueId,
                                                                                  Long applicantId,
                                                                                  Boolean isApplicantProjectMember,
                                                                                  String applicantLoginSearch,
                                                                                  ProjectApplicationPageSort sort,
                                                                                  Integer pageIndex,
                                                                                  Integer pageSize) {
        if (projectId == null && applicantId == null) {
            throw OnlyDustException.badRequest("At least one of projectId and applicantId must be provided");
        }

        final var caller = authenticatedAppUserService.getAuthenticatedUser();
        if (!caller.getGithubUserId().equals(applicantId) && !permissionService.isUserProjectLead(projectId, caller.getId())) {
            throw forbidden("Only project leads can get project applications");
        }

        final var page = applicationReadRepository.findAll(projectId,
                issueId,
                applicantId,
                isApplicantProjectMember,
                applicantLoginSearch,
                PageRequest.of(pageIndex, pageSize, Sort.by("applicant.login")));

        return ok(new ProjectApplicationPageResponse()
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .nextPageIndex(page.hasNext() ? pageIndex + 1 : pageIndex)
                .hasMore(page.hasNext())
                .applications(page.getContent().stream().map(ApplicationReadEntity::toPageItemDto).toList())
        );
    }

    @Override
    public ResponseEntity<ProjectApplicationResponse> getProjectApplication(UUID applicationId) {
        final var caller = authenticatedAppUserService.getAuthenticatedUser();
        final var application =
                applicationReadRepository.findById(applicationId).orElseThrow(() -> notFound("Application %s not found".formatted(applicationId)));
        if (!caller.getGithubUserId().equals(application.applicant().githubUserId()) &&
            !permissionService.isUserProjectLead(application.projectId(), caller.getId())) {
            throw forbidden("Only project leads and applicant can get application details");
        }
        return ok(application.toDto());
    }
}
