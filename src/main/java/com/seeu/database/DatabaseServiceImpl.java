package com.seeu.database;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * For more details, see
 * https://vertx.io/docs/guide-for-java-devs/#_database_service_implementation
 */
public class DatabaseServiceImpl implements DatabaseService {

    Logger logger = Logger.getLogger(DatabaseServiceImpl.class.getName());

    private final SQLClient sqlClient;

    private final HashMap<SqlQuery, String> sqlQueries;

    public DatabaseServiceImpl(SQLClient sqlClient, HashMap<SqlQuery, String> sqlQueries, Handler<AsyncResult<DatabaseService>> readyHandler) {
        this.sqlClient = sqlClient;
        this.sqlQueries = sqlQueries;

        // here one can init tables
        sqlClient.getConnection(res -> {
            if (res.succeeded()) {
                // Got a connection
                SQLConnection connection = res.result();

                connection.execute("CREATE TABLE IF NOT EXISTS \"vertx\" ( id VARCHAR(10) PRIMARY KEY, name VARCHAR(30))",
                        create -> {
                            connection.close();
                            if (create.failed()) {
                                logger.log(Level.INFO, "Database preparation error " + create.cause());
                                readyHandler.handle(Future.failedFuture(create.cause()));
                            } else {
                                logger.log(Level.INFO, "Table created");
                                readyHandler.handle(Future.succeededFuture(this));
                            }
                        });


            } else {
                // Failed to get connection - deal with it
                readyHandler.handle(Future.failedFuture(res.cause()));
            }
        });
    }

    /**
     * Some request to DB
     */
    @Override
    public DatabaseService simpleGet(JsonArray params, Handler<AsyncResult<JsonObject>> resultHandler) {
        sqlClient.queryWithParams(sqlQueries.get(SqlQuery.GET), params, result -> {
            if (result.succeeded()) {
                JsonArray row = result.result().getResults()
                        .stream()
                        .findFirst()
                        .get();

                JsonObject response = new JsonObject();

                String name = row.getString(1);  // getting information from columns
                response.put("name", name);

                resultHandler.handle(Future.succeededFuture(response));
            } else {
                resultHandler.handle(Future.failedFuture(result.cause()));
            }

        });

        return this;
    }

    @Override
    public DatabaseService simplePut(JsonArray params, Handler<AsyncResult<JsonObject>> resultHandler) {
        sqlClient.queryWithParams(sqlQueries.get(SqlQuery.PUT), params, result -> {
            if (result.succeeded()) {
                resultHandler.handle(Future.succeededFuture());
            } else {
                result.cause().printStackTrace();
                resultHandler.handle(Future.failedFuture(result.cause()));
            }
        });
        return this;
    }
}
