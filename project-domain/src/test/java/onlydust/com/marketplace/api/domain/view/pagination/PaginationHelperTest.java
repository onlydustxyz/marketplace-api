package onlydust.com.marketplace.project.domain.view.pagination;

import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PaginationHelperTest {

    @Test
    void should_sanitize_page_size_given_inferior_size() {
        // Given
        final int pageSize = 10;

        // When
        final int sanitizedPageSize = PaginationHelper.sanitizePageSize(pageSize);

        // Then
        assertEquals(pageSize, sanitizedPageSize);
    }

    @Test
    void should_sanitize_page_size_given_superior_size() {
        // Given
        final int pageSize = 5000;

        // When
        final int sanitizedPageSize = PaginationHelper.sanitizePageSize(pageSize);

        // Then
        assertEquals(1000, sanitizedPageSize);
    }

    @Test
    void should_sanitize_page_index() {
        // Then
        assertEquals(0, PaginationHelper.sanitizePageIndex(null));
        assertEquals(0, PaginationHelper.sanitizePageIndex(0));
        assertEquals(1, PaginationHelper.sanitizePageIndex(1));
    }

    @Test
    void should_sanitize_page_size() {
        // Then
        assertEquals(50, PaginationHelper.sanitizePageSize(null));
    }

    @Test
    void should_compute_page_total_number_from_count() {
        assertEquals(2, PaginationHelper.calculateTotalNumberOfPage(20, 26));
        assertEquals(2, PaginationHelper.calculateTotalNumberOfPage(5, 10));
        assertEquals(2, PaginationHelper.calculateTotalNumberOfPage(5, 9));
        assertEquals(1, PaginationHelper.calculateTotalNumberOfPage(5, 1));
    }

    @Test
    void should_has_more_and_next_page() {
        assertEquals(false, PaginationHelper.hasMore(0, 1));
        assertEquals(0, PaginationHelper.nextPageIndex(0, 1));
        assertEquals(false, PaginationHelper.hasMore(1, 2));
        assertEquals(1, PaginationHelper.nextPageIndex(1, 2));
        assertEquals(true, PaginationHelper.hasMore(0, 2));
        assertEquals(1, PaginationHelper.nextPageIndex(0, 2));
        assertEquals(true, PaginationHelper.hasMore(0, 2));
    }
}
