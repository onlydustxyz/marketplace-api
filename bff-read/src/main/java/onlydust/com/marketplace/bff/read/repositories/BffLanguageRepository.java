package onlydust.com.marketplace.bff.read.repositories;

import onlydust.com.marketplace.bff.read.entities.LanguageViewEntity;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface BffLanguageRepository extends Repository<LanguageViewEntity, UUID> {
    List<LanguageViewEntity> findAll();
}
