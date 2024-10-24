package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadContributorsApi;
import onlydust.com.marketplace.api.contract.model.ContributorKpiResponse;
import onlydust.com.marketplace.api.read.entities.user.UserKpiReadEntity;
import onlydust.com.marketplace.api.read.repositories.UserKpiReadRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper.DEFAULT_FROM_DATE;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper.sanitizedDate;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadContributorsApiPostgresAdapter implements ReadContributorsApi {

    private final UserKpiReadRepository userKpiReadRepository;

    @Override
    public ResponseEntity<ContributorKpiResponse> getContributorKPI(Long contributorId, String fromDate, String toDate) {
        final var sanitizedFromDate = sanitizedDate(fromDate, DEFAULT_FROM_DATE).truncatedTo(ChronoUnit.DAYS);
        final var sanitizedToDate = sanitizedDate(toDate, ZonedDateTime.now()).truncatedTo(ChronoUnit.DAYS).plusDays(1);
        final UserKpiReadEntity userKpiReadEntity = userKpiReadRepository.findByGithubUserIdAndDateRange(contributorId, sanitizedFromDate, sanitizedToDate)
                .orElseThrow(() -> notFound("Contributor %s not found".formatted(contributorId)));
        return ResponseEntity.ok(userKpiReadEntity.toResponse());
    }
}
