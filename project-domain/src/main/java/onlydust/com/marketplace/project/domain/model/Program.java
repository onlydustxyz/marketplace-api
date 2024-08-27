package onlydust.com.marketplace.project.domain.model;

import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.kernel.model.UserId;

import java.net.URI;
import java.util.List;

@Builder(toBuilder = true)
public record Program(@NonNull ProgramId id,
                      @NonNull String name,
                      @NonNull SponsorId sponsorId,
                      @NonNull List<UserId> leadIds,
                      URI url,
                      URI logoUrl) {
    public static Program create(@NonNull String name, @NonNull SponsorId sponsorId, @NonNull List<UserId> programLeadIds, URI url, URI logoUrl) {
        return Program.builder()
                .id(ProgramId.random())
                .name(name)
                .sponsorId(sponsorId)
                .leadIds(programLeadIds)
                .url(url)
                .logoUrl(logoUrl)
                .build();
    }
}
