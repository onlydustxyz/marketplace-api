package onlydust.com.marketplace.api.postgres.adapter.entity.write;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "sumsub_rejection_reasons", schema = "accounting")
public class SumsubRejectionReasonEntity {
    @Id
    @NonNull
    UUID id;
    @NonNull
    String button;
    @NonNull
    String buttonId;
    String groupId;
    @NonNull
    String associatedRejectionLabel;
    @NonNull
    String description;
}
