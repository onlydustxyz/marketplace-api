package onlydust.com.marketplace.api.postgres.adapter.repository.old;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<ApplicationEntity, UUID> {
    Optional<ApplicationEntity> findByProjectIdAndApplicantId(UUID projectId, Long applicantId);

    List<ApplicationEntity> findAllByApplicantId(Long applicantId);

    List<ApplicationEntity> findAllByApplicantIdAndProjectIdAndIssueId(Long applicantId, UUID projectId, Long value);
}
