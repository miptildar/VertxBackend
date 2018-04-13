package com.seeu.database;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;


/**
 * 1. The @ProxyGen annotation is used to trigger the code generation of a proxy for clients of that service.
 * 2. The @Fluent annotation is optional, but allows fluent interfaces where operations can be chained by returning the service instance.
 * 3. Since services provide asynchronous results, the last argument of a service method
 * needs to be a Handler<AsyncResult<T>> where T is any of the types suitable for code generation
 */

@ProxyGen
public interface DatabaseService {

    @Fluent
    DatabaseService getSomething(Handler<AsyncResult<JsonObject>> resultHandler);


    static DatabaseService create(JDBCClient dbClient, Handler<AsyncResult<DatabaseService>> readyHandler) {
        return new DatabaseServiceImpl(dbClient, readyHandler);
    }


    // ????
    static DatabaseService createProxy(Vertx vertx, String address) {
        return null;
    }

}
