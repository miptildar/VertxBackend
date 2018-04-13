package com.seeu.database;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;

public class DatabaseServiceImpl implements DatabaseService {

    private final JDBCClient dbClient;

    public DatabaseServiceImpl(JDBCClient dbClient, Handler<AsyncResult<DatabaseService>> readyHandler){
        this.dbClient = dbClient;

        // init tables
    }

    @Override
    public DatabaseService getSomething(Handler<AsyncResult<JsonObject>> resultHandler) {
        return null;
    }
}
