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

import java.math.BigDecimal;
import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

    final UserLinkResponse pierre = new UserLinkResponse()
            .githubUserId(16590657L)
            .userId(UUID.fromString("fc92397c-3431-4a84-8054-845376b630a0"))
            .avatarUrl("https://avatars.githubusercontent.com/u/16590657?v=4")
            .login("PierreOucif");

    final UserLinkResponse olivier = new UserLinkResponse()
            .githubUserId(595505L)
            .userId(UUID.fromString("e461c019-ba23-4671-9b6c-3a5a18748af9"))
            .avatarUrl("https://avatars.githubusercontent.com/u/595505?v=4")
            .login("ofux");

    final ProjectLinkResponse bretzel = new ProjectLinkResponse()
            .id(UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56"))
            .name("Bretzel")
            .logoUrl("Bretzel")
            .slug("bretzel");

    final ProjectLinkResponse barbicane = new ProjectLinkResponse()
            .id(UUID.fromString("545a2fb2-0b2e-4b14-ac64-c750b338a685"))
            .logoUrl("https://staging-onlydust-app-images.s3.eu-west-1.amazonaws.com/db1f4c8752147890915b764bf3db1baa.png")
            .name("Barbicane")
            .slug("barbicane");

    final SponsorLinkResponse starknet = new SponsorLinkResponse()
            .avatarUrl("https://logos-marques.com/wp-content/uploads/2020/09/Logo-Instagram-1.png")
            .name("Starknet Foundation");

    private final CommitteeFacadePort committeeFacadePort;

    @Override
    public ResponseEntity<Void> allocateBudget(UUID committeeId, CommitteeBudgetAllocationsRequest committeeBudgetAllocationsRequest) {
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<CommitteeResponse> createCommittee(CreateCommitteeRequest createCommitteeRequest) {
        final Committee committee = committeeFacadePort.createCommittee(createCommitteeRequest.getName(), createCommitteeRequest.getStartDate(),
                createCommitteeRequest.getEndDate());
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
        return ResponseEntity.noContent().build();
    }

    private CommitteeResponse getCommitteeResponse() {
        final CommitteeResponse committeeResponse = new CommitteeResponse();
        committeeResponse.setAllocationCurrency(allocationCurrency);
        committeeResponse.setApplications(List.of(
                new ApplicationResponse()
                        .applicant(pierre)
                        .project(barbicane)
                        .score(3)
                        .allocatedBudget(BigDecimal.valueOf(10.2)),
                new ApplicationResponse()
                        .applicant(olivier)
                        .project(bretzel)
                        .score(2)
                        .allocatedBudget(null)
        ));
        committeeResponse.setId(UUID.randomUUID());
        committeeResponse.setEndDate(ZonedDateTime.of(2024, 6, 23, 0, 0, 0, 0, ZoneId.systemDefault()));
        committeeResponse.setStartDate(ZonedDateTime.of(2024, 5, 31, 0, 0, 0, 0, ZoneId.systemDefault()));
        committeeResponse.setCompletedAssignments(0);
        committeeResponse.setName("Committee mock");
        committeeResponse.setProjectCount(2);
        committeeResponse.setProjectQuestions(getProjectQuestions());
        committeeResponse.setTotalAssignments(2);
        committeeResponse.setSponsor(starknet);
        return committeeResponse;
    }

    private static List<ProjectQuestionResponse> getProjectQuestions() {
        return List.of(
                new ProjectQuestionResponse()
                        .id(UUID.randomUUID())
                        .required(false)
                        .question("Quel est le meilleur langage de programmation ?"),
                new ProjectQuestionResponse()
                        .id(UUID.randomUUID())
                        .required(true)
                        .question("Mettons des merguez dans les couscous marocain ?"),
                new ProjectQuestionResponse()
                        .id(UUID.randomUUID())
                        .required(true)
                        .question("Trouves-tu les blagues de Grég drôles ?")
        );
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
