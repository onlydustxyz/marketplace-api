select setval('accounting.billing_profile_verification_outbox_events_id_seq',
              (select max(id)
               from accounting.billing_profile_verification_outbox_events));