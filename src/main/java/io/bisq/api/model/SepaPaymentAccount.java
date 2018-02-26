package io.bisq.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.bisq.core.payment.payload.PaymentMethod;

@JsonTypeName(PaymentMethod.SEPA_ID)
public class SepaPaymentAccount extends PaymentAccount {

    public String accountName;
    public String countryCode;
    public String holderName;
    public String bic;
    public String iban;

    public SepaPaymentAccount() {
        paymentMethod = PaymentMethod.SEPA_ID;
    }
}
