package onlydust.com.marketplace.api.read.entities.project;

import java.util.UUID;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.ProjectLinkWithDescriptionResponse;

@Entity
@Immutable
@NoArgsConstructor(force = true)
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SimilarProjectReadEntity {
    @Id
    UUID id;
    
    @JdbcTypeCode(SqlTypes.JSON)
    ProjectLinkWithDescriptionResponse project;

    public ProjectLinkWithDescriptionResponse toResponse() {
        return project;
    }
} 