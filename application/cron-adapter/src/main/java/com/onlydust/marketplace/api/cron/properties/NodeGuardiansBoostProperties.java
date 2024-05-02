package com.onlydust.marketplace.api.cron.properties;

import lombok.Data;

import java.util.UUID;

@Data
public class NodeGuardiansBoostProperties {
    private UUID projectId;
    private UUID projectLeadId;
    private Long githubRepoId;
    private UUID ecosystemId;
}
