package org.unencrypted.obscene;


import com.google.common.util.concurrent.AbstractIdleService;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.unencrypted.obscene.indexer.IndexService;
import org.unencrypted.obscene.server.IndexServer;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Main extends AbstractIdleService {
    private final IndexService indexer;
    private final IndexServer indexServer;

    private static final Logger log = LogManager.getLogger(Main.class);


    public Main(ObsceneConfiguration configuration) throws IOException {
        indexer = new IndexService(configuration);
        indexServer = new IndexServer(new ObsceneConfiguration(new SystemConfiguration()), indexer);
    }

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();

        if(args.length > 0) {
            SystemConfiguration.setSystemProperties(args[0]);
        }

        log.info("Configuring Obscene Main");
        Configuration config = new SystemConfiguration();
        ObsceneConfiguration configuration = new ObsceneConfiguration(config);
        final Main mainService = new Main(configuration);

        SignalHandler handler = new SignalHandler() {
            @Override
            public void handle(Signal signal) {
                log.info("Shutting down Obscene Main");
                mainService.stopAndWait();
                log.info("Obscene Main shutdown");
            }
        };

        Signal.handle(new Signal("TERM"), handler);
        Signal.handle(new Signal("INT"), handler);

        log.info("Staring Obscene Main");
        mainService.startAndWait();
        log.info("Obscene Main Started");
    }

    @Override
    protected void startUp() throws Exception {
        log.info("Starting Indexer");
        indexer.startAndWait();
        log.info("Indexer Started");

        log.info("Starting HTTP server");
        indexServer.startAndWait();
        log.info("HTTP server started");
    }

    @Override
    protected void shutDown() throws Exception {
        log.info("Stopping indexer");
        indexer.stopAndWait();
        log.info("Indexer Stopped");

        indexServer.stopAndWait();
    }
}
