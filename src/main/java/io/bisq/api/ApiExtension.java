package io.bisq.api;

import bisq.spi.LoadableExtension;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import io.bisq.api.service.BisqApiApplication;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.util.concurrent.CompletableFuture;

public class ApiExtension implements LoadableExtension {
    @Override
    public void decorateOptionParser(OptionParser parser) {
        parser.accepts("api", "Enable API").withOptionalArg().ofType(boolean.class).defaultsTo(true);
    }

    @Override
    public AbstractModule configure(OptionSet options) {
        final ApiEnvironment environment = new ApiEnvironment(options);
        return new ApiModule(environment);
    }

    @Override
    public CompletableFuture<Void> preStart(Injector injector) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> setup(Injector injector) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void start(Injector injector) {
        final ApiEnvironment environment = injector.getInstance(ApiEnvironment.class);
        if (!environment.isEnabled()) {
            System.out.printf("ApiExtension is disabled\n");
            return;
        }
        try {
            injector.getInstance(BisqApiApplication.class).run("server", "bisq-api.yml");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
