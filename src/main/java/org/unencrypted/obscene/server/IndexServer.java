package org.unencrypted.obscene.server;

import com.google.common.util.concurrent.AbstractIdleService;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.spi.inject.SingletonTypeInjectableProvider;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.unencrypted.obscene.Main;
import org.unencrypted.obscene.ObsceneConfiguration;
import org.unencrypted.obscene.indexer.IndexService;

import javax.ws.rs.core.Context;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

public class IndexServer extends AbstractIdleService {
    private final Logger log = LogManager.getLogger(IndexServer.class);
    private final Server server;

    public IndexServer(ObsceneConfiguration configuration, IndexService indexService) {
        server = new Server(new QueuedThreadPool(10, 200));
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(configuration.hostAndPort.getPort());
        server.setConnectors(new Connector[]{connector});
        server.setStopAtShutdown(true);

        ServletContextHandler root = new ServletContextHandler(server, "/", ServletContextHandler.NO_SESSIONS);
        PackagesResourceConfig jerseyPackages = new PackagesResourceConfig("org.unencrypted.obscene.resource");
        jerseyPackages.getSingletons().add(new ContextInjectableProvider<IndexService>(IndexService.class, indexService));
        ServletContainer container = new ServletContainer(jerseyPackages);
        ServletHolder holder = new ServletHolder(container);
        root.addServlet(holder, "/*");
    }

    @Override
    protected void startUp() throws Exception {
        log.info("Starting HTTP Server");
        server.start();
        log.info("HTTP Server started");
    }

    @Override
    protected void shutDown() throws Exception {
        server.stop();
    }

    protected static class ContextInjectableProvider<T> extends
            SingletonTypeInjectableProvider<Context, T> {
        protected ContextInjectableProvider(Type type, T instance) {
            super(type, instance);
        }
    }
}
