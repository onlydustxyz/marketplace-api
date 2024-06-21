package onlydust.com.marketplace.api.it.api;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;


public class ActivityApiIT extends AbstractMarketplaceApiIT {


    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @AfterAll
    static void tearDown() throws IOException, InterruptedException {
        restoreIndexerDump();
    }

    @Test
    void should_return_server_starting_date() {
        // Given
        final EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("""
                        UPDATE indexer_exp.contributions c
                        SET completed_at = now() - CAST(subquery.row_number || ' minutes' as interval)
                        FROM (
                            SELECT public_contributions.id, ROW_NUMBER() OVER(ORDER BY completed_at DESC NULLS LAST, pull_request_id DESC NULLS LAST, github_user_id ASC NULLS LAST) as row_number
                            FROM public_contributions
                                JOIN iam.users u on u.github_user_id = github_author_id
                            WHERE type = 'PULL_REQUEST'
                            ORDER BY completed_at DESC NULLS LAST, pull_request_id DESC NULLS LAST, github_user_id ASC NULLS LAST
                            LIMIT 2000) as subquery
                        WHERE c.id = subquery.id
                        """)
                .executeUpdate();
        em.createNativeQuery("""
                        UPDATE rewards r
                        SET requested_at = now() - CAST(subquery.row_number || ' minutes' as interval) - interval '5 second'
                        FROM (
                            SELECT id, ROW_NUMBER() OVER(ORDER BY requested_at DESC NULLS LAST) as row_number
                            FROM rewards
                            ORDER BY requested_at DESC NULLS LAST
                            LIMIT 2000) as subquery
                        WHERE r.id = subquery.id
                        """)
                .executeUpdate();
        em.createNativeQuery("""
                        UPDATE accounting.reward_status_data rsd
                        SET invoice_received_at = now() - CAST(subquery.row_number || ' minutes' as interval) - interval '10 second'
                        FROM (
                            SELECT reward_id, ROW_NUMBER() OVER(ORDER BY invoice_received_at DESC NULLS LAST) as row_number
                            FROM accounting.reward_status_data
                            ORDER BY invoice_received_at DESC NULLS LAST
                            LIMIT 2000) as subquery
                        WHERE rsd.reward_id = subquery.reward_id
                        """)
                .executeUpdate();
        em.createNativeQuery("""
                        UPDATE projects p
                        SET created_at = now() - CAST(subquery.row_number || ' minutes' as interval) - interval '15 second'
                        FROM (
                            SELECT id, ROW_NUMBER() OVER(ORDER BY created_at DESC NULLS LAST) as row_number
                            FROM projects
                            ORDER BY created_at DESC NULLS LAST
                            LIMIT 2000) as subquery
                        WHERE p.id = subquery.id
                        """)
                .executeUpdate();
        em.flush();
        em.getTransaction().commit();
        em.close();

        // When
        client.get()
                .uri(getApiURI("/api/v1/public-activity", "pageSize", "5"))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.activities[?(@.timestamp.length() == 0)]").doesNotExist()
                .json("""
                        {
                          "totalPageNumber": 18,
                          "totalItemNumber": 90,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "activities": [
                            {
                              "type": "PULL_REQUEST",
                              "pullRequest": {
                                "project": {
                                  "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                                  "slug": "kaaper",
                                  "name": "kaaper",
                                  "logoUrl": null
                                },
                                "author": {
                                  "githubUserId": 5160414,
                                  "login": "haydencleary",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4"
                                }
                              },
                              "rewardCreated": null,
                              "rewardClaimed": null,
                              "projectCreated": null
                            },
                            {
                              "type": "REWARD_CREATED",
                              "pullRequest": null,
                              "rewardCreated": {
                                "project": {
                                  "id": "7d04163c-4187-4313-8066-61504d34fc56",
                                  "slug": "bretzel",
                                  "name": "Bretzel",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                                },
                                "recipient": {
                                  "githubUserId": 8642470,
                                  "login": "gregcha",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp"
                                },
                                "amount": {
                                  "amount": 1000.00,
                                  "currency": {
                                    "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                                    "code": "USD",
                                    "name": "US Dollar",
                                    "logoUrl": null,
                                    "decimals": 2
                                  }
                                }
                              },
                              "rewardClaimed": null,
                              "projectCreated": null
                            },
                            {
                              "type": "REWARD_CLAIMED",
                              "pullRequest": null,
                              "rewardCreated": null,
                              "rewardClaimed": {
                                "project": {
                                  "id": "a0c91aee-9770-4000-a893-953ddcbd62a7",
                                  "slug": "aldbaran-du-taureau",
                                  "name": "Aldébaran du Taureau",
                                  "logoUrl": "https://www.puregamemedia.fr/media/images/uploads/2019/11/ban_saint_seiya_awakening_kotz_aldebaran_taureau.jpg/?w=790&h=inherit&fm=webp&fit=contain&s=ab78704b124d2de9525a8af91ef7c4ed"
                                },
                                "recipient": {
                                  "githubUserId": 43467246,
                                  "login": "AnthonyBuisset",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp"
                                },
                                "amount": {
                                  "amount": 1000,
                                  "currency": {
                                    "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                    "code": "USDC",
                                    "name": "USD Coin",
                                    "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                    "decimals": 6
                                  }
                                }
                              },
                              "projectCreated": null
                            },
                            {
                              "type": "PROJECT_CREATED",
                              "pullRequest": null,
                              "rewardCreated": null,
                              "rewardClaimed": null,
                              "projectCreated": {
                                "project": {
                                  "id": "2a95f786-beb2-461d-b573-7150e4a1b65b",
                                  "slug": "poor-project",
                                  "name": "Poor Project",
                                  "logoUrl": null
                                },
                                "createdBy": {
                                  "githubUserId": 31901905,
                                  "login": "kaelsky",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4"
                                }
                              }
                            },
                            {
                              "type": "PROJECT_CREATED",
                              "pullRequest": null,
                              "rewardCreated": null,
                              "rewardClaimed": null,
                              "projectCreated": {
                                "project": {
                                  "id": "1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e",
                                  "slug": "calcom",
                                  "name": "Cal.com",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5271998260751715005.png"
                                },
                                "createdBy": {
                                  "githubUserId": 117665867,
                                  "login": "gilbertVDB17",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/117665867?v=4"
                                }
                              }
                            }
                          ]
                        }
                        """);
    }
}
