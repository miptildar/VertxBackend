package com.seeu.database;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;

import java.util.HashMap;


/**
 * 1. The @ProxyGen annotation is used to trigger the code generation of a proxy for clients of that service.
 * 2. The @Fluent annotation is optional, but allows fluent interfaces where operations can be chained by returning the service instance.
 * 3. Since services provide asynchronous results, the last argument of a service method
 * needs to be a Handler<AsyncResult<T>> where T is any of the types suitable for code generation
 */

@ProxyGen
public interface DatabaseService {

    @Fluent
    DatabaseService getSomething(String idParam, Handler<AsyncResult<JsonObject>> resultHandler);


    static DatabaseService create(JDBCClient dbClient, HashMap<SqlQuery, String> sqlQueries,  Handler<AsyncResult<DatabaseService>> readyHandler) {
        return new DatabaseServiceImpl(dbClient, sqlQueries, readyHandler);
    }


    /**
     *  When you compose a Vert.x application, you may want to isolate a functionality somewhere
     *  and make it available to the rest of your application. That’s the main purpose of service proxies.
     *  It lets you expose a service on the event bus, so, any other Vert.x component can consume it,
     *  as soon as they know the address (@param address) on which the service is published.
     *
     *  See, https://vertx.io/docs/vertx-service-proxy/java/
     */
    static DatabaseService createProxy(Vertx vertx, String address) {
        return new DatabaseServiceVertxEBProxy(vertx, address);
    }

}
