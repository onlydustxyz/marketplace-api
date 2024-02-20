package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.Ecosystem;
import onlydust.com.marketplace.api.domain.port.output.EcosystemStorage;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.EcosystemEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.EcosystemRepository;
import onlydust.com.marketplace.kernel.pagination.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@AllArgsConstructor
public class PostgresEcosystemAdapter implements EcosystemStorage {

    private final EcosystemRepository ecosystemRepository;

    @Override
    public Page<Ecosystem> findAll(int pageIndex, int pageSize) {
        final var ecosystemEntityPage = ecosystemRepository.findAll(PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.ASC, "name")));
        return Page.<Ecosystem>builder()
                .content(ecosystemEntityPage.getContent().stream().map(EcosystemEntity::toDomain).toList())
                .totalItemNumber((int) ecosystemEntityPage.getTotalElements())
                .totalPageNumber(ecosystemEntityPage.getTotalPages())
                .build();
    }
}
