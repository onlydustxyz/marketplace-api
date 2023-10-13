package onlydust.com.marketplace.api.bootstrap.it;

import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

public class ProjectsApiIT extends AbstractMarketplaceApiIT {

    private static final String BRETZEL_OVERVIEW_JSON = """
            {
              "id": "7d04163c-4187-4313-8066-61504d34fc56",
              "slug": "bretzel",
              "name": "Bretzel",
              "shortDescription": "A project for people who love fruits",
              "longDescription": "[Bretzel](http://bretzel.club/) is your best chance to match with your secret crush      \\nEver liked someone but never dared to tell them?      \\n      \\n**Bretzel** is your chance to match with your secret crush      \\nAll you need is a LinkedIn profile.      \\n      \\n1. **Turn LinkedIn into a bretzel party:** Switch the bretzel mode ON — you'll see bretzels next to everyone. Switch it OFF anytime.      \\n2. **Give your bretzels under the radar:** Give a bretzel to your crush, they will never know about it, unless they give you a bretzel too. Maybe they already have?      \\n3. **Ooh la la, it's a match!**  You just got bretzel’d! See all your matches in a dedicated space, and start chatting!",
              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png",
              "moreInfoUrl": null,
              "hiring": true,
              "visibility": "PUBLIC",
              "contributorCount": 2,
              "topContributors": [
                {
                  "githubUserId": 74653697,
                  "login": "antiyro",
                  "htmlUrl": "https://github.com/antiyro",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/74653697?v=4"
                },
                {
                  "githubUserId": 8642470,
                  "login": "gregcha",
                  "htmlUrl": "https://github.com/gregcha",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4"
                }
              ],
              "repos": [
                {
                  "id": 380954304,
                  "owner": "gregcha",
                  "name": "bretzel-app",
                  "description": "",
                  "stars": 0,
                  "forkCount": 0,
                  "htmlUrl": "https://github.com/gregcha/bretzel-app",
                  "hasIssues": true
                },
                {
                  "id": 466482535,
                  "owner": "gregcha",
                  "name": "bretzel-ressources",
                  "description": "",
                  "stars": 0,
                  "forkCount": 0,
                  "htmlUrl": "https://github.com/gregcha/bretzel-ressources",
                  "hasIssues": true
                },
                {
                  "id": 659718526,
                  "owner": "KasarLabs",
                  "name": "deoxys-telemetry",
                  "description": "Deoxys Telemetry service",
                  "stars": 0,
                  "forkCount": 1,
                  "htmlUrl": "https://github.com/KasarLabs/deoxys-telemetry",
                  "hasIssues": false
                },
                {
                  "id": 452047076,
                  "owner": "gregcha",
                  "name": "bretzel-site",
                  "description": "",
                  "stars": 0,
                  "forkCount": 0,
                  "htmlUrl": "https://github.com/gregcha/bretzel-site",
                  "hasIssues": true
                }
              ],
              "leaders": [
                {
                  "githubUserId": 8642470,
                  "login": "gregcha",
                  "htmlUrl": "https://github.com/gregcha",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                  "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                },
                {
                  "githubUserId": 98735421,
                  "login": "pacovilletard",
                  "htmlUrl": "https://github.com/pacovilletard",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/98735421?v=4",
                  "id": "f20e6812-8de8-432b-9c31-2920434fe7d0"
                }
              ],
              "sponsors": [
                {
                  "id": "c8dfb479-ee9d-4c16-b4b3-0ba39c2fdd6f",
                  "name": "OGC Nissa Ineos",
                  "url": null,
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2946389705306833508.png"
                },
                {
                  "id": "0980c5ab-befc-4314-acab-777fbf970cbb",
                  "name": "Coca Cola",
                  "url": null,
                  "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj"
                }
              ],
              "technologies": {
                "TypeScript": 189275,
                "Dockerfile": 1982,
                "CSS": 422216,
                "Shell": 732,
                "Rust": 407023,
                "SCSS": 98360,
                "JavaScript": 62717,
                "HTML": 121906
              }
            }
            """;
    @Autowired
    ProjectRepository projectRepository;

    @Test
    void should_get_projects() {
        client.get()
                .uri(getApiURI(PROJECTS_GET))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();
    }

    @Test
    public void should_get_a_project_by_slug() {
        // Given
        final String slug = "bretzel";

        // When
        client.get()
                .uri(getApiURI(PROJECTS_GET_BY_SLUG + "/" + slug))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(BRETZEL_OVERVIEW_JSON);
    }

    @Test
    public void should_get_a_project_by_id() {
        // Given
        final String id = "7d04163c-4187-4313-8066-61504d34fc56";

        // When
        client.get()
                .uri(getApiURI(PROJECTS_GET_BY_ID + "/" + id))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(BRETZEL_OVERVIEW_JSON);
    }

    //@Test
    public void should_create_a_new_project() {
        client.post()
                .uri(getApiURI(PROJECTS_POST))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Super Project",
                          "shortDescription": "This is a super project",
                          "longDescription": "This is a super awesome project with a nice description",
                          "moreInfo": [
                            {
                              "url": "https://t.me/foobar",
                              "value": "foobar"
                            }
                          ],
                          "isLookingForContributors": true,
                          "inviteGithubUserIdsAsProjectLeads": [
                            595505, 43467246
                          ],
                          "githubRepoIds": [
                            498695724, 698096830
                          ],
                          "image": "string"
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();
    }
}
