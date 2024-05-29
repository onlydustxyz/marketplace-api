package onlydust.com.marketplace.bff.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackOfficeCommitteeReadApi;
import onlydust.com.backoffice.api.contract.model.CommitteeBudgetAllocationsResponse;
import onlydust.com.marketplace.bff.read.entities.CommitteeBudgetAllocationViewEntity;
import onlydust.com.marketplace.bff.read.entities.ShortCurrencyResponseEntity;
import onlydust.com.marketplace.bff.read.repositories.CommitteeBudgetAllocationsResponseEntityRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
public class BackOfficeCommitteeReadApiPostgresAdapter implements BackOfficeCommitteeReadApi {
    private final CommitteeBudgetAllocationsResponseEntityRepository committeeBudgetAllocationsResponseEntityRepository;

    @Override
    public ResponseEntity<CommitteeBudgetAllocationsResponse> getBudgetAllocations(UUID committeeId) {
        final var committeeBudgetAllocations = committeeBudgetAllocationsResponseEntityRepository.findAllByCommitteeId(committeeId);
        return ResponseEntity.ok(new CommitteeBudgetAllocationsResponse()
                .totalAllocationAmount(committeeBudgetAllocations.stream()
                        .map(CommitteeBudgetAllocationViewEntity::amount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .currency(committeeBudgetAllocations.stream().findFirst()
                        .map(CommitteeBudgetAllocationViewEntity::currency).map(ShortCurrencyResponseEntity::toDto)
                        .orElse(null))
                .projectAllocations(committeeBudgetAllocations.stream()
                        .map(CommitteeBudgetAllocationViewEntity::toDto)
                        .toList()));
    }
}
