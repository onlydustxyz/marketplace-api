package onlydust.com.marketplace.project.domain.view.commitee;

import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.JuryAssignment;
import onlydust.com.marketplace.project.domain.model.JuryCriteria;
import onlydust.com.marketplace.project.domain.model.ProjectQuestion;
import onlydust.com.marketplace.project.domain.view.RegisteredContributorLinkView;
import onlydust.com.marketplace.project.domain.view.ShortSponsorView;

import java.time.ZonedDateTime;
import java.util.List;

@Builder
public record CommitteeView(@NonNull Committee.Id id, @NonNull String name, @NonNull ZonedDateTime applicationStartDate,
                            @NonNull ZonedDateTime applicationEndDate,
                            @NonNull Committee.Status status, ShortSponsorView sponsor, List<ProjectQuestion> projectQuestions,
                            List<CommitteeApplicationLinkView> committeeApplicationLinks, List<RegisteredContributorLinkView> juries,
                            List<JuryCriteria> juryCriteria, Integer votePerJury, List<JuryAssignmentView> juryAssignments) {
}
