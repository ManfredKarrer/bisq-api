package io.bisq.gui;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import io.bisq.spi.LoadableExtension;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class GuiExtension implements LoadableExtension {
    @Override
    public void decorateOptionParser(OptionParser parser) {
//        TODO allowsUnrecognizedOptions should not be invoked
        parser.allowsUnrecognizedOptions();
        parser.accepts("gui", "Enable GUI").withOptionalArg().ofType(boolean.class).defaultsTo(true);
    }

    @Override
    public AbstractModule configure(OptionSet options) {
        final GuiEnvironment environment = new GuiEnvironment(options);
        return new GuiModule(environment);
    }

    @Override
    public void start(Injector injector) {
        final GuiEnvironment environment = injector.getInstance(GuiEnvironment.class);
        if (!environment.isEnabled()) {
            System.out.printf("GuiExtension is disabled\n");
            return;
        }
        new Thread() {
            @Override
            public void run() {
                    final Gui gui = injector.getInstance(Gui.class);
                    gui.run(injector);
            }
        }.start();
    }
}
