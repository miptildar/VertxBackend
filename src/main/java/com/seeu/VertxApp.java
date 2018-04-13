package com.seeu;

import io.vertx.core.Vertx;

public class VertxApp {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        // Once you have created a verticle you need to deploy it to the Vertx instance
        vertx.deployVerticle(new MainVerticle());
    }

}
