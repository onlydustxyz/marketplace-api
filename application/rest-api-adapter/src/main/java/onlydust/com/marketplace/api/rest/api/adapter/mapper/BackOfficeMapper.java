package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.backoffice.api.contract.model.BudgetPage;
import onlydust.com.backoffice.api.contract.model.BudgetResponse;
import onlydust.com.backoffice.api.contract.model.GithubRepositoryPage;
import onlydust.com.backoffice.api.contract.model.GithubRepositoryResponse;
import onlydust.com.marketplace.api.domain.view.backoffice.ProjectBudgetView;
import onlydust.com.marketplace.api.domain.view.backoffice.ProjectRepositoryView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper;

public interface BackOfficeMapper {

    static GithubRepositoryPage mapGithubRepositoryPageToContract(Page<ProjectRepositoryView> projectRepositoryViewPage, int sanitizedPageIndex) {
        final GithubRepositoryPage githubRepositoryPage = new GithubRepositoryPage();
        for (ProjectRepositoryView projectRepositoryView : projectRepositoryViewPage.getContent()) {
            githubRepositoryPage.addGithubRepositoriesItem(new GithubRepositoryResponse()
                    .id(projectRepositoryView.getId())
                    .projectId(projectRepositoryView.getProjectId())
                    .owner(projectRepositoryView.getOwner())
                    .technologies(projectRepositoryView.getTechnologies())
                    .name(projectRepositoryView.getName()));
        }
        githubRepositoryPage.setNextPageIndex(PaginationHelper.nextPageIndex(sanitizedPageIndex,
                projectRepositoryViewPage.getTotalPageNumber()));
        githubRepositoryPage.setTotalPageNumber(projectRepositoryViewPage.getTotalPageNumber());
        githubRepositoryPage.setTotalItemNumber(projectRepositoryViewPage.getTotalItemNumber());
        githubRepositoryPage.setHasMore(PaginationHelper.hasMore(sanitizedPageIndex,
                projectRepositoryViewPage.getTotalPageNumber()));
        return githubRepositoryPage;
    }

    static BudgetPage mapBudgetPageToContract(Page<ProjectBudgetView> projectBudgetViewPage, int sanitizedPageIndex) {
        final BudgetPage budgetPage = new BudgetPage();
        for (ProjectBudgetView view : projectBudgetViewPage.getContent()) {
            budgetPage.addBudgetsItem(new BudgetResponse()
                    .initialAmount(view.getInitialAmount())
                    .remainingAmount(view.getRemainingAmount())
                    .spentAmount(view.getSpentAmount())
                    .id(view.getId())
                    .projectId(view.getProjectId())
                    .initialAmountDollarsEquivalent(view.getInitialAmountDollarsEquivalent())
                    .remainingAmountDollarsEquivalent(view.getRemainingAmountDollarsEquivalent())
                    .spentAmountDollarsEquivalent(view.getSpentAmountDollarsEquivalent()));
        }
        budgetPage.setNextPageIndex(PaginationHelper.nextPageIndex(sanitizedPageIndex,
                projectBudgetViewPage.getTotalPageNumber()));
        budgetPage.setTotalPageNumber(projectBudgetViewPage.getTotalPageNumber());
        budgetPage.setTotalItemNumber(projectBudgetViewPage.getTotalItemNumber());
        budgetPage.setHasMore(PaginationHelper.hasMore(sanitizedPageIndex,
                projectBudgetViewPage.getTotalPageNumber()));
        return budgetPage;
    }
}
