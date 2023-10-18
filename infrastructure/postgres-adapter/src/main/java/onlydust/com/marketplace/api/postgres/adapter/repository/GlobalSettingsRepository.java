package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.GlobalSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GlobalSettingsRepository extends JpaRepository<GlobalSettingsEntity, Integer>, JpaSpecificationExecutor<GlobalSettingsEntity> {

}
