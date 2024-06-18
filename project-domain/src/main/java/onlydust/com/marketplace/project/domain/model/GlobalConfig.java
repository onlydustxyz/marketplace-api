package onlydust.com.marketplace.project.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalConfig {
    String appBaseUrl;
}
