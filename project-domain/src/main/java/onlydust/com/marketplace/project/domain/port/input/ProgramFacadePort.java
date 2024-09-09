package onlydust.com.marketplace.project.domain.port.input;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Program;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.UUID;

public interface ProgramFacadePort {
    Program create(@NonNull String name, @NonNull SponsorId sponsorId, URI url, URI logoUrl, @NonNull List<UserId> leadIds);

    void update(@NonNull UUID programId, @NonNull String name, URI url, URI logoUrl, @NonNull List<UserId> leadIds);

    URL uploadLogo(InputStream imageInputStream);
}
