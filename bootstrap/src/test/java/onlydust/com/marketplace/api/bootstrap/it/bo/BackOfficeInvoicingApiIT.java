package onlydust.com.marketplace.api.bootstrap.it.bo;

import com.github.javafaker.Faker;
import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.port.out.PdfStoragePort;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.InvoiceEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.InvoiceRewardEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.InvoiceRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.InvoiceRewardRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.PaymentRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class BackOfficeInvoicingApiIT extends AbstractMarketplaceBackOfficeApiIT {
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private InvoiceRewardRepository invoiceRewardRepository;
    @Autowired
    private PaymentRequestRepository paymentRequestRepository;
    @Autowired
    PdfStoragePort pdfStoragePort;
    private final Faker faker = new Faker();
    private List<InvoiceEntity> invoices;

    @BeforeEach
    void setUp() {
        invoices = List.of(
                fakeInvoice(UUID.fromString("16ca82f4-4671-4036-8666-ce0930824558"), List.of(UUID.fromString("061e2c7e-bda4-49a8-9914-2e76926f70c2"))),
                fakeInvoice(UUID.fromString("51d37fff-ed4c-474a-b846-af18edda6b8a"), List.of(UUID.fromString("ee28315c-7a84-4052-9308-c2236eeafda1"),
                        UUID.fromString("d067b24d-115a-45e9-92de-94dd1d01b184"))),
                fakeInvoice(UUID.fromString("3fcd930b-b84d-4ce5-82cd-eca91b8a7553"), List.of(UUID.fromString("d506a05d-3739-452f-928d-45ea81d33079"),
                        UUID.fromString("5083ac1f-4325-4d47-9760-cbc9ab82f25c"),
                        UUID.fromString("e6ee79ae-b3f0-4f4e-b7e3-9e643bc27236")))
        );

        invoices.forEach(invoice -> {
            final var rewards = paymentRequestRepository.findAllById(invoice.data().rewards().stream().map(InvoiceRewardEntity::id).toList());
            rewards.forEach(reward -> reward.setInvoice(null));
            paymentRequestRepository.saveAll(rewards);
        });

        invoiceRepository.deleteAll();
        invoiceRepository.saveAll(invoices);

        invoices.forEach(invoice -> {
            final var rewards = paymentRequestRepository.findAllById(invoice.data().rewards().stream().map(InvoiceRewardEntity::id).toList());
            rewards.forEach(reward -> reward.setInvoice(invoice));
            paymentRequestRepository.saveAll(rewards);
        });
    }

    @Test
    void should_list_invoices() {
        client
                .get()
                .uri(getApiURI(INVOICES, Map.of(
                        "pageIndex", "0",
                        "pageSize", "10",
                        "invoiceIds", invoices.stream().map(InvoiceEntity::id).map(UUID::toString).collect(Collectors.joining(",")))))
                .header("Api-Key", apiKey())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.invoices[?(@.createdAt empty true)]").isEmpty()
                .jsonPath("$.invoices[?(@.dueAt empty true)]").isEmpty()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 3,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "invoices": [
                            {
                              "id": "16ca82f4-4671-4036-8666-ce0930824558",
                              "status": "PROCESSING",
                              "internalStatus": "TO_REVIEW",
                              "amount": 1010.00,
                              "currencyId": "f35155b5-6107-4677-85ac-23f8c2a63193",
                              "rewardIds": [
                                "061e2c7e-bda4-49a8-9914-2e76926f70c2"
                              ],
                              "downloadUrl": "https://local-bo-api.onlydust.com/bo/v1/external/invoices/16ca82f4-4671-4036-8666-ce0930824558?token=BO_TOKEN"
                            },
                            {
                              "id": "51d37fff-ed4c-474a-b846-af18edda6b8a",
                              "status": "PROCESSING",
                              "internalStatus": "TO_REVIEW",
                              "amount": 2777.50,
                              "currencyId": "f35155b5-6107-4677-85ac-23f8c2a63193",
                              "rewardIds": [
                                "d067b24d-115a-45e9-92de-94dd1d01b184",
                                "ee28315c-7a84-4052-9308-c2236eeafda1"
                              ],
                              "downloadUrl": "https://local-bo-api.onlydust.com/bo/v1/external/invoices/51d37fff-ed4c-474a-b846-af18edda6b8a?token=BO_TOKEN"
                            },
                            {
                              "id": "3fcd930b-b84d-4ce5-82cd-eca91b8a7553",
                              "status": "PROCESSING",
                              "internalStatus": "TO_REVIEW",
                              "amount": 4765.00,
                              "currencyId": "f35155b5-6107-4677-85ac-23f8c2a63193",
                              "rewardIds": [
                                "d506a05d-3739-452f-928d-45ea81d33079",
                                "5083ac1f-4325-4d47-9760-cbc9ab82f25c",
                                "e6ee79ae-b3f0-4f4e-b7e3-9e643bc27236"
                              ],
                              "downloadUrl": "https://local-bo-api.onlydust.com/bo/v1/external/invoices/3fcd930b-b84d-4ce5-82cd-eca91b8a7553?token=BO_TOKEN"
                            }
                          ]
                        }
                        """)
        ;
    }

    @Test
    void should_approve_invoices() {
        client
                .patch()
                .uri(getApiURI(INVOICE.formatted(invoices.get(1).id())))
                .header("Api-Key", apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "status": "APPROVED"
                        }
                        """)
                .exchange()
                .expectStatus()
                .isNoContent()
        ;

        client
                .get()
                .uri(getApiURI(INVOICES, Map.of("pageIndex", "0", "pageSize", "10")))
                .header("Api-Key", apiKey())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "invoices": [
                            {
                              "status": "PROCESSING",
                              "internalStatus": "APPROVED",
                              "rewardIds": [
                                "d067b24d-115a-45e9-92de-94dd1d01b184",
                                "ee28315c-7a84-4052-9308-c2236eeafda1"
                              ]
                            },
                            {
                              "status": "PROCESSING",
                              "internalStatus": "TO_REVIEW",
                              "rewardIds": [
                                "d506a05d-3739-452f-928d-45ea81d33079",
                                "5083ac1f-4325-4d47-9760-cbc9ab82f25c",
                                "e6ee79ae-b3f0-4f4e-b7e3-9e643bc27236"
                              ]
                            },
                            {
                              "status": "PROCESSING",
                              "internalStatus": "TO_REVIEW",
                              "rewardIds": [
                                "061e2c7e-bda4-49a8-9914-2e76926f70c2"
                              ]
                            }
                          ]
                        }
                        """)
        ;
    }

    @Test
    void should_reject_invoices() {
        client
                .patch()
                .uri(getApiURI(INVOICE.formatted(invoices.get(1).id())))
                .header("Api-Key", apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "status": "REJECTED"
                        }
                        """)
                .exchange()
                .expectStatus()
                .isNoContent()
        ;

        client
                .get()
                .uri(getApiURI(INVOICES, Map.of("pageIndex", "0", "pageSize", "10")))
                .header("Api-Key", apiKey())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "invoices": [
                            {
                              "status": "REJECTED",
                              "rewardIds": [
                                "d067b24d-115a-45e9-92de-94dd1d01b184",
                                "ee28315c-7a84-4052-9308-c2236eeafda1"
                              ]
                            },
                            {
                              "status": "PROCESSING",
                              "rewardIds": [
                                "d506a05d-3739-452f-928d-45ea81d33079",
                                "5083ac1f-4325-4d47-9760-cbc9ab82f25c",
                                "e6ee79ae-b3f0-4f4e-b7e3-9e643bc27236"
                              ]
                            },
                            {
                              "status": "PROCESSING",
                              "internalStatus": "TO_REVIEW",
                              "rewardIds": [
                                "061e2c7e-bda4-49a8-9914-2e76926f70c2"
                              ]
                            }
                          ]
                        }
                        """)
        ;
    }


    @Test
    void should_filter_invoices_by_status() {
        client
                .patch()
                .uri(getApiURI(INVOICE.formatted(invoices.get(0).id())))
                .header("Api-Key", apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "status": "REJECTED"
                        }
                        """)
                .exchange()
                .expectStatus()
                .isNoContent()
        ;

        client
                .get()
                .uri(getApiURI(INVOICES, Map.of("pageIndex", "0", "pageSize", "10", "internalStatuses", "TO_REVIEW")))
                .header("Api-Key", apiKey())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "invoices": [
                            {
                              "internalStatus": "TO_REVIEW"
                            },
                            {
                              "internalStatus": "TO_REVIEW"
                            }
                          ]
                        }
                        """)
        ;
    }

    @Test
    void should_download_invoices() {
        final var invoiceId = invoices.get(1).id();
        final var pdfData = faker.lorem().paragraph().getBytes();
        when(pdfStoragePort.download(eq(invoiceId + ".pdf"))).then(invocation -> new ByteArrayInputStream(pdfData));

        final var data = client.get()
                .uri(getApiURI(EXTERNAL_INVOICE.formatted(invoiceId), Map.of("token", "BO_TOKEN")))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody().returnResult().getResponseBody();

        assertThat(data).isEqualTo(pdfData);
    }

    @Test
    void should_reject_invoice_download_if_wrong_token() {
        final var invoiceId = invoices.get(1).id();

        client.get()
                .uri(getApiURI(EXTERNAL_INVOICE.formatted(invoiceId), Map.of("token", "INVALID_TOKEN")))
                .exchange()
                // Then
                .expectStatus()
                .isUnauthorized();
    }

    @SneakyThrows
    @Transactional
    InvoiceEntity fakeInvoice(UUID id, List<UUID> rewardIds) {
        final var firstName = faker.name().firstName();
        final var lastName = faker.name().lastName();

        final var rewards = invoiceRewardRepository.findAll(rewardIds);

        return new InvoiceEntity(
                id,
                UUID.randomUUID(),
                Invoice.Number.of(12, lastName, firstName).toString(),
                ZonedDateTime.now().minusDays(1),
                InvoiceEntity.Status.TO_REVIEW,
                rewards.stream().map(InvoiceRewardEntity::targetAmount).reduce(BigDecimal.ZERO, BigDecimal::add),
                rewards.get(0).targetCurrency(),
                new URL("https://s3.storage.com/invoice.pdf"),
                null,
                new InvoiceEntity.Data(
                        ZonedDateTime.now().plusDays(9),
                        BigDecimal.ZERO,
                        new Invoice.PersonalInfo(
                                firstName,
                                lastName,
                                faker.address().fullAddress(),
                                faker.address().countryCode()
                        ),
                        null,
                        null,
                        List.of(new Invoice.Wallet("ETHEREUM", "vitalik.eth")),
                        rewards
                )
        );
    }
}
