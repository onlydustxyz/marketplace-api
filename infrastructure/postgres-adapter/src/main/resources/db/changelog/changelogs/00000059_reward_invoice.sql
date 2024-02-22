ALTER TABLE rewards
    ADD COLUMN invoice_id UUID REFERENCES accounting.invoices (id);

alter type invoice_status rename value 'PROCESSING' to 'TO_REVIEW';
alter type invoice_status add value 'PAID' after 'APPROVED';

CREATE OR REPLACE FUNCTION public.update_invoice_status()
    RETURNS TRIGGER AS
$$
BEGIN
    UPDATE accounting.invoices
    SET status = 'PAID'
    WHERE id = (select pr.invoice_id from payment_requests pr where pr.id = NEW.request_id)
      AND NOT EXISTS (select 1
                      from payment_requests pr
                               left join payments p on pr.id = p.request_id
                      where pr.invoice_id = accounting.invoices.id
                        and p is null);

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_invoice_status_after_payment_insert_trigger
    AFTER INSERT
    ON payments
    FOR EACH ROW
EXECUTE FUNCTION public.update_invoice_status();
