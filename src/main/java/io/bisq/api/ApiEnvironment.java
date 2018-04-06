package io.bisq.api;

import com.google.inject.AbstractModule;
import joptsimple.OptionSet;

public class ApiEnvironment extends AbstractModule {

    private boolean enabled;

    public ApiEnvironment(OptionSet options) {
        enabled = !options.has("api") || (boolean) options.valueOf("api");
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    protected void configure() {

    }
}
