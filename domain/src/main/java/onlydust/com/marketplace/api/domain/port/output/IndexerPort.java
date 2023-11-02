package onlydust.com.marketplace.api.domain.port.output;

import java.util.List;

public interface IndexerPort {
    void indexUser(Long githubUserId);

    void indexUsers(List<Long> githubUserIds);
}
