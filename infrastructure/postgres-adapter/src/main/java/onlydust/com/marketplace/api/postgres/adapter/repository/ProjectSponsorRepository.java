package onlydust.com.marketplace.api.postgres.adapter.repository;


import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectSponsorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectSponsorRepository extends JpaRepository<ProjectSponsorEntity, ProjectSponsorEntity.PrimaryKey> {

}
