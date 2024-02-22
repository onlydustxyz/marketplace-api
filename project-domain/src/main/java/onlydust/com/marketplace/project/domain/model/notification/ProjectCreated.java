package onlydust.com.marketplace.project.domain.model.notification;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.kernel.model.Event;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("ProjectCreated")
public class ProjectCreated extends Event {
    UUID projectId;
    Date createdAt;
}
