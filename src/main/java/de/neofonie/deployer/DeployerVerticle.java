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
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Deploys verticles defined in the deployer.json configuration file. The
 * deployer iterates through the configuration and deploys the verticles. It
 * respects the "dependsOn" order. It will only deploy verticles in the
 * chronological order in the configuration and only when the dependencies are
 * satisfied. When a verticle is not deployed, the deployer-verticle exits.
 *
 * You can only initialize one DeployerVerticle at once. Otherwise you will
 * deploy verticles in parallel.
 *
 * The deployer uses a localHandler and does not propagate events across the
 * cluster. You'll notice that localConsumer doesn't accept an AsyncResult
 * handler. The deployment can happen synchronously.
 *
 * @author jan.decooman@neofonie.de, jonas.muecke@neofonie.de
 */
public class DeployerVerticle extends AbstractVerticle {

    private static final Logger LOG = Logger.getLogger(DeployerVerticle.class.getName());

    protected final static String LOOPBACK = "local://" + DeployerVerticle.class.getName();

    protected final static String VERTICLES = "verticles";
    
    protected final static String CONFIG = "config";

    private JsonArray deployed = null;

    private JsonObject workingCopy = null;

    private JsonObject globalConfig = null;

    /**
     * Start the deployer.
     *
     * @param startFuture
     */
    @Override
    public void start(final Future<Void> startFuture) {

        // load the deployer.json when available
        JsonObject configuration = this.loadConfiguration();

        if (configuration != null) {

            deployed = new JsonArray();
            
            // assign loopback to this handler
            vertx.eventBus().localConsumer(LOOPBACK, this::deployVerticle);

            // copy the current verticle configuration
            workingCopy = configuration.
                    getJsonObject(VERTICLES, new JsonObject()).
                    copy();

            // set the global configuration
            globalConfig = configuration.
                    getJsonObject(CONFIG, new JsonObject());

            // start iterations
            vertx.eventBus().send(LOOPBACK, workingCopy, (AsyncResult<Message<Boolean>> event) -> {
                if (event.succeeded() && event.result().body()) {
                    LOG.log(Level.INFO, "Deployed {0} Verticles: {1}", new Object[]{this.deployed.size(), deployed});
                    startFuture.complete();
                } else {
                    LOG.log(Level.SEVERE, "Deployment stopped: {0}", event.cause().getMessage());
                    startFuture.fail(event.cause());
                }
            });
        } else {
            LOG.info("No deployer.json found on the classpath.");
        }
    }

    /**
     * Load the configuration
     * @return JsonObject with the configuration
     */
    protected JsonObject loadConfiguration() {
        return ConfigLoader.loadConfiguration();
    }

    /**
     * Stop this verticle.
     */
    @Override
    public void stop() {
        // just try to write to the log, when it is still there
        if (LOG != null) {
            LOG.log(Level.INFO, "Undeploying {0}", DeployerVerticle.class.getName());
        }
    }

    /**
     * Iterate and deploy verticles
     */
    private void deployVerticle(final Message<JsonObject> event) {

        // iterate over all candidates to be deployed
        Set<String> candidates = this.workingCopy.fieldNames();

        // detach from underlying json
        Map<String, JsonObject> initiants = new HashMap<>();
        candidates.forEach(id -> {
            JsonObject info = this.workingCopy.getJsonObject(id);
            JsonArray dependsOn = info.getJsonArray("dependsOn");
            if (dependsOn != null && deployed.getList().containsAll(dependsOn.getList())
                    || dependsOn == null || dependsOn.isEmpty()) {
                initiants.put(id, info);
            }
        });

        // remove the initiants
        initiants.keySet().forEach(id -> this.workingCopy.remove(id));

        // setup latch for the reply
        CountDownLatch latch = new CountDownLatch(initiants.size());
        if (initiants.isEmpty()) {
            event.reply(Boolean.TRUE);
            return;
        }

        // run over all dependencies
        initiants.forEach((id, info) -> {

            // get the name of the verticle
            String name = info.getString("name");
            final JsonObject localConfig = new JsonObject();
            localConfig.mergeIn(globalConfig);
            localConfig.mergeIn(info.getJsonObject("config", new JsonObject()));

            Handler<AsyncResult<String>> handler = innerEvent -> {
                if (innerEvent.succeeded()) {
                    // add service to deployed-list
                    deployed.add(id);

                    // re-emit
                    vertx.eventBus().send(LOOPBACK, workingCopy, (AsyncResult<Message<Boolean>> recursiveReply) -> {
                        // always decrease latch
                        latch.countDown();

                        if (recursiveReply.succeeded() && recursiveReply.result().body()) {
                            if (latch.getCount() == 0) {
                                event.reply(recursiveReply.result().body() & Boolean.TRUE);
                            }
                        } else {
                            event.fail(500, this.getFailure(id, recursiveReply));
                        }
                    });

                } else {
                    event.fail(500, id + " >> " + innerEvent.cause().getMessage());
                }
            };

            LOG.log(Level.INFO, "Deploying: ''{0}''", new Object[]{id});
            DeploymentOptions deploymentOptions = new DeploymentOptions(info);
            vertx.deployVerticle(name, deploymentOptions.setConfig(localConfig), handler);
        });
    }

    private String getFailure(final String id, final AsyncResult innerEvent) {
        return "'" + id + "' -> "
                + ((innerEvent != null && innerEvent.cause() != null)
                        ? innerEvent.cause().getMessage() : "[nothing]");
    }
}
