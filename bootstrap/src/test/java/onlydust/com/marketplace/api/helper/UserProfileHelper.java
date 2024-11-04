package onlydust.com.marketplace.api.helper;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ContactInformationEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ContactInformationRepository;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Contact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserProfileHelper {
    @Autowired
    ContactInformationRepository contactInformationRepository;

    public void addContact(UserId userId, Contact contact) {
        contactInformationRepository.save(ContactInformationEntity.fromDomain(userId, contact));
    }
}
