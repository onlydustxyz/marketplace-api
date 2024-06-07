package onlydust.com.marketplace.bff.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadMeApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.bff.read.entities.billing_profile.AllBillingProfileUserReadEntity;
import onlydust.com.marketplace.bff.read.mapper.RewardsMapper;
import onlydust.com.marketplace.bff.read.repositories.AllBillingProfileUserReadRepository;
import onlydust.com.marketplace.bff.read.repositories.RewardDetailsReadRepository;
import onlydust.com.marketplace.bff.read.repositories.UserRewardStatsReadRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageIndex;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageSize;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
public class ReadMeApiPostgresAdapter implements ReadMeApi {
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final AllBillingProfileUserReadRepository allBillingProfileUserReadRepository;
    private final RewardDetailsReadRepository rewardDetailsReadRepository;
    private final UserRewardStatsReadRepository userRewardStatsReadRepository;

    @Override
    public ResponseEntity<MyBillingProfilesResponse> getMyBillingProfiles() {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var billingProfiles = allBillingProfileUserReadRepository.findAllByUserId(authenticatedUser.getId());

        final var response = new MyBillingProfilesResponse()
                .billingProfiles(billingProfiles.stream().map(AllBillingProfileUserReadEntity::toShortResponse).toList());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<MyRewardsPageResponse> getMyRewards(Integer pageIndex, Integer pageSize, RewardsSort sort, SortDirection direction,
                                                              List<UUID> currencies, List<UUID> projects, String fromDate, String toDate,
                                                              List<RewardStatusContract> status) {
        final var sanitizedPageSize = sanitizePageSize(pageSize);
        final var sanitizedPageIndex = sanitizePageIndex(pageIndex);
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var sortBy = RewardDetailsReadRepository.sortBy(sort, direction);

        final var page = rewardDetailsReadRepository.findUserRewards(authenticatedUser.getGithubUserId(), currencies, projects,
                authenticatedUser.getAdministratedBillingProfiles(), fromDate, toDate, PageRequest.of(sanitizedPageIndex, sanitizedPageSize, sortBy));


        final var rewardsStats = userRewardStatsReadRepository.findByUser(authenticatedUser.getGithubUserId(), currencies, projects,
                authenticatedUser.getAdministratedBillingProfiles(), fromDate, toDate);

        final var myRewardsPageResponse = RewardsMapper.mapMyRewardsToResponse(sanitizedPageIndex, page, rewardsStats, authenticatedUser.asAuthenticatedUser());

        return myRewardsPageResponse.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(myRewardsPageResponse) :
                ResponseEntity.ok(myRewardsPageResponse);
    }

}
