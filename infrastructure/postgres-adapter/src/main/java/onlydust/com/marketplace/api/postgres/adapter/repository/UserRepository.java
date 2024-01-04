package onlydust.com.marketplace.api.postgres.adapter.repository;

import java.util.UUID;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserRepository extends JpaRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity> {

}
