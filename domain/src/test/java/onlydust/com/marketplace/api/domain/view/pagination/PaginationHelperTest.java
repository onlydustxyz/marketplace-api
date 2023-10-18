package onlydust.com.marketplace.api.domain.view.pagination;

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
        assertEquals(100, sanitizedPageSize);
    }

    @Test
    void should_compute_page_total_number_from_count() {
        assertEquals(2, PaginationHelper.calculateTotalNumberOfPage(5, 10));
        assertEquals(2, PaginationHelper.calculateTotalNumberOfPage(5, 9));
        assertEquals(1, PaginationHelper.calculateTotalNumberOfPage(5, 1));
    }
}
