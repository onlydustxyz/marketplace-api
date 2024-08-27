package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProgramLeadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramLeadRepository extends JpaRepository<ProgramLeadEntity, ProgramLeadEntity.PrimaryKey> {
}
