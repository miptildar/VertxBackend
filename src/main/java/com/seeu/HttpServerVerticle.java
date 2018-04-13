package com.seeu;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.jwt.JWTOptions;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.JWTAuthHandler;

public class HttpServerVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        Future<Void> future = Future.future();

        HttpServerOptions httpServerOptions = new HttpServerOptions();
        httpServerOptions.setSsl(true);
        httpServerOptions.setKeyStoreOptions(new JksOptions().setPath("keystore/KeyStore.jks").setPassword("test1/"));

        HttpServer server = vertx.createHttpServer(httpServerOptions);


        Router router = Router.router(vertx);

        router.route("/some/path/1").handler(routingContext -> {

            HttpServerResponse response = routingContext.response();
            // enable chunked responses because we will be adding data as
            // we execute over other handlers. This is only required once and
            // only if several handlers do output.
            response.setChunked(true);

            response.write("route1\n");

            // Call the next matching route after a 5 second delay
            routingContext.vertx().setTimer(5000, tid -> routingContext.next());
        });

        router.route("/some/path/2").handler(routingContext -> {

            HttpServerResponse response = routingContext.response();
            response.write("route2\n");

            // Call the next matching route after a 5 second delay
            routingContext.vertx().setTimer(5000, tid -> routingContext.next());
        });

        router.route("/some/path/3").handler(routingContext -> {

            HttpServerResponse response = routingContext.response();
            response.setChunked(true);
            response.write("route3");

            // Now end the response
            routingContext.response().end();
        });


        JWTAuth jwtAuth = JWTAuth.create(vertx, new JsonObject()
                .put("keyStore", new JsonObject()
                        .put("path", "keystore.jceks")
                        .put("type", "jceks")
                        .put("password", "secret")));

        router.route().handler(JWTAuthHandler.create(jwtAuth, "/api/token"));



        server.requestHandler(router::accept).listen(4443, ar -> {
            if (ar.succeeded()) {
                System.out.println("HTTP server running on port 8080");
                future.complete();
            } else {
                System.out.println("Could not start a HTTP server" + ar.cause());
                future.fail(ar.cause());
            }
        });

    }

}
