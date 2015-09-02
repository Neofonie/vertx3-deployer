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

import io.vertx.core.json.JsonObject;
import org.junit.Test;
import static org.junit.Assert.*;
import io.vertx.core.AsyncResult;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

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
    public void simpleDeploy(final TestContext context) {

        JsonObject configuration = this.readConfiguration("/simple.json");
        DeployerVerticle mock = PowerMockito.mock(DeployerVerticle.class, Mockito.CALLS_REAL_METHODS);
        PowerMockito.when(mock.loadConfiguration()).thenReturn(configuration);
        
        Async async = context.async();
        rule.vertx().deployVerticle(mock,
                (AsyncResult<String> serverReply) -> {
                    try {
                        context.assertTrue(serverReply.succeeded());
                        context.assertFalse(serverReply.failed());
                    } finally {
                        async.complete();
                        rule.vertx().close();
                    }
                });
    }

    /**
     * Read a JSON configuration from the classpath.
     * 
     * @param name The filename to read from.
     * @return JsonObject with the configuration.
     */
    private JsonObject readConfiguration(final String name) {
        JsonObject result = null;
        try {
            InputStream u = DeployerVerticleTest.class.getResourceAsStream(name);
            assertNotNull(u);
            result = new JsonObject(IOUtils.toString(u));
        } catch (IOException e) {
            fail(e.getMessage());
        }
        return result;
    }
}
