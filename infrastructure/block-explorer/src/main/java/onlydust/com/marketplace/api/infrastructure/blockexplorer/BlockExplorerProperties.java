package onlydust.com.marketplace.api.infrastructure.blockexplorer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.kernel.model.Environment;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockExplorerProperties {
    private Environment environment;
}
