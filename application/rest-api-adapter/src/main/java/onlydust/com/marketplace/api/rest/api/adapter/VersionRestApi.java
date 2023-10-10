package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import onlydust.com.marketplace.api.contract.VersionApi;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tags(@Tag(name = "Version"))
public class VersionRestApi implements VersionApi {
}
