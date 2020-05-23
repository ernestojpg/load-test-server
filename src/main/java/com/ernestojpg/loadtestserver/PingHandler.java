package com.ernestojpg.loadtestserver;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * PingHandler.java
 *
 * @author <a href="mailto:ernestojpg@gmail.com">Ernesto J. Perez</a>
 * @since 23/05/2020
 */
public class PingHandler implements Handler<RoutingContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PingHandler.class);

    private final Random random = new Random();

    @Override
    public void handle(RoutingContext context) {
        final HttpServerRequest request = context.request();

        final int delay = getDelay(request);
        context.put("delay", delay);
        if (delay > 0) {
            request.pause();
            context.vertx().setTimer(delay, tid -> {
                request.resume();
                produceResponse(context);
            });
        } else {
            produceResponse(context);
        }
    }

    /**
     * delay: <delay>
     * random-delay: [<minDelay>,]<maxDelay> Delay between <minDelay> and <maxDelay>, both inclusive
     * @param request
     * @return
     */
    protected int getDelay(HttpServerRequest request) {
        final String delayHeader = request.getHeader("delay");
        if (delayHeader != null) {
            try {
                return Integer.parseInt(delayHeader);
            } catch (NumberFormatException ex) {
                LOGGER.warn("Received invalid header. 'delay: {}'", delayHeader);
            }
        }

        final String randomDelayHeader = request.getHeader("random-delay");
        if (randomDelayHeader != null) {
            try {
                final String[] pieces = randomDelayHeader.split(",");
                final int minDelay;
                final int maxDelay;
                if (pieces.length == 1) {
                    minDelay = 0;
                    maxDelay = Integer.parseInt(pieces[0].trim());
                } else if (pieces.length == 2) {
                    minDelay = Integer.parseInt(pieces[0].trim());
                    maxDelay = Integer.parseInt(pieces[1].trim());
                } else {
                    throw new NumberFormatException();
                }
                return minDelay + random.nextInt(maxDelay - minDelay + 1);
            } catch (NumberFormatException ex) {
                LOGGER.warn("Received invalid header. 'random-delay: {}'", randomDelayHeader);
            }
        }

        return 0;
    }

    protected void produceResponse(RoutingContext context) {
        final HttpServerRequest request = context.request();
        final HttpServerResponse response = context.response();

        final String contentType = request.getHeader(HttpHeaders.CONTENT_TYPE);
        response.setChunked(true);
        if (contentType != null) {
            response.putHeader(HttpHeaders.CONTENT_TYPE, contentType);
        }
        final Integer delay = context.get("delay");
        if (delay != null) {
            response.putHeader("delay", delay.toString());
        }
        request.pipeTo(response);
    }
}
