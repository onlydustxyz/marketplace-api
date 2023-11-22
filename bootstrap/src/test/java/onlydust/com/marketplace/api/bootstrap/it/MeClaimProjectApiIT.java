package onlydust.com.marketplace.api.bootstrap.it;

import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"hasura_auth"})
public class MeClaimProjectApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    HasuraUserHelper hasuraUserHelper;
    @Autowired
    ProjectRepository projectRepository;


}
