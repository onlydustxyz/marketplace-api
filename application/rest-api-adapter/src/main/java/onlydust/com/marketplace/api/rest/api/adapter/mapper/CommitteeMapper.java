package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.CommitteeApplicationRequest;
import onlydust.com.marketplace.api.contract.model.CommitteeProjectAnswerRequest;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.ProjectQuestion;

import java.util.ArrayList;
import java.util.List;

public interface CommitteeMapper {

    static Committee.Application committeeApplicationRequestToDomain(final CommitteeApplicationRequest committeeApplicationRequest,
                                                                     final UserId userId, final ProjectId projectId) {
        final List<Committee.ProjectAnswer> projectAnswers = new ArrayList<>();
        for (CommitteeProjectAnswerRequest answer : committeeApplicationRequest.getAnswers()) {
            projectAnswers.add(new Committee.ProjectAnswer(ProjectQuestion.Id.of(answer.getQuestionId()), answer.getAnswer()));
        }
        return new Committee.Application(userId, projectId, projectAnswers);
    }
}
