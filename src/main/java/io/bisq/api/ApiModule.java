package io.bisq.api;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import io.bisq.api.service.BisqApiApplication;

public class ApiModule extends AbstractModule {

    private final ApiEnvironment environment;

    public ApiModule(ApiEnvironment environment)
    {
        this.environment = environment;
    }

    @Override
    protected void configure()
    {
        bind(ApiEnvironment.class).toInstance(environment);
        bind(BisqApiApplication.class).in(Singleton.class);
    }
}
