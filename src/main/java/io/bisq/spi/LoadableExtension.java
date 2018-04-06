package io.bisq.spi;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public interface LoadableExtension {

    void decorateOptionParser(OptionParser parser);

    AbstractModule configure(OptionSet options);

    void start(Injector injector);
}
