package onlydust.com.marketplace.api.postgres.adapter.entity.recommendation;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Immutable
public class ProjectRecommendationEntity {
    @Id
    private UUID projectId;
    private BigDecimal score;
}