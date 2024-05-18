package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.backoffice.api.contract.model.CommitteeResponse;
import onlydust.com.marketplace.project.domain.model.Committee;

public interface CommitteeMapper {

    static CommitteeResponse toCommitteeResponse(Committee committee) {
        final CommitteeResponse committeeResponse = new CommitteeResponse();
        committeeResponse.setName(committee.name());
        committeeResponse.setId(committee.id().value());
        committeeResponse.setStartDate(committee.startDate());
        committeeResponse.setEndDate(committee.endDate());
        return committeeResponse;
    }
}
