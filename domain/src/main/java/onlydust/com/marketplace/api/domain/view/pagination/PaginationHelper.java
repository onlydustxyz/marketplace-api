package onlydust.com.marketplace.api.domain.view.pagination;

import lombok.extern.slf4j.Slf4j;

import static java.lang.Math.min;
import static java.lang.Math.round;
import static java.util.Objects.isNull;


@Slf4j
public class PaginationHelper {

    private static final int MAXIMUM_PAGE_SIZE = 1000;
    private static final int DEFAULT_PAGE_INDEX = 0;
    private static final int DEFAULT_PAGE_SIZE = 50;

    public static int sanitizePageSize(final Integer pageSize) {
        return isNull(pageSize) || pageSize.equals(0) ? DEFAULT_PAGE_SIZE : min(pageSize, MAXIMUM_PAGE_SIZE);
    }

    public static int sanitizePageIndex(final Integer pageIndex) {
        return isNull(pageIndex) ? DEFAULT_PAGE_INDEX : pageIndex;
    }

    public static int calculateTotalNumberOfPage(final int pageSize, final int count) {
        return count < pageSize ? 1 : round((float) count / pageSize);
    }

    public static boolean hasMore(final int pageIndex, final int totalPageNumber) {
        return (pageIndex + 1) < totalPageNumber;
    }

    public static Integer nextPageIndex(final int pageIndex, final int totalPageNumber) {
        return hasMore(pageIndex, totalPageNumber) ? pageIndex + 1 : pageIndex;
    }
}
