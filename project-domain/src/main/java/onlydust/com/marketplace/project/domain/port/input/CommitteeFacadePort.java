package onlydust.com.marketplace.project.domain.port.input;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.JuryCriteria;
import onlydust.com.marketplace.project.domain.view.commitee.CommitteeLinkView;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

public interface CommitteeFacadePort {
    Committee createCommittee(@NonNull String name,
                              @NonNull ZonedDateTime startDate,
                              @NonNull ZonedDateTime endDate);

    Page<CommitteeLinkView> getCommittees(Integer pageIndex, Integer pageSize);

    void update(Committee committee);

    void updateStatus(Committee.Id committeeId, Committee.Status status);

    void createUpdateApplicationForCommittee(Committee.Id committeeId, Committee.Application application);

    void vote(UserId juryId, Committee.Id committeeId, ProjectId projectId, Map<JuryCriteria.Id, Integer> scores);

    void allocate(Committee.Id committeeId, UUID currencyId, BigDecimal budget, int precision);

    void saveAllocations(Committee.Id committeeId, UUID currencyId, Map<ProjectId, BigDecimal> projectAllocations);
}
