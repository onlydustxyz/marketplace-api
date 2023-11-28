package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeApi;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tags(@Tag(name = "Backoffice"))
@AllArgsConstructor
public class BackofficeRestApi implements BackofficeApi {
}
