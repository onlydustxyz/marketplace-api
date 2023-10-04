package com.onlydust.shared.write.adapters.secondary.dateprovision;


import com.onlydust.shared.write.hexagon.gateways.dateprovision.DateProvider;

import java.time.LocalDateTime;

public class DeterministicDateProvider implements DateProvider {

    private LocalDateTime now;

    @Override
    public LocalDateTime now() {
        return now;
    }

    public void setNow(LocalDateTime now) {
        this.now = now;
    }
}
