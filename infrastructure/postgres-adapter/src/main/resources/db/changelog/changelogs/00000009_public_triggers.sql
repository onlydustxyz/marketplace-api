create trigger applications_set_tech_updated_at
    before update
    on applications
    for each row
execute procedure set_tech_updated_at();


create trigger update_currencies_tech_updated_at
    before update
    on currencies
    for each row
execute procedure set_tech_updated_at();


create trigger ecosystems_set_tech_updated_at
    before update
    on ecosystems
    for each row
execute procedure set_tech_updated_at();

create trigger update_erc20_tech_updated_at
    before update
    on erc20
    for each row
execute procedure set_tech_updated_at();

create trigger indexer_outbox_events_set_tech_updated_at
    before update
    on indexer_outbox_events
    for each row
execute procedure set_tech_updated_at();

create trigger notification_outbox_events_set_tech_updated_at
    before update
    on notification_outbox_events
    for each row
execute procedure set_tech_updated_at();

create trigger project_allowances_set_tech_updated_at
    before update
    on project_allowances
    for each row
execute procedure set_tech_updated_at();

create trigger reward_items_set_tech_updated_at
    before update
    on reward_items
    for each row
execute procedure set_tech_updated_at();

create trigger tracking_outbox_events_set_tech_updated_at
    before update
    on tracking_outbox_events
    for each row
execute procedure set_tech_updated_at();

create trigger public_user_profile_info_set_tech_updated_at
    before update
    on user_profile_info
    for each row
execute procedure set_tech_updated_at();

create trigger hackathons_set_tech_updated_at
    before update
    on hackathons
    for each row
execute procedure set_tech_updated_at();

create trigger hackathon_registrations_set_tech_updated_at
    before update
    on hackathon_registrations
    for each row
execute procedure set_tech_updated_at();

create trigger node_guardians_rewards_boost_outbox_events_set_tech_updated_at
    before update
    on node_guardians_rewards_boost_outbox_events
    for each row
execute procedure set_tech_updated_at();

create trigger languages_set_tech_updated_at
    before update
    on languages
    for each row
execute procedure set_tech_updated_at();

create trigger language_file_extensions_set_tech_updated_at
    before update
    on language_file_extensions
    for each row
execute procedure set_tech_updated_at();

create trigger project_categories_set_tech_updated_at
    before update
    on project_categories
    for each row
execute procedure set_tech_updated_at();

create trigger committees_set_tech_updated_at
    before update
    on committees
    for each row
execute procedure set_tech_updated_at();

create trigger committee_project_questions_set_tech_updated_at
    before update
    on committee_project_questions
    for each row
execute procedure set_tech_updated_at();

create trigger committee_project_answers_set_tech_updated_at
    before update
    on committee_project_answers
    for each row
execute procedure set_tech_updated_at();

create trigger mail_outbox_events_set_tech_updated_at
    before update
    on mail_outbox_events
    for each row
execute procedure set_tech_updated_at();

create trigger committee_jury_criteria_set_tech_updated_at
    before update
    on committee_jury_criteria
    for each row
execute procedure set_tech_updated_at();

create trigger committee_jury_votes_set_tech_updated_at
    before update
    on committee_jury_votes
    for each row
execute procedure set_tech_updated_at();

create trigger committee_budget_allocations_tech_updated_at
    before update
    on committee_budget_allocations
    for each row
execute procedure set_tech_updated_at();

create trigger ecosystem_banners_set_tech_updated_at
    before update
    on ecosystem_banners
    for each row
execute procedure set_tech_updated_at();

create trigger ecosystem_articles_set_tech_updated_at
    before update
    on ecosystem_articles
    for each row
execute procedure set_tech_updated_at();

create trigger ecosystems_articles_set_tech_updated_at
    before update
    on ecosystems_articles
    for each row
execute procedure set_tech_updated_at();

create trigger project_category_suggestions_set_tech_updated_at
    before update
    on project_category_suggestions
    for each row
execute procedure set_tech_updated_at();

create trigger projects_project_categories_set_tech_updated_at
    before update
    on projects_project_categories
    for each row
execute procedure set_tech_updated_at();

create trigger banners_set_tech_updated_at
    before update
    on banners
    for each row
execute procedure set_tech_updated_at();

create trigger banners_closed_by_set_tech_updated_at
    before update
    on banners_closed_by
    for each row
execute procedure set_tech_updated_at();

create trigger hackathon_projects_set_tech_updated_at
    before update
    on hackathon_projects
    for each row
execute procedure set_tech_updated_at();

create trigger hackathon_events_set_tech_updated_at
    before update
    on hackathon_events
    for each row
execute procedure set_tech_updated_at();

create trigger programs_set_tech_updated_at
    before update
    on programs
    for each row
execute procedure set_tech_updated_at();

create trigger program_leads_set_tech_updated_at
    before update
    on program_leads
    for each row
execute procedure set_tech_updated_at();

create trigger ecosystem_leads_set_tech_updated_at
    before update
    on ecosystem_leads
    for each row
execute procedure set_tech_updated_at();

create trigger project_contributor_labels_set_tech_updated_at
    before update
    on project_contributor_labels
    for each row
execute procedure set_tech_updated_at();

create trigger contributor_project_contributor_labels_set_tech_updated_at
    before update
    on contributor_project_contributor_labels
    for each row
execute procedure set_tech_updated_at();

create trigger public_user_project_recommendations_set_tech_updated_at
    before update
    on p_user_project_recommendations
    for each row
execute procedure set_tech_updated_at();

create trigger archived_github_contributions_set_tech_updated_at
    before update
    on archived_github_contributions
    for each row
execute procedure set_tech_updated_at();