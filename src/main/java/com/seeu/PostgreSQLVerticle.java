package com.seeu;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PostgreSQLVerticle extends AbstractVerticle {

    Logger LOGGER = Logger.getLogger(PostgreSQLVerticle.class.getName());


    public static final String CONFIG_JDBC_URL_KEY = "jdbc.url";
    public static final String CONFIG_JDBC_DRIVER_CLASS_KEY = "jdbc.driver_class";
    public static final String CONFIG_JDBC_MAX_POOL_SIZE_KEY = "jdbc.max_pool_size";
    public static final String CONFIG_WIKIDB_SQL_QUERIES_RESOURCE_FILE_KEY = "sqlqueries.resource.file";
    public static final String CONFIG_QUEUE_KEY = "queue";

    private JDBCClient dbClient;

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        dbClient = JDBCClient.createShared(vertx, new JsonObject()
                .put("url", config().getString(CONFIG_JDBC_URL_KEY, "jdbc:hsqldb:file:db/wiki"))
                .put("driver_class", config().getString(CONFIG_JDBC_DRIVER_CLASS_KEY, "org.hsqldb.jdbcDriver"))
                .put("max_pool_size", config().getInteger(CONFIG_JDBC_MAX_POOL_SIZE_KEY, 30)));




        dbClient.getConnection(ar -> {
            if (ar.failed()) {
                LOGGER.log(Level.WARNING,"Could not open a database connection", ar.cause());
                startFuture.fail(ar.cause());
            } else {
                SQLConnection connection = ar.result();

//                connection.execute(sqlQueries.get(SqlQuery.CREATE_PAGES_TABLE), create -> {
//                    connection.close();
//                    if (create.failed()) {
//                        LOGGER.error("Database preparation error", create.cause());
//                        startFuture.fail(create.cause());
//                    } else {
//                        vertx.eventBus().consumer(config().getString(CONFIG_WIKIDB_QUEUE, "wikidb.queue"), this::onMessage);
//                        startFuture.complete();
//                    }
//                });

            }
        });


    }

}
