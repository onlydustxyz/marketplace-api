package onlydust.com.marketplace.bff.read.repositories;

import onlydust.com.marketplace.bff.read.entities.RecentPublicActivityReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RecentPublicActivityReadRepository extends JpaRepository<RecentPublicActivityReadEntity, RecentPublicActivityReadEntity.PrimaryKey> {

    @Query(value = """
            select a
            from RecentPublicActivityReadEntity a
            join fetch a.project
            left join fetch a.pullRequestAuthor
            left join fetch a.reward
            order by a.timestamp desc
            """)
    Page<RecentPublicActivityReadEntity> findLastActivity(Pageable pageable);
}
