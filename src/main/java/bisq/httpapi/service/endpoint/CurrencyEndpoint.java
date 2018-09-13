package bisq.httpapi.service.endpoint;

import bisq.httpapi.facade.PriceFeedFacade;
import bisq.httpapi.model.CurrencyList;
import bisq.httpapi.model.PriceFeed;

import javax.inject.Inject;



import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Api(value = "currencies", authorizations = @Authorization(value = "accessToken"))
@Produces(MediaType.APPLICATION_JSON)
public class CurrencyEndpoint {

    private final PriceFeedFacade priceFeedFacade;

    @Inject
    public CurrencyEndpoint(PriceFeedFacade priceFeedFacade) {
        this.priceFeedFacade = priceFeedFacade;
    }

    @ApiOperation("List available currencies")
    @GET
    public CurrencyList getCurrencyList() {
        return MarketEndpoint.getCurrencyList();
    }

    @ApiOperation(value = "Get market prices", notes = "If currencyCodes is not provided then currencies from preferences are used.")
    @GET
    @Path("/prices")
    public PriceFeed getPriceFeed(@QueryParam("currencyCodes") String currencyCodes) {
        final String[] codes;
        if (null == currencyCodes || 0 == currencyCodes.length())
            codes = new String[0];
        else
            codes = currencyCodes.split("\\s*,\\s*");
        return priceFeedFacade.getPriceFeed(codes);
    }
}
