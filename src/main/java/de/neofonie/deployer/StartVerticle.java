/*
The MIT License (MIT)

Copyright (c) 2015 Neofonie GmbH

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.neofonie.deployer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * StartVerticle is a predefined start verticle for the application. It start
 * the deployer automatically and exists when the deployment was not
 * successful. On failure, exitcode 1 is returned.
 * 
 * @author jan.decooman@neofonie.de, jonas.muecke@neofonie.de
 */
public class StartVerticle extends AbstractVerticle {

    private static final Logger LOG = Logger.getLogger(StartVerticle.class.getName());

    private String deployerId = null;

    /**
     * Initialize the verticle. This verticle attaches a shutdown-hook to 
     * clean up initialized verticles.
     * 
     * @param startedResult 
     */
    @Override
    public void start(final Future<Void> startedResult) {
        Runtime.getRuntime().addShutdownHook(new Thread(this::undeploy));
        vertx.deployVerticle(new DeployerVerticle(), this::handleDeployResult);
    }

    /**
     * Undeploy the verticles.
     */
    private void undeploy() {
        if (this.deployerId != null) {
            vertx.undeploy(this.deployerId);
        }
        waitForExit();
    }

    /**
     * Give the JVM 5 seconds to shut down. When the application cannot stop
     * within 5 seconds, shut it down.
     */
    private void waitForExit() {
        if (LOG != null) {
            LOG.info("Waiting to exit....");
        }

        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            if (LOG != null) {
                LOG.log(Level.SEVERE, "Error during shutdown: " + ex.getMessage(), ex);
            }
        }
    }

    /**
     * Display the result of the deployment operation in a nice way.
     * 
     * @param reply The reply from the deployer.
     */
    private void handleDeployResult(final AsyncResult<String> reply) {
        if (reply.failed()) {
            LOG.log(Level.SEVERE, "Loading modules failed! " + 
                    reply.cause().getMessage(), reply.cause());
            vertx.close();
            System.exit(1);
        } else {
            this.deployerId = reply.result();
            LOG.info("Application ready.");
        }
    }
}
