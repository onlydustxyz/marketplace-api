package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;


public class MeGetContributedProjectsApiIT extends AbstractMarketplaceApiIT {


    @Test
    void should_get_all_my_contributed_projects() {
        // Given
        final String jwt = userAuthHelper.authenticateAnthony().jwt();

        // When
        client.get()
                .uri(getApiURI(ME_GET_CONTRIBUTED_PROJECTS))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.projects[0].slug").isEqualTo("b-conseil")
                .json("""
                        {
                           "projects": [
                             {
                               "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                               "slug": "kaaper",
                               "name": "kaaper",
                               "shortDescription": "Documentation generator for Cairo projects.",
                               "logoUrl": null,
                               "visibility": "PUBLIC"
                             },
                             {
                               "id": "f39b827f-df73-498c-8853-99bc3f562723",
                               "slug": "qa-new-contributions",
                               "name": "QA new contributions",
                               "shortDescription": "QA new contributions",
                               "logoUrl": null,
                               "visibility": "PUBLIC"
                             },
                             {
                               "id": "57f76bd5-c6fb-4ef0-8a0a-74450f4ceca8",
                               "slug": "pizzeria-yoshi-",
                               "name": "Pizzeria Yoshi !",
                               "shortDescription": "Miaaaam une pizza !",
                               "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/14305950553200301786.png",
                               "visibility": "PUBLIC"
                             },
                             {
                               "id": "90fb751a-1137-4815-b3c4-54927a5db059",
                               "slug": "no-sponsors",
                               "name": "No sponsors",
                               "shortDescription": "afsasdas",
                               "logoUrl": null,
                               "visibility": "PUBLIC"
                             },
                             {
                               "id": "00490be6-2c03-4720-993b-aea3e07edd81",
                               "slug": "zama",
                               "name": "Zama",
                               "shortDescription": "A super description for Zama",
                               "logoUrl": "https://dl.airtable.com/.attachments/f776b6ea66adbe46d86adaea58626118/610d50f6/15TqNyRwTMGoVeAX2u1M",
                               "visibility": "PUBLIC"
                             },
                             {
                               "id": "594ca5ca-48f7-49a8-9c26-84b949d4fdd9",
                               "slug": "mooooooonlight",
                               "name": "Mooooooonlight",
                               "shortDescription": "hello la team",
                               "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/1913921207486176664.jpg",
                               "visibility": "PUBLIC"
                             },
                             {
                               "id": "dc60d963-4b5f-4a96-928c-8440b4657138",
                               "slug": "zero-title-4",
                               "name": "Zero title 4",
                               "shortDescription": "Missing short description",
                               "logoUrl": null,
                               "visibility": "PUBLIC"
                             },
                             {
                               "id": "7d04163c-4187-4313-8066-61504d34fc56",
                               "slug": "bretzel",
                               "name": "Bretzel",
                               "shortDescription": "A project for people who love fruits",
                               "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png",
                               "visibility": "PUBLIC"
                             },
                             {
                               "id": "27ca7e18-9e71-468f-8825-c64fe6b79d66",
                               "slug": "b-conseil",
                               "name": "B Conseil",
                               "shortDescription": "Nous sommes B.Conseil, la bonne gestion du Crédit d’Impôt Recherche.",
                               "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11012050846615405488.png",
                               "visibility": "PRIVATE"
                             }
                           ]
                         }
                        """);
    }

    @Test
    void should_get_my_contributed_projects_filtered_by_repo() {
        // Given
        final String jwt = userAuthHelper.authenticateAnthony().jwt();

        // When
        client.get()
                .uri(getApiURI(ME_GET_CONTRIBUTED_PROJECTS, Map.of(
                        "repositories", "493591124")
                ))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                           "projects": [
                             {
                               "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                               "slug": "kaaper",
                               "name": "kaaper",
                               "shortDescription": "Documentation generator for Cairo projects.",
                               "logoUrl": null,
                               "visibility": "PUBLIC"
                             }
                           ]
                         }
                        """)
        ;
    }
}
