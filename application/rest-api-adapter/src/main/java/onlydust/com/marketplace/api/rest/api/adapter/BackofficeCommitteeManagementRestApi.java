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
import onlydust.com.marketplace.project.domain.view.CommitteeLinkView;
import onlydust.com.marketplace.project.domain.view.CommitteeView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeCommitteeMapper.toCommitteeResponse;

@RestController
@Tags(@Tag(name = "BackofficeCommitteeManagement"))
@AllArgsConstructor
public class BackofficeCommitteeManagementRestApi implements BackOfficeCommitteeManagementApi {


    final ShortCurrencyResponse allocationCurrency = new ShortCurrencyResponse().id(UUID.randomUUID())
            .code("STRK")
            .name("Starknet token")
            .logoUrl(URI.create("https://s2.coinmarketcap.com/static/img/coins/64x64/22691.png"))
            .decimals(5);

    final ProjectLinkResponse bretzel = new ProjectLinkResponse()
            .id(UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56"))
            .name("Bretzel")
            .logoUrl("Bretzel")
            .slug("bretzel");

    private final CommitteeFacadePort committeeFacadePort;

    @Override
    public ResponseEntity<Void> allocateBudget(UUID committeeId, CommitteeBudgetAllocationsRequest committeeBudgetAllocationsRequest) {
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<CommitteeResponse> createCommittee(CreateCommitteeRequest createCommitteeRequest) {
        final Committee committee = committeeFacadePort.createCommittee(createCommitteeRequest.getName(), createCommitteeRequest.getApplicationStartDate(),
                createCommitteeRequest.getApplicationEndDate());
        return ResponseEntity.ok(toCommitteeResponse(committee));
    }

    @Override
    public ResponseEntity<CommitteeBudgetAllocationsResponse> getBudgetAllocations(UUID committeeId) {
        return BackOfficeCommitteeManagementApi.super.getBudgetAllocations(committeeId);
    }

    @Override
    public ResponseEntity<CommitteeResponse> getCommittee(UUID committeeId) {
        final CommitteeView committeeView = committeeFacadePort.getCommitteeById(Committee.Id.of(committeeId));
        return ResponseEntity.ok(BackOfficeCommitteeMapper.toCommitteeResponse(committeeView));
    }

    @Override
    public ResponseEntity<CommitteePageResponse> getCommittees(Integer pageIndex, Integer pageSize) {
        final int sanitizePageIndex = PaginationHelper.sanitizePageIndex(pageIndex);
        final int sanitizePageSize = PaginationHelper.sanitizePageSize(pageSize);
        final Page<CommitteeLinkView> page = committeeFacadePort.getCommittees(sanitizePageIndex, sanitizePageSize);
        final CommitteePageResponse committeePageResponse = BackOfficeCommitteeMapper.toCommitteePageResponse(page, sanitizePageIndex);
        return committeePageResponse.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(committeePageResponse) :
                ResponseEntity.ok(committeePageResponse);
    }

    @Override
    public ResponseEntity<CommitteeProjectApplicationResponse> getProjectApplication(UUID committeeId, UUID projectId) {
        final CommitteeProjectApplicationResponse committeeProjectApplicationResponse = new CommitteeProjectApplicationResponse();
        committeeProjectApplicationResponse.setProject(bretzel);
        committeeProjectApplicationResponse.setAllocationCurrency(allocationCurrency);
        committeeProjectApplicationResponse.setProjectQuestions(getProjectAnswers());
        return ResponseEntity.ok(committeeProjectApplicationResponse);
    }

    @Override
    public ResponseEntity<Void> updateCommittee(UUID committeeId, UpdateCommitteeRequest updateCommitteeRequest) {
        committeeFacadePort.update(BackOfficeCommitteeMapper.updateCommitteeRequestToDomain(committeeId, updateCommitteeRequest));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> updateCommitteeStatus(UUID committeeId, UpdateCommitteeStatusRequest updateCommitteeStatusRequest) {
        committeeFacadePort.updateStatus(Committee.Id.of(committeeId), BackOfficeCommitteeMapper.statusToDomain(updateCommitteeStatusRequest.getStatus()));
        return ResponseEntity.noContent().build();
    }

    private static List<ProjectAnswerResponse> getProjectAnswers() {
        return List.of(
                new ProjectAnswerResponse()
                        .answer("Java")
                        .question("Quel est le meilleur langage de programmation ?"),
                new ProjectAnswerResponse()
                        .answer("Qu'en pense Mehdi ?")
                        .question("Mettons des merguez dans les couscous marocain ?"),
                new ProjectAnswerResponse()
                        .answer("Ca c'est une bonne blague !")
                        .question("Trouves-tu les blagues de Grég drôles ?")
        );
    }
}
