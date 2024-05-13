package onlydust.com.marketplace.bff.read.mapper;

import lombok.NonNull;
import onlydust.com.marketplace.api.contract.model.ContactInformation;
import onlydust.com.marketplace.api.contract.model.ContactInformationChannel;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ContactInformationViewEntity;

public interface ContactMapper {
    static ContactInformation map(final @NonNull ContactInformationViewEntity contact) {
        return new ContactInformation()
                .channel(map(contact.channel()))
                .contact(contact.contact())
                .visibility(contact.isPublic() ? ContactInformation.VisibilityEnum.PUBLIC : ContactInformation.VisibilityEnum.PRIVATE);
    }

    static ContactInformationChannel map(final @NonNull ContactInformationViewEntity.Channel channel) {
        return switch (channel) {
            case email -> ContactInformationChannel.EMAIL;
            case telegram -> ContactInformationChannel.TELEGRAM;
            case twitter -> ContactInformationChannel.TWITTER;
            case discord -> ContactInformationChannel.DISCORD;
            case linkedin -> ContactInformationChannel.LINKEDIN;
            case whatsapp -> ContactInformationChannel.WHATSAPP;
        };
    }
}
