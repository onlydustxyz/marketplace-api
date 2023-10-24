package onlydust.com.marketplace.api.od.rust.api.client.adapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestRewardResponseDTO {
    UUID paymentId;
    UUID commandId;
}
