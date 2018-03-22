package io.bisq.api.model.payment;

import io.bisq.common.locale.FiatCurrency;
import io.bisq.common.locale.TradeCurrency;
import io.bisq.core.payment.payload.PaymentAccountPayload;

import java.util.List;

public abstract class AbstractPaymentAccountConverter<B extends io.bisq.core.payment.PaymentAccount, BP extends PaymentAccountPayload, R extends PaymentAccount> implements PaymentAccountConverter<B, BP, R> {

    protected void toBusinessModel(B business, R rest) {
        if (null != rest.accountName)
            business.setAccountName(rest.accountName);
        business.getTradeCurrencies().clear();
        if (null != rest.selectedTradeCurrency)
            business.setSelectedTradeCurrency(new FiatCurrency(rest.selectedTradeCurrency));
        if (null != rest.tradeCurrencies) {
            rest.tradeCurrencies.stream().forEach(currencyCode -> business.addCurrency(new FiatCurrency(currencyCode)));
        }
    }

    protected void toRestModel(R rest, B business) {
        rest.id = business.getId();
        rest.accountName = business.getAccountName();
        final TradeCurrency selectedTradeCurrency = business.getSelectedTradeCurrency();
        if (null != selectedTradeCurrency)
            rest.selectedTradeCurrency = selectedTradeCurrency.getCode();
        final List<TradeCurrency> tradeCurrencies = business.getTradeCurrencies();
        if (null != tradeCurrencies)
            tradeCurrencies.stream().forEach(currency -> rest.tradeCurrencies.add(currency.getCode()));
    }

    protected void toRestModel(R rest, BP business) {
        rest.paymentDetails = business.getPaymentDetails();
    }

}
