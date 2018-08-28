package bisq.httpapi.service.resources;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;



import bisq.httpapi.BisqProxy;
import bisq.httpapi.model.ClosedTradableList;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Slf4j
@Api(value = "closed-tradables", authorizations = @Authorization(value = "accessToken"))
@Produces(MediaType.APPLICATION_JSON)
public class ClosedTradableResource {

    private final BisqProxy bisqProxy;

    @Inject
    public ClosedTradableResource(BisqProxy bisqProxy) {
        this.bisqProxy = bisqProxy;
    }

    @ApiOperation("List portfolio history")
    @GET
    public ClosedTradableList listClosedTrades() {
        final ClosedTradableList list = new ClosedTradableList();
        list.closedTradables = bisqProxy.getClosedTradableList();
        list.total = list.closedTradables.size();
        return list;
    }

}
