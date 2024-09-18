package onlydust.com.marketplace.project.domain.port.input;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Ecosystem;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.UUID;

public interface EcosystemFacadePort {
    Ecosystem createEcosystem(@NonNull String name,
                              @NonNull String url,
                              @NonNull String logoUrl,
                              @NonNull String description,
                              @NonNull Boolean hidden,
                              @NonNull List<UserId> leads);

    void updateEcosystem(@NonNull UUID ecosystemId,
                         @NonNull String name,
                         @NonNull String url,
                         @NonNull String logoUrl,
                         @NonNull String description,
                         @NonNull Boolean hidden,
                         @NonNull List<UserId> leads);

    URL uploadLogo(InputStream imageInputStream);
}
