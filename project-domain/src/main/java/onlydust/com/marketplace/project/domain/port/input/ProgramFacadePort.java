package onlydust.com.marketplace.project.domain.port.input;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.Program;

import java.net.URI;

public interface ProgramFacadePort {
    Program create(@NonNull String name, @NonNull URI logoUrl);
}
