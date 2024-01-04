package onlydust.com.marketplace.api.postgres.adapter.repository.old;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<ApplicationEntity, UUID> {

  Optional<ApplicationEntity> findByProjectIdAndApplicantId(UUID projectId, UUID applicantId);

  List<ApplicationEntity> findAllByApplicantId(UUID id);
}
