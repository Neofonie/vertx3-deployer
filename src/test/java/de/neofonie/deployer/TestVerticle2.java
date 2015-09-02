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
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * Simple Verticle checking its configuration.
 *
 * @author jan.decooman@neofonie.de
 */

public class TestVerticle2 extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        JsonObject config = context.config();
        if ("text".equals(config.getString("field3"))
                && Integer.valueOf(300).equals(config.getInteger("field2"))
                && config.getJsonObject("field1") != null) {
            startFuture.complete();
        } else {
            startFuture.fail("wrong config");
        }
    }

}
