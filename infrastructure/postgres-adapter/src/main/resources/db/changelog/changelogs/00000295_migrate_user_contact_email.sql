update iam.users
set email = ci.contact
from contact_informations ci
where ci.user_id = users.id
  and ci.channel = 'email';

delete
from contact_informations
where channel = 'email';

alter type contact_channel rename to contact_channel_old;

create type contact_channel as enum ('telegram', 'twitter', 'discord', 'linkedin', 'whatsapp');

alter table contact_informations
    alter column channel type contact_channel using channel::text::contact_channel;

drop type contact_channel_old;

alter type contact_channel rename value 'telegram' to 'TELEGRAM';
alter type contact_channel rename value 'twitter' to 'TWITTER';
alter type contact_channel rename value 'discord' to 'DISCORD';
alter type contact_channel rename value 'linkedin' to 'LINKEDIN';
alter type contact_channel rename value 'whatsapp' to 'WHATSAPP';

