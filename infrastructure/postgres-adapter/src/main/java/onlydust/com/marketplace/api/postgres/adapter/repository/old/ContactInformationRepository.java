package onlydust.com.marketplace.api.postgres.adapter.repository.old;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ContactInformationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ContactInformationIdEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactInformationRepository extends JpaRepository<ContactInformationEntity,
        ContactInformationIdEntity> {
}
