package com.ernestojpg.loadtestserver;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Server.java
 *
 * @author <a href="mailto:ernestojpg@gmail.com">Ernesto J. Perez</a>
 * @since 22/05/2020
 */
public class ServerVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerVerticle.class);
    private static final int DEFAULT_LISTENING_PORT = 8080;
    private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger();

    private final int instance;

    public ServerVerticle() {
        instance = INSTANCE_COUNTER.incrementAndGet();
    }

    public void start() {
        final int listeningPort = config().getInteger("listeningPort", DEFAULT_LISTENING_PORT);
        final HttpServerOptions options = new HttpServerOptions();
        options.setPort(listeningPort);
        options.setCompressionSupported(true);
        final HttpServer server = vertx.createHttpServer(options);
        final Router router = Router.router(vertx);
        registerEndpoint(router, HttpMethod.POST, "/ping", new PingHandler());
        server.requestHandler(router).listen();

        if (instance == context.getInstanceCount()) {
            LOGGER.info("Listening on {}:{} ...", options.getHost(), options.getPort());
        }
    }

    private void registerEndpoint(Router router, HttpMethod method, String path, Handler<RoutingContext> handler) {
        router.route(method, path).handler(handler);
        if (instance == context.getInstanceCount()) {
            LOGGER.info("Registered endpoint {}: {}", method, path);
        }
    }
}
