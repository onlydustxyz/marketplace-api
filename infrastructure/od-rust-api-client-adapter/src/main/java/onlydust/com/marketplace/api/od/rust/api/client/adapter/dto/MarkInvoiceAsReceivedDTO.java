package onlydust.com.marketplace.api.od.rust.api.client.adapter.dto;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MarkInvoiceAsReceivedDTO {

  List<UUID> payments;
}
