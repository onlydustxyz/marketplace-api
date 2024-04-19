package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.HackathonShortViewEntity;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;
import java.util.UUID;

public interface HackathonShortViewRepository extends JpaRepository<HackathonShortViewEntity, UUID> {

    Page<HackathonShortViewEntity> findByStatusIn(Set<Hackathon.Status> statuses, Pageable pageable);

}
