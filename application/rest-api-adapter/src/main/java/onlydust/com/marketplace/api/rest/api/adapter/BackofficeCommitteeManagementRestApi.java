package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackOfficeCommitteeManagementApi;
import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeCommitteeMapper;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.port.input.CommitteeFacadePort;
import onlydust.com.marketplace.project.domain.view.commitee.CommitteeApplicationDetailsView;
import onlydust.com.marketplace.project.domain.view.commitee.CommitteeLinkView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static java.util.stream.Collectors.toMap;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeCommitteeMapper.toCommitteeResponse;
import static org.springframework.http.ResponseEntity.*;

@RestController
@Tags(@Tag(name = "BackofficeCommitteeManagement"))
@AllArgsConstructor
public class BackofficeCommitteeManagementRestApi implements BackOfficeCommitteeManagementApi {

    private final CommitteeFacadePort committeeFacadePort;

    @Override
    public ResponseEntity<CommitteeResponse> createCommittee(CreateCommitteeRequest createCommitteeRequest) {
        final Committee committee = committeeFacadePort.createCommittee(createCommitteeRequest.getName(), createCommitteeRequest.getApplicationStartDate(),
                createCommitteeRequest.getApplicationEndDate());
        return ok(toCommitteeResponse(committee));
    }

    @Override
    public ResponseEntity<Void> createProjectAllocations(UUID committeeId, CommitteeBudgetAllocationsCreateRequest request) {
        committeeFacadePort.allocate(Committee.Id.of(committeeId),
                request.getCurrencyId(),
                request.getAmount(),
                request.getMinAllocation(),
                request.getMaxAllocation());
        return noContent().build();
    }

    @Override
    public ResponseEntity<CommitteePageResponse> getCommittees(Integer pageIndex, Integer pageSize) {
        final int sanitizePageIndex = PaginationHelper.sanitizePageIndex(pageIndex);
        final int sanitizePageSize = PaginationHelper.sanitizePageSize(pageSize);
        final Page<CommitteeLinkView> page = committeeFacadePort.getCommittees(sanitizePageIndex, sanitizePageSize);
        final CommitteePageResponse committeePageResponse = BackOfficeCommitteeMapper.toCommitteePageResponse(page, sanitizePageIndex);
        return committeePageResponse.getTotalPageNumber() > 1 ?
                status(HttpStatus.PARTIAL_CONTENT).body(committeePageResponse) :
                ok(committeePageResponse);
    }

    @Override
    public ResponseEntity<CommitteeProjectApplicationResponse> getProjectApplication(UUID committeeId, UUID projectId) {
        final CommitteeApplicationDetailsView committeeApplicationDetails = committeeFacadePort.getCommitteeApplicationDetails(Committee.Id.of(committeeId),
                projectId);
        return ok(BackOfficeCommitteeMapper.committeeApplicationDetailsToResponse(committeeApplicationDetails));
    }

    @Override
    public ResponseEntity<Void> updateCommittee(UUID committeeId, UpdateCommitteeRequest updateCommitteeRequest) {
        committeeFacadePort.update(BackOfficeCommitteeMapper.updateCommitteeRequestToDomain(committeeId, updateCommitteeRequest));
        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> updateCommitteeStatus(UUID committeeId, UpdateCommitteeStatusRequest updateCommitteeStatusRequest) {
        committeeFacadePort.updateStatus(Committee.Id.of(committeeId), BackOfficeCommitteeMapper.statusToDomain(updateCommitteeStatusRequest.getStatus()));
        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> updateProjectAllocations(UUID committeeId, CommitteeBudgetAllocationsUpdateRequest request) {
        committeeFacadePort.saveAllocations(Committee.Id.of(committeeId), request.getCurrencyId(),
                request.getAllocations().stream().collect(toMap(
                        CommitteeProjectAllocationRequest::getProjectId,
                        CommitteeProjectAllocationRequest::getAmount)));
        return noContent().build();
    }
}
