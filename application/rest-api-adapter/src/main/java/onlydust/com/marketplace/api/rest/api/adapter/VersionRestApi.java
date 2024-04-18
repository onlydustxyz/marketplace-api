package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.VersionApi;
import onlydust.com.marketplace.api.contract.model.GetAPIVersion200Response;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@Tags(@Tag(name = "Version"))
@AllArgsConstructor
public class VersionRestApi implements VersionApi {

    private final Date startingDate;

    @Override
    public ResponseEntity<GetAPIVersion200Response> getAPIVersion() {
        final GetAPIVersion200Response response = new GetAPIVersion200Response();
        response.setReleaseDate(DateMapper.toZoneDateTime(startingDate));
        return ResponseEntity.ok(response);
    }
}
