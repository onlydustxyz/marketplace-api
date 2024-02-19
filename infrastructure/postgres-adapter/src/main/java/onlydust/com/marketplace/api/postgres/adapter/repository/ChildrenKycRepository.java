package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.ChildrenKycEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChildrenKycRepository extends JpaRepository<ChildrenKycEntity, String> {

    List<ChildrenKycEntity> findAllByParentApplicantId(String parentApplicantId);
}
