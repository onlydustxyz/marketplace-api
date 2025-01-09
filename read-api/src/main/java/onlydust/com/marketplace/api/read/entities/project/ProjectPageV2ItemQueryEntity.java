package onlydust.com.marketplace.api.read.entities.project;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor(force = true)
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
@Immutable
@Entity
public class ProjectPageV2ItemQueryEntity extends BaseProjectPageV2ItemQueryEntity {
    
}
