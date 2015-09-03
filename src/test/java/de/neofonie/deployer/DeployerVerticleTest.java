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

import org.junit.Test;
import io.vertx.core.AsyncResult;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import org.junit.Rule;
import org.junit.runner.RunWith;
import static de.neofonie.deployer.DeployerMock.*;
import java.util.List;

/**
 * Test the config-loader with different JSONs.
 *
 * @author jan.decooman@neofonie.de
 */
@RunWith(io.vertx.ext.unit.junit.VertxUnitRunner.class)
public class DeployerVerticleTest {

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    /**
     * Test a single deployment.
     *
     * @param context The Vertx context
     */
    @Test
    public void simpleDeployment(final TestContext context) {

        DeployerVerticle mock = prepareDeployer("/simple.json");

        Async async = context.async();
        rule.vertx().deployVerticle(mock,
                (AsyncResult<String> serverReply) -> {
                    try {
                        context.assertTrue(serverReply.succeeded());
                        context.assertFalse(serverReply.failed());
                        context.assertTrue(mock.deployed.contains("verticle-simple"));
                    } finally {
                        async.complete();
                        rule.vertx().close();
                    }
                });
    }

    /**
     * Test a single deployment with unknown verticle.
     *
     * @param context The Vertx context
     */
    @Test
    public void wrongDeployment(final TestContext context) {

        DeployerVerticle mock = prepareDeployer("/simple-wrong.json");

        Async async = context.async();
        rule.vertx().deployVerticle(mock,
                (AsyncResult<String> serverReply) -> {
                    try {
                        context.assertFalse(serverReply.succeeded());
                        context.assertTrue(serverReply.failed());
                    } finally {
                        async.complete();
                        rule.vertx().close();
                    }
                });
    }

    /**
     * Test a single deployment with a config.
     *
     * @param context The Vertx context
     */
    @Test
    public void configDeployment(final TestContext context) {

        DeployerVerticle mock = prepareDeployer("/simple-config.json");

        Async async = context.async();
        rule.vertx().deployVerticle(mock,
                (AsyncResult<String> serverReply) -> {
                    context.assertTrue(serverReply.succeeded());
                    async.complete();
                    rule.vertx().close();
                });
    }

    /**
     * Test a a deployment with three depending serial verticles.
     *
     * @param context The Vertx context
     */
    @Test
    public void dependsOnSerial(final TestContext context) {

        DeployerVerticle mock = prepareDeployer("/depending-serial.json");
        TestVerticle3.order.clear();

        Async async = context.async();
        rule.vertx().deployVerticle(mock,
                (AsyncResult<String> serverReply) -> {
                    List<String> ids = TestVerticle3.order;
                    context.assertTrue(serverReply.succeeded());
                    context.assertFalse(ids.isEmpty());
                    context.assertTrue(ids.size() == 3);
                    context.assertTrue("v1".equals(ids.get(0)));
                    context.assertTrue("v3".equals(ids.get(1)));
                    context.assertTrue("v2".equals(ids.get(2)));
                    async.complete();
                    rule.vertx().close();
                });
    }

    /**
     * Test a a deployment with three depending verticles in parallel and serial
     *
     * @param context The Vertx context
     */
    @Test
    public void dependsOnParallel(final TestContext context) {

        DeployerVerticle mock = prepareDeployer("/depending-parallel.json");
        TestVerticle3.order.clear();

        Async async = context.async();
        rule.vertx().deployVerticle(mock,
                (AsyncResult<String> serverReply) -> {
                    List<String> ids = TestVerticle3.order;
                    context.assertTrue(serverReply.succeeded());
                    context.assertFalse(ids.isEmpty());
                    context.assertTrue(ids.size() == 3);
                    context.assertTrue("v1".equals(ids.get(0)) || "v2".equals(ids.get(0)));
                    context.assertTrue("v1".equals(ids.get(1)) || "v2".equals(ids.get(1)));
                    context.assertTrue("v3".equals(ids.get(2)));
                    async.complete();
                    rule.vertx().close();
                });
    }

}
