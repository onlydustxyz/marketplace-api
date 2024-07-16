package onlydust.com.marketplace.api.read.entities.hackathon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.contract.model.HackathonProjectIssuesResponse;
import onlydust.com.marketplace.api.contract.model.ProjectLinkResponse;
import org.hibernate.annotations.Immutable;

import java.util.UUID;


@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Immutable
public class HackathonProjectIssuesReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @Column(nullable = false)
    UUID id;
    String name;
    String logoUrl;
    String slug;

    Integer issueCount;

    public HackathonProjectIssuesResponse toDto() {
        return new HackathonProjectIssuesResponse()
                .project(toLinkResponse())
                .issueCount(issueCount);
    }

    private ProjectLinkResponse toLinkResponse() {
        return new ProjectLinkResponse()
                .id(id)
                .name(name)
                .logoUrl(logoUrl)
                .slug(slug);
    }
}
