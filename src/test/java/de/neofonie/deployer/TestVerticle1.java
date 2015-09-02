/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.neofonie.deployer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

/**
 *
 * @author jan
 */
public class TestVerticle1 extends AbstractVerticle {

    
    @Override
    public void start(Future<Void> startFuture) throws Exception {
        startFuture.complete();
    }
    
    
}
