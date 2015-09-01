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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Read the configuration for the deployer. The configuration must be stored in
 * a file called "app-conf.json". This file must be available on the classpath.
 *
 * @author jan.decooman@neofonie.de, jonas.muecke@neofonie.de
 */
public class AppConfigLoader {

    private static final Logger LOG
            = Logger.getLogger(AppConfigLoader.class.getName());

    /**
     * Load the global application configuration. The global application state
     * is merged with the external configuration. The external configuration
     * takes precedence.
     *
     * @return The configuration for the deployer
     */
    public static JsonObject loadConfiguration() {
        JsonObject result = null;

        // read file by convention
        LOG.info("Looking for the deployer configuration");
        URL appConf = AppConfigLoader.class.getResource("/app-conf.json");
        if (appConf != null) {
            LOG.info("Deployer configuration found");
            try {
                System.out.println(appConf.toURI());
                URI uri = appConf.toURI();
                initFileSystemIfNeeded(uri);

                byte[] readAllBytes = Files.readAllBytes(Paths.get(appConf.toURI()));
                result = new JsonObject(new String(readAllBytes, "UTF-8"));
                LOG.info("Deployer configuration loaded");
                
            } catch (IOException | URISyntaxException e) {
                LOG.log(Level.SEVERE, "Global application configuration invalid", e);
            }
        } else {
            LOG.info("Global application configuration not found.");
        }
        return result;
    }

    /**
     * Initialize the underlying filesystem.
     *
     * @param uri
     * @throws IOException
     */
    private static void initFileSystemIfNeeded(final URI uri) throws IOException {
        final String uriAsString = uri.toString();
        if (uriAsString.contains("!")) {
            final String[] array = uri.toString().split("!");
            FileSystems.newFileSystem(URI.create(array[0]), new HashMap<>());
            LOG.log(Level.INFO, "Initialized filesystem with: {0}", URI.create(array[0]));
        } else {
            LOG.log(Level.INFO, "Skip file system initialization with uri: {0}", uriAsString);
        }
    }

}
