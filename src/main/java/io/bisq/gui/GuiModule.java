package io.bisq.gui;

import bisq.desktop.DesktopModule;
import com.google.inject.AbstractModule;

public class GuiModule extends AbstractModule {

    private final GuiEnvironment environment;

    public GuiModule(GuiEnvironment environment) {
        this.environment = environment;
    }

    @Override
    protected void configure() {
        bind(GuiEnvironment.class).toInstance(environment);
        // ordering is used for shut down sequence
        install(desktopModule());
    }

    private DesktopModule desktopModule() {
        return new DesktopModule(environment);
    }
}
