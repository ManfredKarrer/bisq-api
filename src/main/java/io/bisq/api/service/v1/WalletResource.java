package io.bisq.api.service.v1;

import io.bisq.api.AmountTooLowException;
import io.bisq.api.BisqProxy;
import io.bisq.api.BisqProxyResult;
import io.bisq.api.model.WalletAddressList;
import io.bisq.api.model.WalletDetails;
import io.bisq.api.model.WalletTransactionList;
import io.bisq.api.service.ResourceHelper;
import io.bisq.core.btc.AddressEntryException;
import io.bisq.core.btc.InsufficientFundsException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.bitcoinj.core.Coin;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashSet;
import java.util.Optional;


@Api("wallet")
@Produces(MediaType.APPLICATION_JSON)
public class WalletResource {

    private final BisqProxy bisqProxy;

    WalletResource(BisqProxy bisqProxy) {
        this.bisqProxy = bisqProxy;
    }

    @ApiOperation(value = "Get wallet details")
    @GET
    @Path("/")
    public WalletDetails getWalletDetails() {
        BisqProxyResult<WalletDetails> walletDetails = bisqProxy.getWalletDetails();
        if (walletDetails.isInError()) {
            ResourceHelper.handleBisqProxyError(Optional.of(walletDetails));
        }
        return walletDetails.getResult();
    }

    @ApiOperation("Get wallet addresses")
    @GET
    @Path("/addresses")
    public WalletAddressList getAddresses(@QueryParam("purpose") BisqProxy.WalletAddressPurpose purpose) {
        return bisqProxy.getWalletAddresses(purpose);
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

    @Path("btc")
    public BtcWalletResource getBtcWalletResource() {
        return new BtcWalletResource(bisqProxy);
    }


}
