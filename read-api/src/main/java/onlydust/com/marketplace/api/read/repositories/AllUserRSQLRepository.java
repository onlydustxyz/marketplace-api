package onlydust.com.marketplace.api.read.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import onlydust.com.marketplace.api.read.entities.user.rsql.AllUserRSQLEntity;

public interface AllUserRSQLRepository extends JpaRepository<AllUserRSQLEntity, Long>, JpaSpecificationExecutor<AllUserRSQLEntity> {
}
