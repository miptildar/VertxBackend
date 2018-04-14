package com.seeu.database;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;

import java.util.HashMap;

/**
 * For more details, see
 * https://vertx.io/docs/guide-for-java-devs/#_database_service_implementation
 */
public class DatabaseServiceImpl implements DatabaseService {

    private final JDBCClient dbClient;

    private final HashMap<SqlQuery, String> sqlQueries;

    public DatabaseServiceImpl(JDBCClient dbClient, HashMap<SqlQuery, String> sqlQueries, Handler<AsyncResult<DatabaseService>> readyHandler){
        this.dbClient = dbClient;
        this.sqlQueries = sqlQueries;

        // here one can init tables
    }

    /**
     * Some request to DB
     */
    @Override
    public DatabaseService getSomething(String idParam, Handler<AsyncResult<JsonObject>> resultHandler) {

        dbClient.queryWithParams(sqlQueries.get(SqlQuery.GET_SOMETHING), new JsonArray().add(idParam), result -> {
            if(result.succeeded()){
                JsonArray row = result.result().getResults()
                        .stream()
                        .findFirst()
                        .get();

                JsonObject response = new JsonObject();

                // Integer id = row.getInteger(0);  // getting information from columns
                // response.put("id", id);

                resultHandler.handle(Future.succeededFuture(response));
            }else {
                resultHandler.handle(Future.failedFuture(result.cause()));
            }

        });

        return this;
    }
}
