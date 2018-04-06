package io.bisq.gui;

import bisq.desktop.app.BisqApp;
import com.google.inject.Injector;

public class Gui {

    public void run(Injector injector) {
        final GuiEnvironment environment = injector.getInstance(GuiEnvironment.class);
        BisqApp.setEnvironment(environment);
        BisqApp.setInjector(injector);
        javafx.application.Application.launch(BisqApp.class);
    }
}
