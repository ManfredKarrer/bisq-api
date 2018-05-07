/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package io.bisq.api.app;

import bisq.common.app.AppModule;
import bisq.core.app.BisqExecutable;
import bisq.desktop.app.BisqAppMain;
import io.bisq.api.service.BisqApiApplication;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class BisqApiWithUIMain extends BisqAppMain {

    public static void main(String[] args) throws Exception {
        if (BisqExecutable.setupInitialOptionParser(args)) {
            // For some reason the JavaFX launch process results in us losing the thread context class loader: reset it.
            // In order to work around a bug in JavaFX 8u25 and below, you must include the following code as the first line of your realMain method:
            Thread.currentThread().setContextClassLoader(BisqApiWithUIMain.class.getClassLoader());

            new BisqApiWithUIMain().execute(args);
        }
    }

    @Override
    protected void customizeOptionParsing(OptionParser parser) {
        super.customizeOptionParsing(parser);
        ApiOptionCustomizer.customizeOptionParsing(parser);
    }

    @Override
    protected void setupEnvironment(OptionSet options) {
        bisqEnvironment = new ApiEnvironment(options);
    }

    @Override
    protected AppModule getModule() {
        return new BisqApiWithUIModule(bisqEnvironment);
    }

    @Override
    protected void startApplication() {
        super.startApplication();
        if (((ApiEnvironment) bisqEnvironment).isApiEnabled())
            injector.getInstance(BisqApiApplication.class).run();
    }
}
