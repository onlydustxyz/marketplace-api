package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.kernel.pagination.SortDirection;

import static java.util.Objects.isNull;

public interface SortDirectionMapper {

    static SortDirection requestToDomain(final String direction) {
        return isNull(direction) || direction.isEmpty() ? SortDirection.asc : switch (direction) {
            case "DESC" -> SortDirection.desc;
            default -> SortDirection.asc;
        };
    }
}
