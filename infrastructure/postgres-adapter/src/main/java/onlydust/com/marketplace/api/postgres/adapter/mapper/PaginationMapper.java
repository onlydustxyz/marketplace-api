package onlydust.com.marketplace.api.postgres.adapter.mapper;

public interface PaginationMapper {

  static Integer getPostgresOffsetFromPagination(int pageSize, int pageIndex) {
    return pageIndex * pageSize;
  }

  static Integer getPostgresLimitFromPagination(int pageSize, int pageIndex) {
    return pageSize;
  }

}
