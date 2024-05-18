package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.CommitteeStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.backoffice.BoCommitteeQueryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CommitteeApplicationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CommitteeEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CommitteeApplicationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CommitteeRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.BoCommitteeQueryRepository;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.port.output.CommitteeStoragePort;
import onlydust.com.marketplace.project.domain.view.CommitteeLinkView;
import onlydust.com.marketplace.project.domain.view.CommitteeView;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@AllArgsConstructor
public class PostgresCommitteeAdapter implements CommitteeStoragePort {

    private final CommitteeRepository committeeRepository;
    private final BoCommitteeQueryRepository boCommitteeQueryRepository;
    private final CommitteeApplicationRepository committeeApplicationRepository;

    @Override
    @Transactional
    public Committee save(Committee committee) {
        return committeeRepository.save(CommitteeEntity.fromDomain(committee)).toDomain();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommitteeLinkView> findAll(Integer pageIndex, Integer pageSize) {
        final var committees = committeeRepository.findAll(PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "techCreatedAt")));
        return Page.<CommitteeLinkView>builder()
                .content(committees.getContent().stream().map(CommitteeEntity::toLink).toList())
                .totalPageNumber(committees.getTotalPages())
                .totalItemNumber((int) committees.getTotalElements())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CommitteeView> findById(Committee.Id committeeId) {
        return boCommitteeQueryRepository.findById(committeeId.value()).map(BoCommitteeQueryEntity::toView);
    }

    @Override
    @Transactional
    public void updateStatus(Committee.Id committeeId, Committee.Status status) {
        committeeRepository.updateStatus(committeeId.value(), CommitteeStatusEntity.fromDomain(status).name());
    }

    @Override
    @Transactional
    public void saveApplication(Committee.Id committeeId, Committee.Application application) {
        committeeApplicationRepository.save(CommitteeApplicationEntity.fromDomain(committeeId, application));
    }
}
