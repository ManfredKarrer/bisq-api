package network.bisq.api.model.payment;

import com.fasterxml.jackson.annotation.JsonTypeName;
import bisq.core.payment.payload.PaymentMethod;
import org.hibernate.validator.constraints.NotBlank;

@JsonTypeName(PaymentMethod.US_POSTAL_MONEY_ORDER_ID)
public class USPostalMoneyOrderPaymentAccount extends PaymentAccount {

    @NotBlank
    public String holderName;

    @NotBlank
    public String postalAddress;

    public USPostalMoneyOrderPaymentAccount() {
        super(PaymentMethod.US_POSTAL_MONEY_ORDER_ID);
    }
}
