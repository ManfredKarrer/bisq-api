package bisq.httpapi.service.resources;

import bisq.core.btc.AddressEntryException;
import bisq.core.btc.BalanceModel;
import bisq.core.btc.InsufficientFundsException;

import org.bitcoinj.core.Coin;

import javax.inject.Inject;

import com.google.common.collect.ImmutableList;

import java.util.HashSet;

import lombok.extern.slf4j.Slf4j;



import bisq.httpapi.BisqProxy;
import bisq.httpapi.exceptions.AmountTooLowException;
import bisq.httpapi.model.AuthForm;
import bisq.httpapi.model.Balances;
import bisq.httpapi.model.SeedWords;
import bisq.httpapi.model.SeedWordsRestore;
import bisq.httpapi.model.WalletAddress;
import bisq.httpapi.model.WalletAddressList;
import bisq.httpapi.model.WalletTransactionList;
import bisq.httpapi.model.WithdrawFundsForm;
import io.dropwizard.jersey.validation.ValidationErrorMessage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

//TODO @bernard: i would prefer to rename those resource classes to either WalletResource -> Wallet as resource is in the
// package name already or to something more clear. Resource is so overloaded....

@Api(value = "wallet", authorizations = @Authorization(value = "accessToken"))
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class WalletResource {

    private final BisqProxy bisqProxy;
    private final BalanceModel balanceModel;

    @Inject
    public WalletResource(BisqProxy bisqProxy, BalanceModel balanceModel) {
        this.bisqProxy = bisqProxy;
        this.balanceModel = balanceModel;
    }

    @ApiOperation(value = "Get wallet details")
    @GET
    public Balances getWalletDetails() {
        return new Balances(balanceModel.getAvailableBalance().get().value,
                balanceModel.getReservedBalance().get().value,
                balanceModel.getLockedBalance().get().value);
    }

    @ApiOperation("Get wallet addresses")
    @GET
    @Path("/addresses")
    public WalletAddressList getAddresses(@QueryParam("purpose") BisqProxy.WalletAddressPurpose purpose) {
        return bisqProxy.getWalletAddresses(purpose);
    }

    @ApiOperation("Get or create wallet address")
    @POST
    @Path("/addresses")
    public WalletAddress getOrCreateAvailableUnusedWalletAddresses() {
        return bisqProxy.getOrCreateAvailableUnusedWalletAddresses();
    }

    @ApiOperation("Get wallet seed words")
    @POST
    @Path("/seed-words/retrieve")
    public SeedWords getSeedWords(AuthForm form) {
        final String password = null == form ? null : form.password;
        return bisqProxy.getSeedWords(password);
    }

    @ApiOperation("Restore wallet from seed words")
    @POST
    @Path("/seed-words/restore")
    public void restoreWalletFromSeedWords(@Suspended final AsyncResponse asyncResponse, @Valid @NotNull SeedWordsRestore data) {
        bisqProxy.restoreWalletFromSeedWords(data.mnemonicCode, data.walletCreationDate, data.password)
                .thenApply(response -> asyncResponse.resume(Response.noContent().build()))
                .exceptionally(e -> {
                    final Throwable cause = e.getCause();
                    final Response.ResponseBuilder responseBuilder;

                    final String message = cause.getMessage();
                    responseBuilder = Response.status(500);
                    if (null != message)
                        responseBuilder.entity(new ValidationErrorMessage(ImmutableList.of(message)));
                    log.error("Unable to restore wallet from seed", cause);
                    return asyncResponse.resume(responseBuilder.build());
                });
    }

    @ApiOperation("Get wallet transactions")
    @GET
    @Path("/transactions")
    public WalletTransactionList getTransactions() {
        return bisqProxy.getWalletTransactions();
    }

    @ApiOperation("Withdraw funds")
    @POST
    @Path("/withdraw")
    public void withdrawFunds(@Valid WithdrawFundsForm data) {
        final HashSet<String> sourceAddresses = new HashSet<>(data.sourceAddresses);
        final Coin amountAsCoin = Coin.valueOf(data.amount);
        final boolean feeExcluded = data.feeExcluded;
        final String targetAddress = data.targetAddress;
        try {
            bisqProxy.withdrawFunds(sourceAddresses, amountAsCoin, feeExcluded, targetAddress);
        } catch (AddressEntryException e) {
            throw new ValidationException(e.getMessage());
        } catch (InsufficientFundsException e) {
            throw new WebApplicationException(e.getMessage(), 423);
        } catch (AmountTooLowException e) {
            throw new WebApplicationException(e.getMessage(), 424);
        }
    }
}
