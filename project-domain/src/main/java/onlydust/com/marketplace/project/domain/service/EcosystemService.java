package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.port.output.ImageStoragePort;
import onlydust.com.marketplace.project.domain.model.Ecosystem;
import onlydust.com.marketplace.project.domain.port.input.EcosystemFacadePort;
import onlydust.com.marketplace.project.domain.port.output.EcosystemStorage;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class EcosystemService implements EcosystemFacadePort {
    private final EcosystemStorage ecosystemStorage;
    private final ImageStoragePort imageStoragePort;

    @Override
    public Ecosystem createEcosystem(@NonNull String name,
                                     @NonNull String url,
                                     @NonNull String logoUrl,
                                     @NonNull String description,
                                     @NonNull Boolean hidden,
                                     @NonNull List<UserId> leads) {
        final var ecosystem = Ecosystem.create(name,
                URI.create(url),
                URI.create(logoUrl),
                description,
                hidden,
                leads);
        ecosystemStorage.save(ecosystem);
        return ecosystem;
    }

    @Override
    public void updateEcosystem(@NonNull UUID ecosystemId,
                                @NonNull String name,
                                @NonNull String url,
                                @NonNull String logoUrl,
                                @NonNull String description,
                                @NonNull Boolean hidden,
                                @NonNull List<UserId> leads) {
        final var ecosystem = ecosystemStorage.get(ecosystemId)
                .orElseThrow(() -> notFound("Ecosystem %s not found".formatted(ecosystemId)));

        ecosystemStorage.save(ecosystem.toBuilder()
                .name(name)
                .url(URI.create(url))
                .logoUrl(URI.create(logoUrl))
                .description(description)
                .hidden(hidden)
                .leads(leads)
                .build());
    }

    @Override
    public URL uploadLogo(InputStream imageInputStream) {
        return imageStoragePort.storeImage(imageInputStream);
    }
}
