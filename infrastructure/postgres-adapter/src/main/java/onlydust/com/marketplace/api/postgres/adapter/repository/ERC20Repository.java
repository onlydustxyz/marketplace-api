package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.ERC20Entity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ERC20Repository extends JpaRepository<ERC20Entity, ERC20Entity.PrimaryKey> {
}
