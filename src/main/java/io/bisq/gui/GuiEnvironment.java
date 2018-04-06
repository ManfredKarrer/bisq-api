package io.bisq.gui;

import bisq.core.app.BisqEnvironment;
import joptsimple.OptionSet;

public class GuiEnvironment extends BisqEnvironment {

    private final String theme;

    private boolean enabled;

    public GuiEnvironment(OptionSet options)
    {
        super(options);
        theme = options.has("guiTheme") ? (String) options.valueOf("guiTheme") : "light";
        enabled = !options.has("gui") || (boolean) options.valueOf("gui");
    }

    public String getTheme()
    {
        return theme;
    }

    public boolean isEnabled()
    {
        return enabled;
    }
}
