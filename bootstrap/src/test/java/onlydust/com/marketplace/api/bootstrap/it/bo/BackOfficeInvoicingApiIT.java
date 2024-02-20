package onlydust.com.marketplace.api.bootstrap.it.bo;

import com.github.javafaker.Faker;
import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
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

import java.math.BigDecimal;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BackOfficeInvoicingApiIT extends AbstractMarketplaceBackOfficeApiIT {
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private InvoiceRewardRepository invoiceRewardRepository;
    @Autowired
    private PaymentRequestRepository paymentRequestRepository;
    private final Faker faker = new Faker();
    private List<InvoiceEntity> invoices;

    @BeforeEach
    void setUp() {
        invoices = List.of(
                fakeInvoice(List.of(UUID.fromString("061e2c7e-bda4-49a8-9914-2e76926f70c2"))),
                fakeInvoice(List.of(UUID.fromString("ee28315c-7a84-4052-9308-c2236eeafda1"), UUID.fromString("d067b24d-115a-45e9-92de-94dd1d01b184"))),
                fakeInvoice(List.of(UUID.fromString("d506a05d-3739-452f-928d-45ea81d33079"), UUID.fromString("5083ac1f-4325-4d47-9760-cbc9ab82f25c"),
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
                .uri(getApiURI(INVOICES, Map.of("pageIndex", "0", "pageSize", "10")))
                .header("Api-Key", apiKey())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.invoices[?(@.id empty true)]").isEmpty()
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
                              "status": "PROCESSING",
                              "downloadLink": "https://s3.storage.com/invoice.pdf",
                              "amount": 1010.00,
                              "currencyId": "f35155b5-6107-4677-85ac-23f8c2a63193",
                              "rewardIds": [
                                "061e2c7e-bda4-49a8-9914-2e76926f70c2"
                              ]
                            },
                            {
                              "status": "PROCESSING",
                              "downloadLink": "https://s3.storage.com/invoice.pdf",
                              "amount": 2777.50,
                              "currencyId": "f35155b5-6107-4677-85ac-23f8c2a63193",
                              "rewardIds": [
                                "d067b24d-115a-45e9-92de-94dd1d01b184",
                                "ee28315c-7a84-4052-9308-c2236eeafda1"
                              ]
                            },
                            {
                              "status": "PROCESSING",
                              "downloadLink": "https://s3.storage.com/invoice.pdf",
                              "amount": 4765.00,
                              "currencyId": "f35155b5-6107-4677-85ac-23f8c2a63193",
                              "rewardIds": [
                                "d506a05d-3739-452f-928d-45ea81d33079",
                                "5083ac1f-4325-4d47-9760-cbc9ab82f25c",
                                "e6ee79ae-b3f0-4f4e-b7e3-9e643bc27236"
                              ]
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
                          "status": "COMPLETE"
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
                              "status": "COMPLETE",
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
                              "rewardIds": [
                                "061e2c7e-bda4-49a8-9914-2e76926f70c2"
                              ]
                            }
                          ]
                        }
                        """)
        ;
    }

    @SneakyThrows
    @Transactional
    InvoiceEntity fakeInvoice(List<UUID> rewardIds) {
        final var firstName = faker.name().firstName();
        final var lastName = faker.name().lastName();

        final var rewards = invoiceRewardRepository.findAll(rewardIds);

        return new InvoiceEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Invoice.Number.of(12, lastName, firstName).toString(),
                ZonedDateTime.now().minusDays(1),
                InvoiceEntity.Status.PROCESSING,
                rewards.stream().map(InvoiceRewardEntity::baseAmount).reduce(BigDecimal.ZERO, BigDecimal::add),
                rewards.get(0).baseCurrency(),
                new URL("https://s3.storage.com/invoice.pdf"),
                new InvoiceEntity.Data(
                        ZonedDateTime.now().plusDays(9),
                        BigDecimal.ZERO,
                        new Invoice.PersonalInfo(
                                firstName,
                                lastName,
                                faker.address().fullAddress()
                        ),
                        null,
                        null,
                        List.of(new Invoice.Wallet("ETHEREUM", "vitalik.eth")),
                        rewards
                )
        );
    }
}
