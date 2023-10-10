package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import onlydust.com.marketplace.api.contract.TechnologiesApi;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tags(@Tag(name = "Technologies"))
public class TechnologiesRestApi implements TechnologiesApi {
}
