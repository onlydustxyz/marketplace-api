package onlydust.com.marketplace.project.domain.port.output;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;

import java.util.List;

public interface FgaPort {
    interface Project {

        void setMaintainers(@NonNull ProjectId projectId, @NonNull List<UserId> userIds);

        void addGrantingProgram(@NonNull ProjectId projectId, @NonNull ProgramId programId);

        boolean canEdit(@NonNull ProjectId projectId, @NonNull UserId userId);

        boolean canEditPermissions(@NonNull ProjectId projectId, @NonNull UserId userId);

        boolean canReadFinancial(@NonNull ProjectId projectId, @NonNull UserId userId);

    }

    /*
https://dashboard.fga.dev/

model
  schema 1.1

# Represents a signed-up user
type user

type billing_profile
  relations
    define admin: [user]
    define member: [user]
    define can_read: admin or member
    define can_write: admin

type reward
  relations
    define project: [project]
    define recipient: [user]
    define can_cancel: maintainer from project
    define can_claim: recipient

type project
  relations
    define maintainer: [user]
    define granting_program: [program]
    define can_edit_maintainers: maintainer
    define can_read_financial: maintainer or can_read_financial from granting_program
    define can_refund_grant: maintainer
    define can_send_reward: maintainer

type program
  relations
    define lead: [user]
    define allocating_sponsor: [sponsor]
    define can_edit_leads: lead or can_edit_a_program from allocating_sponsor
    define can_read_financial: lead or can_read_financial from allocating_sponsor
    define can_refund_allocation: lead
    define can_send_grant: lead

type sponsor
  relations
    define head_of_finance: [user]
    define finance_viewer: [user]
    define ecosystem: [ecosystem]
    define can_read_financial: finance_viewer or head_of_finance or head_of from ecosystem
    define can_send_allocation: head_of_finance
    define can_deposit_funds: head_of_finance
    define can_edit_a_program: head_of_finance

type ecosystem
  relations
    define head_of: [user]

     */
}
