package com.seeu;

import com.seeu.database.PostgreSQLVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;

import java.util.logging.Level;
import java.util.logging.Logger;


public class MainVerticle extends AbstractVerticle {

    private Logger logger = Logger.getLogger(MainVerticle.class.getName());

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        Future<String> dbVerticleDeployment = Future.future();

        // Verticle deployment is asynchronous and may complete some time after the call to deploy has returned.
        // If you want to be notified when deployment is complete you can deploy specifying a completion handler:
        vertx.deployVerticle(new PostgreSQLVerticle(), dbVerticleDeployment.completer());


        // Sequential composition with compose allows to run one asynchronous operation AFTER the other.
        // When the INITIAL future (here dbVerticleDeployment) completes successfully, the composition function is invoked.
        dbVerticleDeployment.compose(id -> {

            logger.log(Level.INFO, "PostgreSQLVerticle deployed. Starting HttpServerVerticle...");

            Future<String> httpVerticleDeployment = Future.future();
            vertx.deployVerticle(
                    "com.seeu.http.HttpServerVerticle",
                    new DeploymentOptions().setInstances(3),
                    httpVerticleDeployment.completer()
            );


            // The composition function returns the NEXT future.
            // Its completion will trigger the completion of the composite operation.
            return httpVerticleDeployment;
        }).setHandler(ar -> {
            if (ar.succeeded()) {
                logger.log(Level.INFO, "Main verticle - success");
                startFuture.complete();
            } else {
                logger.log(Level.INFO, "Main verticle - fail");
                startFuture.fail(ar.cause());
            }
        });

    }

}
