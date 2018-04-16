package io.bisq.gui;

import bisq.desktop.DesktopModule;
import bisq.desktop.common.view.CachingViewLoader;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class GuiModule extends AbstractModule {

    private final GuiEnvironment environment;

    public GuiModule(GuiEnvironment environment) {
        this.environment = environment;
    }

    @Override
    protected void configure() {
        bind(GuiEnvironment.class).toInstance(environment);
        bind(CachingViewLoader.class).in(Singleton.class);
        // ordering is used for shut down sequence
        install(desktopModule());
    }

    private DesktopModule desktopModule() {
        return new DesktopModule(environment);
    }
}
