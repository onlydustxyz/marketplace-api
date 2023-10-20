package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;
import org.junit.jupiter.api.Test;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.SortDirectionMapper.requestToDomain;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SortDirectionMapperTest {

    @Test
    void should_map_sort_direction_to_domain() {
        assertEquals(SortDirection.asc, requestToDomain("ASC"));
        assertEquals(SortDirection.asc, requestToDomain(""));
        assertEquals(SortDirection.asc, requestToDomain(null));
        assertEquals(SortDirection.desc, requestToDomain("DESC"));
    }
}
