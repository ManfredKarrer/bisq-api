package io.bisq.api.app;

import bisq.common.util.Utilities;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.bisq.spi.LoadableExtension;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.IOException;
import java.security.Security;
import java.util.Locale;
import java.util.ServiceLoader;

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
        Security.addProvider(new BouncyCastleProvider());
        new Main().run(args);
    }

    public Main() {
        loader = ServiceLoader.load(LoadableExtension.class);
    }

    private void run(String[] args) throws IOException {
        final OptionSet optionSet = parseOptions(args);
        final AbstractModule aggregatedModule = constructAggregatedModule(optionSet);
        final Injector injector = Guice.createInjector(aggregatedModule);
        start(injector);
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
                loader.forEach(extension -> install(extension.configure(optionSet)));
            }
        };
    }

    private void start(Injector injector) {
        loader.forEach(extension -> extension.start(injector));
    }
}
