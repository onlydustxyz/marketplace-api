package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.CommitteeEntity;
import onlydust.com.marketplace.project.domain.model.Committee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface CommitteeRepository extends JpaRepository<CommitteeEntity, UUID> {

    @Modifying
    @Query(nativeQuery = true, value = """
                update committees
                set status = cast(:status as committee_status)
                where id = :committeeId
            """)
    void updateStatus(UUID committeeId, String status);
}
