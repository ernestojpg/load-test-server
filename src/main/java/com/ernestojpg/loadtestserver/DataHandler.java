package com.ernestojpg.loadtestserver;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * DataHandler.java
 *
 * @author <a href="mailto:ernestojpg@gmail.com">Ernesto J. Perez</a>
 * @since 24/05/2020
 */
public class DataHandler extends AbstractHandler {

    private static final int DEFAULT_RESPONSE_LENGTH = 1024;
    private final Buffer randomBuffer;

    public DataHandler() {
        // Generate a buffer of random readable characters
        this.randomBuffer = Buffer.buffer(DEFAULT_RESPONSE_LENGTH);
        for (int i=0 ; i<DEFAULT_RESPONSE_LENGTH ; i++) {
            randomBuffer.appendByte((byte)(random.nextInt(94) + 33));
        }
    }

    @Override
    protected void produceResponse(RoutingContext context) {
        final HttpServerRequest request = context.request();
        final HttpServerResponse response = context.response();

        response.putHeader(HttpHeaders.CONTENT_TYPE, "text/plain");

        final int dataLength = getDataLength(request);
        response.putHeader("data-length", String.valueOf(dataLength));
        if (dataLength <= randomBuffer.length()) {
            response.putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(dataLength));
            insertCustomResponseHeaders(context);
            response.write(randomBuffer.getBuffer(0, dataLength));
        } else {
            response.setChunked(true);
            insertCustomResponseHeaders(context);
            int remaining = dataLength;
            while (remaining > 0) {
                final int toWrite = Math.min(remaining, randomBuffer.length());
                response.write(randomBuffer.getBuffer(0, toWrite));
                remaining -= toWrite;
            }
        }
        response.end();
    }

    /**
     * data-length: <length>
     * random-data-length: [<minLength>,]<maxLength> Data length between <minLength> and <maxLength>, both inclusive
     * @param request
     * @return
     */
    protected int getDataLength(HttpServerRequest request) {
        return getSimpleNumericHeader(request, "data-length")
                .orElse(getRandomFromMinMaxHeader(request, "random-data-length")
                .orElse(DEFAULT_RESPONSE_LENGTH));
    }
}
