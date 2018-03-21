package io.bisq.api.model.payment;

import io.bisq.core.payment.PopmoneyAccount;
import io.bisq.core.payment.payload.PopmoneyAccountPayload;

public class PopmoneyPaymentAccountConverter extends AbstractPaymentAccountConverter<PopmoneyAccount, PopmoneyPaymentAccount> {

    @Override
    public PopmoneyAccount toBusinessModel(PopmoneyPaymentAccount rest) {
        final PopmoneyAccount business = new PopmoneyAccount();
        business.init();
        business.setAccountId(rest.accountId);
        business.setHolderName(rest.holderName);
        toBusinessModel(business, rest);
        return business;
    }

    @Override
    public PopmoneyPaymentAccount toRestModel(PopmoneyAccount business) {
        final PopmoneyPaymentAccount rest = new PopmoneyPaymentAccount();
        final PopmoneyAccountPayload payload = (PopmoneyAccountPayload) business.getPaymentAccountPayload();
        rest.accountId = payload.getAccountId();
        rest.holderName = payload.getHolderName();
        toRestModel(rest, business);
        return rest;
    }

}
