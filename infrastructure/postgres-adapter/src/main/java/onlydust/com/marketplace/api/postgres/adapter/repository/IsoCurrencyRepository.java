package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.IsoCurrencyViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IsoCurrencyRepository extends JpaRepository<IsoCurrencyViewEntity, String> {
}
