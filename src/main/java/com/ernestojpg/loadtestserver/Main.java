package com.ernestojpg.loadtestserver;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main.java
 *
 * @author <a href="mailto:ernestojpg@gmail.com">Ernesto J. Perez</a>
 * @since 22/05/2020
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        final VertxOptions vertxOptions = new VertxOptions();
        final Vertx vertx = Vertx.vertx(vertxOptions);

        final DeploymentOptions deploymentOptions = new DeploymentOptions();
        deploymentOptions.setInstances(vertxOptions.getEventLoopPoolSize() * 2);

        printInfo(vertxOptions, deploymentOptions);

        vertx.deployVerticle(ServerVerticle.class, deploymentOptions);
    }

    private static void printInfo(VertxOptions vertxOptions, DeploymentOptions deploymentOptions) {
        LOGGER.info("Number of Processors: {}", CpuCoreSensor.availableProcessors());
        LOGGER.info("Number of event loop threads: {}", vertxOptions.getEventLoopPoolSize());
        LOGGER.info("Deploying {} {} instances", deploymentOptions.getInstances(),
                ServerVerticle.class.getSimpleName());
    }
}
