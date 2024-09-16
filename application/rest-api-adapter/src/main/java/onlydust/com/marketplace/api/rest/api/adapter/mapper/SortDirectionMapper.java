package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.kernel.pagination.SortDirection;

public interface SortDirectionMapper {

    static SortDirection requestToDomain(final onlydust.com.marketplace.api.contract.model.SortDirection direction) {
        return direction == onlydust.com.marketplace.api.contract.model.SortDirection.DESC ? SortDirection.desc : SortDirection.asc;
    }
}
