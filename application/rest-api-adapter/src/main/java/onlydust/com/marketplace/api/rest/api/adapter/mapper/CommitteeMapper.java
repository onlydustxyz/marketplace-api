package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.CommitteeApplicationRequest;
import onlydust.com.marketplace.api.contract.model.CommitteeProjectAnswerRequest;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.ProjectQuestion;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface CommitteeMapper {

    static Committee.Application committeeApplicationRequestToDomain(final CommitteeApplicationRequest committeeApplicationRequest,
                                                                     final UUID userId, final UUID projectId) {
        final List<Committee.ProjectAnswer> projectAnswers = new ArrayList<>();
        for (CommitteeProjectAnswerRequest answer : committeeApplicationRequest.getAnswers()) {
            projectAnswers.add(new Committee.ProjectAnswer(ProjectQuestion.Id.of(answer.getQuestionId()), answer.getAnswer()));
        }
        return new Committee.Application(userId, projectId, projectAnswers);
    }
}
