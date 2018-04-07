package io.bisq.core;

import bisq.desktop.app.BisqAppModule;
import com.google.inject.Singleton;
import io.bisq.gui.Gui;

public class CoreModule extends BisqAppModule {

    public CoreModule(CoreEnvironment environment) {
        super(environment);
    }

    @Override
    protected void configure() {
        super.configure();
        bind(CoreEnvironment.class).toInstance((CoreEnvironment) environment);
        bind(Core.class).in(Singleton.class);
    }
}
