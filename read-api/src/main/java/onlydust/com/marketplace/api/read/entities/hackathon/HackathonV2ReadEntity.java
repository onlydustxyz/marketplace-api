package onlydust.com.marketplace.api.read.entities.hackathon;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.HackathonResponseV2;
import onlydust.com.marketplace.api.contract.model.SimpleLink;

@NoArgsConstructor(force = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
@Entity
public class HackathonV2ReadEntity {
    @Id
    UUID id;
    String slug;
    String title;
    String description;
    String location;
    ZonedDateTime startDate;
    ZonedDateTime endDate;

    @JdbcTypeCode(SqlTypes.JSON)
    List<SimpleLink> communityLinks;

    @JdbcTypeCode(SqlTypes.JSON)
    List<SimpleLink> links;

    Integer issueCount;
    Integer availableIssueCount;
    Integer projectCount;
    Integer subscriberCount;

    public HackathonResponseV2 toResponse() {
        return new HackathonResponseV2()
                .id(id)
                .slug(slug)
                .title(title)
                .description(description)
                .location(location)
                .subscriberCount(subscriberCount)
                .issueCount(issueCount)
                .availableIssueCount(availableIssueCount)
                .startDate(startDate)
                .endDate(endDate)
                .communityLinks(communityLinks)
                .links(links)
                .projectCount(projectCount);
    }
}
