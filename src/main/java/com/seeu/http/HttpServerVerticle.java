package com.seeu.http;

import com.seeu.database.DatabaseService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.jwt.JWTOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.logging.Logger;

import static com.seeu.database.PostgreSQLVerticle.CONFIG_QUEUE;

public class HttpServerVerticle extends AbstractVerticle {

    Logger logger = Logger.getLogger(HttpServerVerticle.class.getName());

    DatabaseService databaseService;

    JWTAuth jwtAuth;

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
        router.route("/some/path/:param").handler(routingContext -> {
            String urlParam = routingContext.request().getParam("param");

            HttpServerResponse response = routingContext.response();

            // enable chunked responses because we will be adding data as
            // we execute over other handlers. This is only required once and
            // only if several handlers do output.
            response.setChunked(true);

            response.putHeader("Content-Type", "text/html");
            response.write("<html><body><h1>Param "+urlParam+"</h1></body></html>");
            response.end();

            // Call the next matching route after a 5 second delay
            routingContext.vertx().setTimer(5000, tid -> routingContext.next());
        });

        router.route("/get/:id").handler(this::getSomethingHandler);

        // E.g. generating new token, probably bad practice.. this is only for example
        // Implement signIn/signUp methods on your own, solve problems related to username/password transferring
        router.route("/api/newToken").handler(this::newToken);






        // JSON Web Token init
        // https://vertx.io/docs/vertx-auth-jwt/java/
        JWTAuthOptions config = new JWTAuthOptions()
                .setKeyStore(new KeyStoreOptions()
                        .setPath("keystore/jwt_keystore.jceks")
                        .setPassword("test1/")
                );
        jwtAuth = JWTAuth.create(vertx, config);






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


    private void getSomethingHandler(RoutingContext context) {

        // Probably one have to perform some extra work, in order to extract jwt-token from the header, this is for example only
        String token = context.request().getHeader("Authorization");

        jwtAuth.authenticate(new JsonObject().put("jwt", token), result -> {
            if(result.succeeded()){

                // ???
                User user = result.result();


                String id = context.request().getParam("id");
                JsonArray params = new JsonArray()
                        .add(id);       // id column value

                databaseService.simpleGet(params, resultHandler -> {
                    if (resultHandler.succeeded()) {
                        //
                        context.response().putHeader("Content-Type", "text/html");
                        context.response().end(resultHandler.result().getString("name"));
                    }
                });

            }else{
                context.response().setStatusCode(401);
                context.response().end("Unauthorized");
            }
        });


    }


    private void newToken(RoutingContext context) {
        // If token is missing, then the user is unknown and you have redirect him to "Sign In" page (obviously)
        // If the current token is expired, one have to generate a new one


        JsonObject newTokenParams = new JsonObject()
                .put("username", "my_user_name");
        // probably some other information is required, read docs


        JWTOptions jwtOptions = new JWTOptions()
                .setExpiresInMinutes(3600)
                .setAlgorithm("Algorithm")
                .setSubject("My App API")
                .setIssuer("...");
        String newToken = jwtAuth.generateToken(newTokenParams, jwtOptions);
        // New token is generated, deliver it to the user
    }

}
