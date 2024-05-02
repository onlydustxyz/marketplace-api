package onlydust.com.marketplace.api.node.guardians;

import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.project.domain.port.output.NodeGuardiansApiPort;

import java.util.Optional;

@AllArgsConstructor
public class NodeGuardiansApiAdapter implements NodeGuardiansApiPort {

    private final NodeGuardiansHttpClient nodeGuardiansHttpClient;

    @Override
    public Optional<Integer> getContributorLevel(String githubLogin) {
        return nodeGuardiansHttpClient.send("/api/partnership/only-dust/%s".formatted(githubLogin), HttpMethod.GET, Void.class, ContributorLevelDTO.class)
                .map(ContributorLevelDTO::getLevel);
    }
}
