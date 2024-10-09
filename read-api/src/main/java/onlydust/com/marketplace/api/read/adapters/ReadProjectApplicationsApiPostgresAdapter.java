package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadProjectApplicationsApi;
import onlydust.com.marketplace.api.contract.model.ApplicationsQueryParams;
import onlydust.com.marketplace.api.contract.model.ProjectApplicationPageResponse;
import onlydust.com.marketplace.api.contract.model.ProjectApplicationPageSort;
import onlydust.com.marketplace.api.contract.model.ProjectApplicationResponse;
import onlydust.com.marketplace.api.read.entities.project.ApplicationReadEntity;
import onlydust.com.marketplace.api.read.repositories.ApplicationReadRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.service.PermissionService;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.JpaSort;
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
    public ResponseEntity<ProjectApplicationPageResponse> getProjectsApplications(Integer pageIndex,
                                                                                  Integer pageSize, UUID projectId,
                                                                                  Long issueId,
                                                                                  Long applicantId,
                                                                                  Boolean isApplicantProjectMember,
                                                                                  String applicantLoginSearch,
                                                                                  ProjectApplicationPageSort sort) {
        if (projectId == null && applicantId == null) {
            throw OnlyDustException.badRequest("At least one of projectId and applicantId must be provided");
        }

        final var caller = authenticatedAppUserService.getAuthenticatedUser();
        if (!caller.githubUserId().equals(applicantId) && !permissionService.isUserProjectLead(ProjectId.of(projectId), caller.id())) {
            throw forbidden("Only project leads can get project applications");
        }

        final var page = applicationReadRepository.findAll(projectId,
                issueId,
                applicantId,
                isApplicantProjectMember,
                applicantLoginSearch,
                PageRequest.of(pageIndex, pageSize, JpaSort.unsafe("element(applicant.globalUsersRanks).rank")));

        return ok(new ProjectApplicationPageResponse()
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .nextPageIndex(page.hasNext() ? pageIndex + 1 : pageIndex)
                .hasMore(page.hasNext())
                .applications(page.getContent().stream().map(ApplicationReadEntity::toPageItemDto).toList())
        );
    }

    @Override
    public ResponseEntity<ProjectApplicationPageResponse> getProjectsApplicationsV2(ApplicationsQueryParams params) {
        if (params.getProjectId() == null && params.getApplicantId() == null) {
            throw OnlyDustException.badRequest("At least one of projectId and applicantId must be provided");
        }

        final var caller = authenticatedAppUserService.getAuthenticatedUser();
        if (!caller.githubUserId().equals(params.getApplicantId()) && !permissionService.isUserProjectLead(ProjectId.of(params.getProjectId()), caller.id())) {
            throw forbidden("Only project leads can get project applications");
        }

        final var page = applicationReadRepository.findAll(
                params.getProjectId(),
                params.getIssueId(),
                params.getApplicantId(),
                params.getIsApplicantProjectMember(),
                params.getU() == null ? null : params.getU().getSearch(),
                PageRequest.of(params.getPageIndex(), params.getPageSize()));

        return ok(new ProjectApplicationPageResponse()
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .nextPageIndex(page.hasNext() ? params.getPageIndex() + 1 : params.getPageIndex())
                .hasMore(page.hasNext())
                .applications(page.getContent().stream().map(ApplicationReadEntity::toPageItemDto).toList())
        );
    }

    @Override
    public ResponseEntity<ProjectApplicationResponse> getProjectApplication(UUID applicationId) {
        final var caller = authenticatedAppUserService.getAuthenticatedUser();
        final var application =
                applicationReadRepository.findById(applicationId).orElseThrow(() -> notFound("Application %s not found".formatted(applicationId)));
        if (!caller.githubUserId().equals(application.applicant().contributorId()) &&
            !permissionService.isUserProjectLead(ProjectId.of(application.projectId()), caller.id())) {
            throw forbidden("Only project leads and applicant can get application details");
        }
        return ok(application.toDto());
    }
}
