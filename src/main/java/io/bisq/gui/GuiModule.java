package io.bisq.gui;

import bisq.desktop.app.BisqAppModule;
import com.google.inject.Singleton;

public class GuiModule extends BisqAppModule {

    private final GuiEnvironment environment;

    public GuiModule(GuiEnvironment environment)
    {
        super(environment);
        this.environment = environment;
    }

    @Override
    protected void configure()
    {
        super.configure();
        bind(GuiEnvironment.class).toInstance(environment);
        bind(Gui.class).in(Singleton.class);
    }
}
