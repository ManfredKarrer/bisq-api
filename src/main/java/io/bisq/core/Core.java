package io.bisq.core;

import bisq.common.app.Version;
import bisq.common.proto.persistable.PersistedDataHost;
import bisq.common.util.Utilities;
import bisq.core.app.BisqEnvironment;
import bisq.core.arbitration.DisputeManager;
import bisq.core.btc.AddressEntryList;
import bisq.core.btc.BaseCurrencyNetwork;
import bisq.core.locale.CurrencyUtil;
import bisq.core.locale.Res;
import bisq.core.offer.OpenOfferManager;
import bisq.core.provider.price.PriceFeedService;
import bisq.core.trade.TradeManager;
import bisq.core.trade.closed.ClosedTradableManager;
import bisq.core.trade.failed.FailedTradesManager;
import bisq.core.user.Preferences;
import bisq.core.user.User;
import bisq.desktop.Navigation;
import bisq.network.p2p.P2PService;
import com.google.inject.Injector;
import io.bisq.api.app.MainViewModelHeadless;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Security;
import java.util.ArrayList;
import java.util.Date;

public class Core {

    private static final Logger log = LoggerFactory.getLogger(Core.class);

    public void run(Injector injector) {
        System.out.println("Starting core");
        Utilities.printSysInfo();
        Security.addProvider(new BouncyCastleProvider());

        final BaseCurrencyNetwork baseCurrencyNetwork = BisqEnvironment.getBaseCurrencyNetwork();
        final String currencyCode = baseCurrencyNetwork.getCurrencyCode();
        Res.setBaseCurrencyCode(currencyCode);
        Res.setBaseCurrencyName(baseCurrencyNetwork.getCurrencyName());
        CurrencyUtil.setBaseCurrencyCode(currencyCode);

        MainViewModelHeadless mainViewModelHeadless = injector.getInstance(MainViewModelHeadless.class);

        //appSetup.start(); // TODO refactor MainViewModel into AppSetupWithP2P and use it for API + GUI
        mainViewModelHeadless.start();

        // All classes which are persisting objects need to be added here
        // Maintain order!
        ArrayList<PersistedDataHost> persistedDataHosts = new ArrayList<>();
        persistedDataHosts.add(injector.getInstance(Preferences.class));
        persistedDataHosts.add(injector.getInstance(User.class));
        persistedDataHosts.add(injector.getInstance(Navigation.class));
        persistedDataHosts.add(injector.getInstance(AddressEntryList.class));
        persistedDataHosts.add(injector.getInstance(TradeManager.class));
        persistedDataHosts.add(injector.getInstance(OpenOfferManager.class));
        persistedDataHosts.add(injector.getInstance(TradeManager.class));
        persistedDataHosts.add(injector.getInstance(ClosedTradableManager.class));
        persistedDataHosts.add(injector.getInstance(FailedTradesManager.class));
        persistedDataHosts.add(injector.getInstance(DisputeManager.class));
        persistedDataHosts.add(injector.getInstance(P2PService.class));
        //            persistedDataHosts.add(injector.getInstance(VotingManager.class));
        //            persistedDataHosts.add(injector.getInstance(CompensationRequestManager.class));

        // we apply at startup the reading of persisted data but don't want to get it triggered in the constructor
        persistedDataHosts.stream().forEach(e -> {
            try {
                log.debug("call readPersisted at " + e.getClass().getSimpleName());
                e.readPersisted();
            } catch (Throwable e1) {
                log.error("readPersisted error", e1);
            }
        });

        ObjectProperty<Throwable> walletServiceException = new SimpleObjectProperty<>();

                   /*
                   // copy encryption handling from MainViewModel - initWalletService()
                   walletsSetup.initialize(null,
                           () -> {
                               if (walletsManager.areWalletsEncrypted()) {
                                   log.error("Encrypted wallets are not yet supported in the headless api.");
                               } else {
                                   log.info("walletsSetup completed");
                               }
                           },
                           throwable -> log.error(throwable.toString()));
                           */
        long ts = new Date().getTime();
        boolean logged[] = {false};
        PriceFeedService priceFeedService = injector.getInstance(PriceFeedService.class);
        priceFeedService.setCurrencyCodeOnInit();
        priceFeedService.requestPriceFeed(price -> {
                    if (!logged[0]) {
                        log.info("We received data from the price relay after {} ms.",
                                (new Date().getTime() - ts));
                        logged[0] = true;
                    }
                },
                (errorMessage, throwable) -> log.error("requestPriceFeed failed:" + errorMessage));

        Version.setBaseCryptoNetworkId(BisqEnvironment.getBaseCurrencyNetwork().ordinal());
        Version.printVersion();
    }
//    TODO add shutdown support
}
