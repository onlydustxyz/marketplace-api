package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.CreateIssueResponse;
import onlydust.com.marketplace.api.domain.view.CreatedAndClosedIssueView;

import static java.util.Objects.isNull;

public interface IssueMapper {

    static CreateIssueResponse toResponse(final CreatedAndClosedIssueView view) {
        final CreateIssueResponse createIssueResponse = new CreateIssueResponse();
        createIssueResponse.setCreatedAt(DateMapper.toZoneDateTime(view.getCreatedAt()));
        createIssueResponse.setUpdatedAt(DateMapper.toZoneDateTime(view.getUpdatedAt()));
        createIssueResponse.setCloseAt(DateMapper.toZoneDateTime(view.getClosedAt()));
        createIssueResponse.setId(view.getId());
        createIssueResponse.setNumber(view.getNumber());
        createIssueResponse.setRepoId(view.getRepoId());
        createIssueResponse.setTitle(view.getTitle());
        createIssueResponse.setCommentsCount(view.getCommentsCount());
        createIssueResponse.setHtmlUrl(view.getHtmlUrl());
        createIssueResponse.setStatus(isNull(view.getStatus()) ? null : switch (view.getStatus()) {
            case OPEN -> CreateIssueResponse.StatusEnum.OPEN;
            case CANCELLED -> CreateIssueResponse.StatusEnum.CANCELED;
            case CLOSED -> CreateIssueResponse.StatusEnum.CLOSED;
        });
        return createIssueResponse;
    }
}
