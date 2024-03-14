alter table accounting.bank_accounts
    add constraint bank_accounts_billing_profile_id_fk foreign key (billing_profile_id) references accounting.billing_profiles (id);

alter table accounting.billing_profiles_users
    add constraint billing_profiles_users_billing_profile_id_fk foreign key (billing_profile_id) references accounting.billing_profiles (id);

alter table accounting.kyb
    add constraint kyb_billing_profile_id_fk foreign key (billing_profile_id) references accounting.billing_profiles (id);

alter table accounting.kyc
    add constraint kyc_billing_profile_id_fk foreign key (billing_profile_id) references accounting.billing_profiles (id);

alter table accounting.payout_preferences
    add constraint payout_preferences_billing_profile_id_fk foreign key (billing_profile_id) references accounting.billing_profiles (id);

alter table accounting.wallets
    add constraint wallets_billing_profile_id_fk foreign key (billing_profile_id) references accounting.billing_profiles (id);


