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

import bisq.common.UserThread;
import bisq.common.app.AppModule;
import bisq.common.setup.CommonSetup;
import bisq.common.setup.UncaughtExceptionHandler;
import bisq.core.app.AppOptionKeys;
import bisq.core.app.BisqExecutable;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.bisq.api.BackupRestoreManager;
import io.bisq.api.service.BisqApiApplication;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bitcoinj.store.BlockStoreException;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Slf4j
public class ApiMain extends BisqExecutable implements UncaughtExceptionHandler {

    public static void main(String[] args) throws Exception {
        if (BisqExecutable.setupInitialOptionParser(args)) {
            // For some reason the JavaFX launch process results in us losing the thread context class loader: reset it.
            // In order to work around a bug in JavaFX 8u25 and below, you must include the following code as the first line of your realMain method:
            Thread.currentThread().setContextClassLoader(ApiMain.class.getClassLoader());

            new ApiMain().execute(args);
        }
    }

    @Override
    protected void configUserThread() {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("BisqApiMain")
                .setDaemon(true)
                .build();
        UserThread.setExecutor(Executors.newSingleThreadExecutor(threadFactory));
    }

    @Override
    protected void customizeOptionParsing(OptionParser parser) {
        super.customizeOptionParsing(parser);
        ApiOptionCustomizer.customizeOptionParsing(parser);
    }

    @Override
    protected void setupEnvironment(OptionSet options) {
        bisqEnvironment = new ApiEnvironment(options);
        final String appDir = bisqEnvironment.getProperty(AppOptionKeys.APP_DATA_DIR_KEY);
        try {
            new BackupRestoreManager(appDir).restoreIfRequested();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void launchApplication() {
        CommonSetup.setup(this);
        onApplicationLaunched();
    }

    @Override
    protected AppModule getModule() {
        return new ApiHeadlessModule(bisqEnvironment);
    }

    @Override
    protected void startApplication() {
        final MainViewModelHeadless mainViewModelHeadless = injector.getInstance(MainViewModelHeadless.class);
        final BisqApiApplication bisqApiApplication = injector.getInstance(BisqApiApplication.class);
        bisqApiApplication.setShutdown(this::shutdown);
        bisqApiApplication.run();
        mainViewModelHeadless.start();
    }

    private void shutdown() {
        gracefulShutDown(() -> {
        });
    }

    @Override
    public void handleUncaughtException(Throwable throwable, boolean b) {
        if (throwable.getCause() != null && throwable.getCause().getCause() != null &&
                throwable.getCause().getCause() instanceof BlockStoreException) {
            log.error(throwable.getMessage());
        } else {
            log.error("Uncaught Exception from thread " + Thread.currentThread().getName());
            log.error("throwableMessage= " + throwable.getMessage());
            log.error("throwableClass= " + throwable.getClass());
            log.error("Stack trace:\n" + ExceptionUtils.getStackTrace(throwable));
            throwable.printStackTrace();
            log.error("We shut down the app because an unhandled error occurred");
            // We don't use the restart as in case of OutOfMemory errors the restart might fail as well
            // The run loop will restart the node anyway...
            System.exit(EXIT_FAILURE);
        }
    }
}
