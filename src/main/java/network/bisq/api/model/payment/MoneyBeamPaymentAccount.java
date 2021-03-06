package network.bisq.api.model.payment;

import com.fasterxml.jackson.annotation.JsonTypeName;
import bisq.core.payment.payload.PaymentMethod;
import org.hibernate.validator.constraints.NotBlank;

@JsonTypeName(PaymentMethod.MONEY_BEAM_ID)
public class MoneyBeamPaymentAccount extends PaymentAccount {

    @NotBlank
    public String accountId;

    public MoneyBeamPaymentAccount() {
        super(PaymentMethod.MONEY_BEAM_ID);
    }
}
