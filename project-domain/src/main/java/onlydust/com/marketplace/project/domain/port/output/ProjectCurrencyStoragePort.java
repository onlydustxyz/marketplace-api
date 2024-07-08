package onlydust.com.marketplace.project.domain.port.output;

import java.util.Optional;
import java.util.UUID;

public interface ProjectCurrencyStoragePort {

    Optional<UUID> findCurrencyIdByCode(String code);
}
