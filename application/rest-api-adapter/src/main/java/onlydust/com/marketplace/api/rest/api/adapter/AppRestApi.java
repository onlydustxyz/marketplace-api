package onlydust.com.marketplace.api.rest.api.adapter;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
public class AppRestApi {

    @GetMapping("/")
    public RedirectView redirectRootPathToHealthCheck() {
        return new RedirectView("/actuator/health");
    }


}
