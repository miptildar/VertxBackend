package com.seeu.database;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.serviceproxy.ProxyHelper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

public class PostgreSQLVerticle extends AbstractVerticle {

    Logger logger = Logger.getLogger(PostgreSQLVerticle.class.getName());

    public static final String CONFIG_JDBC_URL_KEY = "jdbc.url";
    public static final String CONFIG_JDBC_DRIVER_CLASS_KEY = "jdbc.driver_class";
    public static final String CONFIG_JDBC_MAX_POOL_SIZE_KEY = "jdbc.max_pool_size";
    public static final String CONFIG_SQL_QUERIES_RESOURCE_FILE_KEY = "sqlqueries.resource.file";
    public static final String CONFIG_QUEUE_KEY = "queue";

    private JDBCClient dbClient;

    private DatabaseService databaseService;

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        HashMap<SqlQuery, String> sqlQueries = loadSQLQueries();

        dbClient = JDBCClient.createShared(vertx, new JsonObject()
                .put("url", config().getString(CONFIG_JDBC_URL_KEY, "jdbc:....."))
                .put("driver_class", config().getString(CONFIG_JDBC_DRIVER_CLASS_KEY, "...."))
                .put("max_pool_size", config().getInteger(CONFIG_JDBC_MAX_POOL_SIZE_KEY, 30)));


        databaseService = DatabaseService.create(dbClient, sqlQueries, ready -> {
            if (ready.succeeded()) {
                ProxyHelper.registerService(DatabaseService.class, vertx, ready.result(), CONFIG_QUEUE_KEY);
                startFuture.complete();
            } else {
                startFuture.fail(ready.cause());
            }
        });

    }


    private HashMap<SqlQuery, String> loadSQLQueries() throws IOException{
        String queriesFile = config().getString(CONFIG_SQL_QUERIES_RESOURCE_FILE_KEY);

        InputStream queriesInputStream;
        if (queriesFile != null) {
            queriesInputStream = new FileInputStream(queriesFile);
        } else {
            queriesInputStream = getClass().getResourceAsStream("/db-queries.properties");
        }

        Properties queriesProps = new Properties();
        queriesProps.load(queriesInputStream);
        queriesInputStream.close();


        HashMap<SqlQuery, String> sqlQueries = new HashMap<>();
        sqlQueries.put(SqlQuery.GET_SOMETHING, queriesProps.getProperty("get-something-query"));
        // ... load other queries from the file

        return sqlQueries;
    }


}
