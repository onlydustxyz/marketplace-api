package onlydust.com.marketplace.api.domain.view.pagination;

import lombok.extern.slf4j.Slf4j;

import static java.lang.Math.min;
import static java.lang.Math.round;


@Slf4j
public class PaginationHelper {

    private static final int MAXIMUM_PAGE_SIZE = 100;

    public static int sanitizePageSize(final int pageSize) {
        return min(pageSize, MAXIMUM_PAGE_SIZE);
    }

    public static int calculateTotalNumberOfPage(final int pageSize, final int count) {
        return count < pageSize ? 1 : round((float) count / pageSize);
    }

    public static boolean hasMore(final int pageIndex, final int totalPageNumber) {
        return (pageIndex + 1) < totalPageNumber;
    }
}
