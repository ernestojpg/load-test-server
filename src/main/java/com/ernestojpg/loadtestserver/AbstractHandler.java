package com.ernestojpg.loadtestserver;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * AbstractHandler.java
 *
 * @author <a href="mailto:ernestojpg@gmail.com">Ernesto J. Perez</a>
 * @since 24/05/2020
 */
public abstract class AbstractHandler implements Handler<RoutingContext> {

    protected final Random random = new Random();

    public static final String DELAY_HEADER = "delay";
    public static final String RANDOM_DELAY_HEADER = "random-delay";
    public static final String RESPONSE_HEADER_PREFIX = "response-";

    @Override
    public void handle(RoutingContext context) {
        final HttpServerRequest request = context.request();

        final int delay = getDelay(request);
//        context.put(DELAY_HEADER, delay);
        context.response().putHeader(DELAY_HEADER, Integer.toString(delay));
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
     * @param request HttpServerRequest instance
     * @return
     */
    protected int getDelay(HttpServerRequest request) {
        return getSimpleNumericHeader(request, DELAY_HEADER)
                .orElse(getRandomFromMinMaxHeader(request, RANDOM_DELAY_HEADER)
                        .orElse(0));
    }

    protected Optional<Integer> getSimpleNumericHeader(HttpServerRequest request, String headerName) {
        final String headerValue = request.getHeader(headerName);
        if (headerValue != null) {
            try {
                return Optional.of(new Integer(headerValue));
            } catch (NumberFormatException ex) {
                logger().warn("Received invalid header. '{}: {}'", headerName, headerValue);
            }
        }
        return Optional.empty();
    }

    protected Optional<Integer> getRandomFromMinMaxHeader(HttpServerRequest request, String headerName) {
        final String headerValue = request.getHeader(headerName);
        if (headerValue != null) {
            try {
                final String[] pieces = headerValue.split(",");
                final int minValue;
                final int maxValue;
                if (pieces.length == 1) {
                    minValue = 0;
                    maxValue = Integer.parseInt(pieces[0].trim());
                } else if (pieces.length == 2) {
                    minValue = Integer.parseInt(pieces[0].trim());
                    maxValue = Integer.parseInt(pieces[1].trim());
                } else {
                    throw new NumberFormatException();
                }
                return Optional.of(minValue + random.nextInt(maxValue - minValue + 1));
            } catch (NumberFormatException ex) {
                logger().warn("Received invalid header. '{}}: {}'", headerName, headerValue);
            }
        }
        return Optional.empty();
    }

    protected void insertCustomResponseHeaders(RoutingContext context) {
        for (Map.Entry<String, String> entry : context.request().headers()) {
            if (entry.getKey().toLowerCase().startsWith(RESPONSE_HEADER_PREFIX)) {
                context.response().putHeader(
                        entry.getKey().substring(RESPONSE_HEADER_PREFIX.length()),
                        entry.getValue());
            }
        }
    }

    protected Logger logger() {
        return LoggerFactory.getLogger(this.getClass());
    }

    abstract void produceResponse(RoutingContext context);
}
