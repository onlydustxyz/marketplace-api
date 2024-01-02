package onlydust.com.marketplace.api.od.rust.api.client.adapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MarkInvoiceAsReceivedDTO {
    List<UUID> payments;
}
