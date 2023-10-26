package onlydust.com.marketplace.api.domain.view;

import onlydust.com.marketplace.api.domain.model.UserPayoutInformation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserPayoutInformationTest {

    @Test
    void should_validate_contact_info() {
        assertEquals(true, UserPayoutInformation.builder()
                .hasValidCompany(false)
                .hasValidLocation(true)
                .hasValidPerson(true)
                .build()
                .getHasValidContactInfo()
        );
        assertEquals(false, UserPayoutInformation.builder()
                .hasValidCompany(true)
                .hasValidLocation(false)
                .hasValidPerson(true)
                .build()
                .getHasValidContactInfo()
        );
        assertEquals(true, UserPayoutInformation.builder()
                .hasValidCompany(true)
                .hasValidLocation(true)
                .hasValidPerson(false)
                .build()
                .getHasValidContactInfo()
        );
        assertEquals(true, UserPayoutInformation.builder()
                .hasValidCompany(true)
                .hasValidLocation(true)
                .hasValidPerson(true)
                .build()
                .getHasValidContactInfo()
        );
        assertEquals(false, UserPayoutInformation.builder()
                .hasValidCompany(false)
                .hasValidLocation(false)
                .hasValidPerson(false)
                .build()
                .getHasValidContactInfo()
        );
    }
}
