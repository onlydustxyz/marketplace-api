package onlydust.com.marketplace.api.od.rust.api.client.adapter;

import onlydust.com.marketplace.api.domain.model.RequestRewardCommand;
import onlydust.com.marketplace.api.domain.port.output.RewardStoragePort;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura.HasuraJwtPayload;

public class OdRustApiClientAdapter implements RewardStoragePort<HasuraJwtPayload> {

    @Override
    public void requestPayment(HasuraJwtPayload hasuraJwtPayload, RequestRewardCommand requestRewardCommand) {

    }
}
