package com.ernestojpg.loadtestserver;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * PingHandler.java
 *
 * @author <a href="mailto:ernestojpg@gmail.com">Ernesto J. Perez</a>
 * @since 23/05/2020
 */
public class PingHandler extends AbstractHandler {

    @Override
    protected void produceResponse(RoutingContext context) {
        final HttpServerRequest request = context.request();
        final HttpServerResponse response = context.response();

        final String contentType = request.getHeader(HttpHeaders.CONTENT_TYPE);
        response.setChunked(true);
        if (contentType != null) {
            response.putHeader(HttpHeaders.CONTENT_TYPE, contentType);
        }
        insertCustomResponseHeaders(context);
        request.pipeTo(response);
    }
}
