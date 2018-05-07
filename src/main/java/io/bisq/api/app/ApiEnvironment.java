package io.bisq.api.app;

import bisq.core.app.BisqEnvironment;
import joptsimple.OptionSet;

public class ApiEnvironment extends BisqEnvironment {

    private final boolean apiEnabled;

    private final String apiHost;

    private final Integer apiPort;

    public ApiEnvironment(OptionSet options) {
        super(options);
        apiHost = (String) options.valueOf(ApiOptionKeys.OPTION_API_HOST);
        apiPort = (Integer) options.valueOf(ApiOptionKeys.OPTION_API_PORT);
        final Boolean apiEnabled = (Boolean) options.valueOf(ApiOptionKeys.OPTION_API_ENABLED);
        this.apiEnabled = null == apiEnabled ? false : apiEnabled;
    }

    public boolean isApiEnabled() {
        return apiEnabled;
    }

    public String getApiHost() {
        return apiHost;
    }

    public Integer getApiPort() {
        return apiPort;
    }
}

