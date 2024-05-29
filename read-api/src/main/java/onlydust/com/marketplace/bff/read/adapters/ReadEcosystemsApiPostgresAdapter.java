package onlydust.com.marketplace.bff.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadEcosystemsApi;
import onlydust.com.marketplace.api.contract.model.EcosystemProjectPageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
public class ReadEcosystemsApiPostgresAdapter implements ReadEcosystemsApi {

    @Override
    public ResponseEntity<EcosystemProjectPageResponse> getEcosystemProjects(UUID ecosystemId, Integer pageIndex, Integer pageSize,
                                                                             Boolean hasGoodFirstIssues) {
        return ReadEcosystemsApi.super.getEcosystemProjects(ecosystemId, pageIndex, pageSize, hasGoodFirstIssues);
    }
}
