package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.project.domain.port.input.BannerFacadePort;
import onlydust.com.marketplace.project.domain.port.output.BannerStoragePort;

@AllArgsConstructor
public class BannerService implements BannerFacadePort {
    private final BannerStoragePort bannerStoragePort;
}
