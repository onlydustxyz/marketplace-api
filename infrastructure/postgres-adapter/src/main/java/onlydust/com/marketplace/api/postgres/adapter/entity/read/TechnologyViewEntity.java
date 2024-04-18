package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
@Value
@EqualsAndHashCode
@NoArgsConstructor(force = true)
public class TechnologyViewEntity {
    @Id
    String technology;
}
