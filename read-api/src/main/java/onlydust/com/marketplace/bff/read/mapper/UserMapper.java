package onlydust.com.marketplace.bff.read.mapper;

import onlydust.com.backoffice.api.contract.model.UserPage;
import onlydust.com.marketplace.bff.read.entities.user.AllUserReadEntity;
import org.springframework.data.domain.Page;

import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.hasMore;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.nextPageIndex;

public interface UserMapper {

    static UserPage pageToResponse(final Page<AllUserReadEntity> page, final int pageIndex) {
        return new UserPage()
                .users(page.getContent().stream().map(AllUserReadEntity::toBoPageItemResponse).toList())
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .hasMore(hasMore(pageIndex, page.getTotalPages()))
                .nextPageIndex(nextPageIndex(pageIndex, page.getTotalPages()));
    }
}
