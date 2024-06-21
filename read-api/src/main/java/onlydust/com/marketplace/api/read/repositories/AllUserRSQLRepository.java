package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.user.rsql.AllUserRSQLEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface AllUserRSQLRepository extends JpaRepository<AllUserRSQLEntity, UUID>, JpaSpecificationExecutor<AllUserRSQLEntity> {
}
