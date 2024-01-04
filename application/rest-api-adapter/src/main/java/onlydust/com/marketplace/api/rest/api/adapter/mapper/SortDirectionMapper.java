package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import static java.util.Objects.isNull;

import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;

public interface SortDirectionMapper {

  static SortDirection requestToDomain(final String direction) {
    return isNull(direction) || direction.isEmpty() ? SortDirection.asc : switch (direction) {
      case "DESC" -> SortDirection.desc;
      default -> SortDirection.asc;
    };
  }
}
