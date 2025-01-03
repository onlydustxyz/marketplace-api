package onlydust.com.marketplace.api.read.adapters;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadRewardsApi;
import onlydust.com.marketplace.api.contract.model.PageableRewardsQueryParams;
import onlydust.com.marketplace.api.contract.model.RewardPageItemResponse;
import onlydust.com.marketplace.api.contract.model.RewardPageResponse;
import onlydust.com.marketplace.api.read.entities.reward.RewardV2ReadEntity;
import onlydust.com.marketplace.api.read.repositories.RewardReadV2Repository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.blockchain.MetaBlockExplorer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.StringWriter;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper.parseZonedNullable;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;
import static onlydust.com.marketplace.kernel.model.AuthenticatedUser.BillingProfileMembership.Role.ADMIN;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.hasMore;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.nextPageIndex;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.PARTIAL_CONTENT;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadRewardsPostgresAdapter implements ReadRewardsApi {
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final RewardReadV2Repository rewardReadV2Repository;
    private final MetaBlockExplorer blockExplorer;

    @Override
    public ResponseEntity<RewardPageResponse> getRewards(PageableRewardsQueryParams q) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var page = findRewards(q, authenticatedUser);
        return ok(new RewardPageResponse()
                .rewards(page.stream().map(r -> r.toDto(authenticatedUser, blockExplorer)).toList())
                .hasMore(hasMore(q.getPageIndex(), page.getTotalPages()))
                .nextPageIndex(nextPageIndex(q.getPageIndex(), page.getTotalPages()))
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages()));
    }

    @GetMapping(
            value = "/api/v1/rewards",
            produces = "text/csv"
    )
    @Transactional(readOnly = true)
    public ResponseEntity<String> exportRewards(@Parameter(name = "queryParams", in = ParameterIn.QUERY) @Valid PageableRewardsQueryParams q) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var page = findRewards(q, authenticatedUser);

        final var format = CSVFormat.DEFAULT.builder().build();
        final var sw = new StringWriter();

        try (final var printer = new CSVPrinter(sw, format)) {
            printer.printRecord("id", "status", "request_at", "invoiced_at", "processed_at", "unlock_date", "requestor", "recipient",
                    "project_id", "billing_profile_id", "invoice_id", "invoice_number", "amount", "currency_code", "amount_usd_equivalent",
                    "transaction_reference", "transaction_reference_link");
            for (final var reward : page.getContent())
                reward.toCsv(authenticatedUser, blockExplorer, printer);
        } catch (final IOException e) {
            throw internalServerError("Error while exporting rewards to CSV", e);
        }

        final var csv = sw.toString();
        return status(hasMore(q.getPageIndex(), page.getTotalPages()) ? PARTIAL_CONTENT : OK)
                .body(csv);
    }

    private Page<RewardV2ReadEntity> findRewards(PageableRewardsQueryParams q, AuthenticatedUser authenticatedUser) {
        final var sanitizedFromDate = q.getFromDate() == null ? null : parseZonedNullable(q.getFromDate()).truncatedTo(ChronoUnit.DAYS);
        final var sanitizedToDate = q.getToDate() == null ? null : parseZonedNullable(q.getToDate()).truncatedTo(ChronoUnit.DAYS).plusDays(1);

        return rewardReadV2Repository.findAll(
                q.getIncludeProjectLeds(),
                q.getIncludeBillingProfileAdministrated(),
                q.getIncludeAsRecipient(),
                null,
                sanitizedFromDate,
                sanitizedToDate,
                authenticatedUser.projectsLed() == null ? null : authenticatedUser.projectsLed().toArray(UUID[]::new),
                authenticatedUser.billingProfiles() == null ? null :
                        authenticatedUser.billingProfiles().stream()
                                .filter(bp -> bp.role() == ADMIN)
                                .map(AuthenticatedUser.BillingProfileMembership::billingProfileId)
                                .toArray(UUID[]::new),
                authenticatedUser.githubUserId(),
                q.getStatuses() == null ? null : q.getStatuses().stream().map(Enum::name).toArray(String[]::new),
                q.getProjectIds() == null ? null : q.getProjectIds().toArray(UUID[]::new),
                q.getBillingProfileIds() == null ? null : q.getBillingProfileIds().toArray(UUID[]::new),
                q.getCurrencyIds() == null ? null : q.getCurrencyIds().toArray(UUID[]::new),
                q.getContributionUUIDs() == null ? null : q.getContributionUUIDs().toArray(UUID[]::new),
                q.getRecipientIds() == null ? null : q.getRecipientIds().toArray(Long[]::new),
                q.getSearch(),
                PageRequest.of(q.getPageIndex(), q.getPageSize(), Sort.by(Sort.Order.desc("requested_at")))
        );
    }

    @Override
    public ResponseEntity<RewardPageItemResponse> getReward(UUID rewardId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        final var page = rewardReadV2Repository.findAll(
                true,
                true,
                true,
                rewardId,
                null,
                null,
                authenticatedUser.projectsLed() == null ? null : authenticatedUser.projectsLed().toArray(UUID[]::new),
                authenticatedUser.billingProfiles() == null ? null :
                        authenticatedUser.billingProfiles().stream()
                                .filter(bp -> bp.role() == ADMIN)
                                .map(AuthenticatedUser.BillingProfileMembership::billingProfileId)
                                .toArray(UUID[]::new),
                authenticatedUser.githubUserId(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                PageRequest.of(0, 1)
        );

        final var reward = page.getContent().stream().findFirst().orElseThrow(() -> notFound("Reward %s not found".formatted(rewardId)));

        return ok(reward.toDto(authenticatedUser, blockExplorer));
    }
}
