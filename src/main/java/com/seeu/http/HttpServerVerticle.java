package com.seeu.http;

import com.seeu.database.DatabaseService;
import com.seeu.database.PostgreSQLVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.JWTAuthHandler;

import java.util.logging.Logger;

import static com.seeu.database.PostgreSQLVerticle.CONFIG_QUEUE;

public class HttpServerVerticle extends AbstractVerticle {

    Logger logger = Logger.getLogger(HttpServerVerticle.class.getName());

    DatabaseService databaseService;

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        // Connection with database service
        String dbQueue = config().getString(CONFIG_QUEUE, "queue");
        databaseService = DatabaseService.createProxy(vertx, dbQueue);


        Future<Void> future = Future.future();

        HttpServerOptions httpServerOptions = new HttpServerOptions();
        httpServerOptions.setSsl(true);
        httpServerOptions.setKeyStoreOptions(
                new JksOptions().setPath("keystore/ssl_keystore.jks").setPassword("test1/")
        );

        HttpServer server = vertx.createHttpServer(httpServerOptions);

        Router router = Router.router(vertx);
        router.route("/some/path/1").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();

            // enable chunked responses because we will be adding data as
            // we execute over other handlers. This is only required once and
            // only if several handlers do output.
            response.setChunked(true);

            response.write("route1");

            // Call the next matching route after a 5 second delay
            routingContext.vertx().setTimer(5000, tid -> routingContext.next());
        });

        router.route("/get/:id").handler(this::getSomethingHandler);




        JWTAuth jwtAuth = JWTAuth.create(vertx, new JsonObject()
                .put("keyStore",
                        new JsonObject()
                                .put("path", "keystore/jwt_keystore.jceks")
                                .put("type", "jceks")
                                .put("password", "test1/")
                )
        );

        // Create new JWT token
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



    private void getSomethingHandler(RoutingContext context){
        String id = context.request().getParam("id");

        JsonArray params = new JsonArray()
                .add("vertx")   // database name
                .add("vertx")   // table name
                .add("id")      // column name
                .add(id);       // column value

        databaseService.simpleGet(params, resultHandler -> {
            if(resultHandler.succeeded()){
                context.put("db_name_column", resultHandler.result().getString("name"));
            }
        });
    }


    private void putSomethingHandler(RoutingContext context){

    }

}
