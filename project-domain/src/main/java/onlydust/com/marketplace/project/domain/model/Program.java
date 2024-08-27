package onlydust.com.marketplace.project.domain.model;

import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProgramId;

import java.net.URI;

@Builder(toBuilder = true)
public record Program(@NonNull ProgramId id,
                      @NonNull String name,
                      URI url,
                      URI logoUrl) {
    public static Program create(@NonNull String name, URI url, URI logoUrl) {
        return Program.builder()
                .id(ProgramId.random())
                .name(name)
                .url(url)
                .logoUrl(logoUrl)
                .build();
    }
}
