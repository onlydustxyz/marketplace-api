package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.BillingProfileUserInvitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingProfileUserInvitationRepository extends JpaRepository<BillingProfileUserInvitationEntity,
        BillingProfileUserInvitationEntity.PrimaryKey> {

}
