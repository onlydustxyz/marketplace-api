package onlydust.com.marketplace.api.indexer.api.client.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.port.output.IndexerPort;
import org.springframework.http.HttpMethod;

import java.util.List;

@AllArgsConstructor
public class IndexerApiClientAdapter implements IndexerPort {

    private final IndexerApiHttpClient httpClient;

    @Override
    public void indexUser(Long githubUserId) {
        httpClient.sendRequest("/api/v1/users/" + githubUserId, HttpMethod.PUT, null, Void.class);
    }

    @Override
    public void indexUsers(List<Long> githubUserIds) {
        githubUserIds.stream().parallel().forEach(this::indexUser);
    }
}
