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
import static de.neofonie.deployer.DeployerVerticle.*;
import java.net.URL;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Test the config-loader with different JSONs.
 *
 * @author jan.decooman@neofonie.de
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ConfigLoader.class)
public class ConfigLoaderTest {

    /**
     * Load the configuration from the classpath and check the content.
     */
    @Test
    public void configuration() {
        JsonObject loadConfiguration = ConfigLoader.loadConfiguration();

        JsonObject config = loadConfiguration.getJsonObject(CONFIG);
        assertNotNull(config);
        assertTrue(!config.fieldNames().isEmpty());

        JsonObject verticles = loadConfiguration.getJsonObject(VERTICLES);
        assertNotNull(verticles);
        assertTrue(!verticles.fieldNames().isEmpty());
    }

    /**
     * Load a non existing configuration.
     */
    @Test
    public void missingConfiguration() {
        PowerMockito.mockStatic(ConfigLoader.class);
        Mockito.when(ConfigLoader.loadURL()).thenReturn(null);
        Mockito.when(ConfigLoader.loadConfiguration()).thenCallRealMethod();

        JsonObject loadConfiguration = ConfigLoader.loadConfiguration();
        assertNull(loadConfiguration);
    }

    /**
     * Load an invalid configuration.
     */
    @Test
    public void invalidConfiguration() {
        URL invalidJson = ConfigLoader.class.getResource("/invalid.json");
        assertNotNull(invalidJson);

        PowerMockito.mockStatic(ConfigLoader.class);
        Mockito.when(ConfigLoader.loadURL()).thenReturn(invalidJson);
        Mockito.when(ConfigLoader.loadConfiguration()).thenCallRealMethod();

        JsonObject loadConfiguration = ConfigLoader.loadConfiguration();
        assertNull(loadConfiguration);
    }
}
