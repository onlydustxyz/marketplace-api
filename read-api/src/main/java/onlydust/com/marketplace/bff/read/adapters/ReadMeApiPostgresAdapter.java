package onlydust.com.marketplace.bff.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadMeApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.bff.read.entities.billing_profile.AllBillingProfileUserReadEntity;
import onlydust.com.marketplace.bff.read.entities.project.PublicProjectReadEntity;
import onlydust.com.marketplace.bff.read.mapper.RewardsMapper;
import onlydust.com.marketplace.bff.read.repositories.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.*;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
public class ReadMeApiPostgresAdapter implements ReadMeApi {
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final AllBillingProfileUserReadRepository allBillingProfileUserReadRepository;
    private final RewardDetailsReadRepository rewardDetailsReadRepository;
    private final UserRewardStatsReadRepository userRewardStatsReadRepository;
    private final PublicProjectReadRepository publicProjectReadRepository;
    private final UserReadRepository userReadRepository;

    @Override
    public ResponseEntity<JourneyCompletionResponse> getJourneyCompletion() {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var journeyCompletion = userReadRepository.findByUserId(authenticatedUser.getId())
                .orElseThrow(() -> internalServerError("User not found"));

        return ok(journeyCompletion.journeyCompletion().toResponse());
    }

    @Override
    public ResponseEntity<RecommendedProjectsPageResponse> getRecommendedProjects(Integer pageIndex, Integer pageSize) {
        final int sanitizePageIndex = sanitizePageIndex(pageIndex);
        final int sanitizePageSize = sanitizePageSize(pageSize);

        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var page = publicProjectReadRepository.findAllRecommendedForUser(authenticatedUser.getGithubUserId(),
                PageRequest.of(sanitizePageIndex, sanitizePageSize));

        final var response = new RecommendedProjectsPageResponse()
                .projects(page.getContent().stream().map(PublicProjectReadEntity::toProjectLinkWithDescription).toList())
                .hasMore(hasMore(sanitizePageIndex, page.getTotalPages()))
                .nextPageIndex(nextPageIndex(sanitizePageIndex, page.getTotalPages()))
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages());

        return response.getHasMore() ? status(HttpStatus.PARTIAL_CONTENT).body(response) : ok(response);
    }

    @Override
    public ResponseEntity<MyBillingProfilesResponse> getMyBillingProfiles() {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var billingProfiles = allBillingProfileUserReadRepository.findAllByUserId(authenticatedUser.getId());

        final var response = new MyBillingProfilesResponse()
                .billingProfiles(billingProfiles.stream().map(AllBillingProfileUserReadEntity::toShortResponse).toList());
        return ok(response);
    }

    @Override
    public ResponseEntity<MyRewardsPageResponse> getMyRewards(Integer pageIndex, Integer pageSize, RewardsSort sort, SortDirection direction,
                                                              List<UUID> currencies, List<UUID> projects, String fromDate, String toDate,
                                                              RewardStatusContract status) {
        final var sanitizedPageSize = sanitizePageSize(pageSize);
        final var sanitizedPageIndex = sanitizePageIndex(pageIndex);
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var sortBy = RewardDetailsReadRepository.sortBy(sort, direction);

        final var page = rewardDetailsReadRepository.findUserRewards(authenticatedUser.getGithubUserId(), currencies, projects,
                authenticatedUser.getAdministratedBillingProfiles(), Optional.ofNullable(status).map(Enum::name).orElse(null), fromDate, toDate,
                PageRequest.of(sanitizedPageIndex, sanitizedPageSize, sortBy));


        final var rewardsStats = userRewardStatsReadRepository.findByUser(authenticatedUser.getGithubUserId(), currencies, projects,
                authenticatedUser.getAdministratedBillingProfiles(), fromDate, toDate);

        final var myRewardsPageResponse = RewardsMapper.mapMyRewardsToResponse(sanitizedPageIndex, page, rewardsStats, authenticatedUser.asAuthenticatedUser());

        return myRewardsPageResponse.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(myRewardsPageResponse) :
                ResponseEntity.ok(myRewardsPageResponse);
    }
}
