package io.bisq.api;

import joptsimple.OptionSet;

public class ApiEnvironment {

    private final String apiHost;

    private final Integer apiPort;

    private boolean enabled;

    public ApiEnvironment(OptionSet options) {
        apiHost = (String) options.valueOf("apiHost");
        apiPort = (Integer) options.valueOf("apiPort");
        enabled = !options.has("api") || (boolean) options.valueOf("api");
    }

    public String getApiHost() {
        return apiHost;
    }

    public Integer getApiPort() {
        return apiPort;
    }

    public boolean isEnabled() {
        return enabled;
    }

}
