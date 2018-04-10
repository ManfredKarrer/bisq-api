package io.bisq.api.app;

import bisq.common.setup.CommonSetup;
import bisq.common.util.Utilities;
import bisq.desktop.app.UncaughtExceptionHandler;
import com.google.inject.*;
import com.google.inject.name.Names;
import io.bisq.spi.LoadableExtension;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.IOException;
import java.security.Security;
import java.util.Locale;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.stream.StreamSupport;

@Slf4j
public class Main {

    final ServiceLoader<LoadableExtension> loader;

    public static void main(String[] args) throws Exception {
        // Need to set default locale initially otherwise we get problems at non-english OS
        Locale.setDefault(new Locale("en", Locale.getDefault().getCountry()));
        Utilities.removeCryptographyRestrictions();
        try {
            Utilities.checkCryptoPolicySetup();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
        if (null == Security.getProvider(BouncyCastleProvider.PROVIDER_NAME))
            Security.addProvider(new BouncyCastleProvider());
        Utilities.printSysInfo();
//        TODO BisqAppMain initiates bisqEnv then initiates app dir and then once again initiates bisqEnv
//        TODO BisqAppMain main method also sets context classloader to current thread (some javafx quirks)
        Thread.currentThread().setContextClassLoader(Main.class.getClassLoader());
        new Main().run(args);
    }

    public Main() {
        loader = ServiceLoader.load(LoadableExtension.class);
    }

    private void run(String[] args) throws IOException {
        final OptionSet optionSet = parseOptions(args);
        final AbstractModule aggregatedModule = constructAggregatedModule(optionSet);
        final Injector injector = Guice.createInjector(aggregatedModule);
        injector.getInstance(Key.get(File.class,Names.named("storageDir"))) ;
        final UncaughtExceptionHandler uncaughtExceptionHandler = injector.getInstance(UncaughtExceptionHandler.class);
        uncaughtExceptionHandler.addExceptionHandler(this::handleError);
        CommonSetup.setup(uncaughtExceptionHandler::broadcast);
        setup(injector);
        preStart(injector);
        start(injector);
    }

    private void handleError(Throwable throwable, Boolean doShutDown) {
        final String message = throwable.getMessage();
        final String messageToLog = null == message ? throwable.toString() : message;
        log.error(messageToLog, throwable);
    }

    private OptionSet parseOptions(String[] args) throws IOException {
        final OptionParser parser = decorateOptionParser();
        OptionSet optionSet;
        try {
            optionSet = parser.parse(args);
            return optionSet;
        } catch (OptionException e) {
            System.err.println(e.getMessage());
            parser.printHelpOn(System.err);
            System.exit(1);
            return null;
        }
    }

    private OptionParser decorateOptionParser() throws IOException {
        final OptionParser parser = new OptionParser();
        loader.forEach(extension -> extension.decorateOptionParser(parser));
        return parser;
    }

    private AbstractModule constructAggregatedModule(final OptionSet optionSet) {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(UncaughtExceptionHandler.class).in(Singleton.class);
                loader.forEach(extension -> install(extension.configure(optionSet)));
            }
        };
    }

    private void setup(Injector injector) {
//        TODO probably here we should setup extensions in separate threads
        StreamSupport.stream(loader.spliterator(), true).map(i -> i.setup(injector))
                .map(CompletableFuture::join)
                .count();
    }

    private void preStart(Injector injector) {
//        TODO probably here we should preStart extensions in separate threads
        StreamSupport.stream(loader.spliterator(), true).map(i -> i.preStart(injector))
                .map(CompletableFuture::join)
                .count();
    }

    private void start(Injector injector) {
//        TODO probably here we should start extensions in separate threads
        loader.forEach(extension -> extension.start(injector));
    }
}
