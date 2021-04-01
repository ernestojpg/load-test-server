package com.ernestojpg.loadtestserver;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
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

    public static final String CONFIG_LISTENING_PORT = "listeningPort";
    public static final int DEFAULT_LISTENING_PORT = 8080;

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerVerticle.class);
    private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger();

    private final int instance;

    public ServerVerticle() {
        instance = INSTANCE_COUNTER.incrementAndGet();
    }

    @Override
    public void start(Promise<Void> startPromise) {
        final int listeningPort = config().getInteger(CONFIG_LISTENING_PORT, DEFAULT_LISTENING_PORT);
        final HttpServerOptions options = new HttpServerOptions();
        options.setPort(listeningPort);
        options.setCompressionSupported(true);
        final HttpServer server = vertx.createHttpServer(options);
        final Router router = Router.router(vertx);
        registerEndpoint(router, HttpMethod.GET, "/health", this::healthHandler);
        registerEndpoint(router, HttpMethod.POST, "/ping", new PingHandler());
        registerEndpoint(router, HttpMethod.GET, "/data", new DataHandler());
        registerEndpoint(router, HttpMethod.POST, "/data", new DataHandler());
        server.requestHandler(router).listen(result -> {
            if (result.failed()) {
                startPromise.fail(new IllegalStateException(
                        "Error binding to " + options.getHost() + ":" + options.getPort(), result.cause()));
            } else {
                if (instance == context.getInstanceCount()) {
                    LOGGER.info("Listening on {}:{} ...", options.getHost(), options.getPort());
                }
                startPromise.complete();
            }
        });
    }

    private void registerEndpoint(Router router, HttpMethod method, String path, Handler<RoutingContext> handler) {
        router.route(method, path).handler(handler);
        if (instance == context.getInstanceCount()) {
            LOGGER.info("Registered endpoint {}:{}", method, path);
        }
    }

    private void healthHandler(RoutingContext context) {
        final HttpServerResponse response = context.response();
        response.putHeader(HttpHeaders.CONTENT_TYPE, "text/plain");
        response.end("OK");
    }
}
