package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.OldChildrenKycEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OldChildrenKycRepository extends JpaRepository<OldChildrenKycEntity, String> {

    List<OldChildrenKycEntity> findAllByParentApplicantId(String parentApplicantId);
}
