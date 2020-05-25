package com.ernestojpg.loadtestserver;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.Scanner;

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

        vertx.deployVerticle(ServerVerticle.class, deploymentOptions, result -> {
            if (result.failed()) {
                LOGGER.error("Error starting the application.", result.cause());
            }
        });
    }

    private static void printInfo(VertxOptions vertxOptions, DeploymentOptions deploymentOptions) {
        final DecimalFormat decimalFormat = new DecimalFormat( "#,##0.00" );
        LOGGER.info("Operating System: {} ({})", System.getProperty("os.name"), System.getProperty("os.version"));
        LOGGER.info("Number of Processors: {}", CpuCoreSensor.availableProcessors());
        LOGGER.info("Max memory for application: {} MB", decimalFormat.format(
                ((float)Runtime.getRuntime().maxMemory()) / 1024 / 1024));
        LOGGER.info("Max open files: {} (soft) , {} (hard)", getMaxOpenFilesSoft(), getMaxOpenFilesHard());
        LOGGER.info("Number of event loop threads: {}", vertxOptions.getEventLoopPoolSize());
        LOGGER.info("Deploying {} {} instances", deploymentOptions.getInstances(),
                ServerVerticle.class.getSimpleName());
    }

    private static String getMaxOpenFilesSoft() {
        final String osName = System.getProperty("os.name");
        if (osName!=null && !osName.startsWith("Win")) {
            return executeUnixCommand("ulimit -Sn");
        }
        return null;
    }

    private static String getMaxOpenFilesHard() {
        final String osName = System.getProperty("os.name");
        if (osName!=null && !osName.startsWith("Win")) {
            return executeUnixCommand("ulimit -Hn");
        }
        return null;
    }

    private static String executeUnixCommand(String command) {
        try {
            //final Process process = Runtime.getRuntime().exec(command);
            final Process process = new ProcessBuilder()
                    .command("sh", "-c", command)
                    .start();
            try (Scanner scanner = new Scanner(process.getInputStream())) {
                return scanner.useDelimiter("\\A").next().trim();
            }
        } catch (Exception e) {
            LOGGER.warn("error executing command '{}': {}", command, e.getMessage());
        }
        return null;
    }
}
