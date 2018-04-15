package com.seeu.database;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.PostgreSQLClient;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.serviceproxy.ProxyHelper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PostgreSQLVerticle extends AbstractVerticle {

    Logger logger = Logger.getLogger(PostgreSQLVerticle.class.getName());

    public static final String CONFIG_JDBC_URL_KEY = "jdbc.url";
    public static final String CONFIG_JDBC_DRIVER_CLASS_KEY = "jdbc.driver_class";
    public static final String CONFIG_JDBC_MAX_POOL_SIZE_KEY = "jdbc.max_pool_size";
    public static final String CONFIG_SQL_QUERIES_RESOURCE_FILE_KEY = "sqlqueries.resource.file";
    public static final String CONFIG_QUEUE = "queue";


    public static final String PostgreSQL_POOL_NAME = "PostgreSQLPool";

    private SQLClient postgreSQLClient;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        logger.log(Level.INFO, "Starting PostgreSQLVerticle..");

        HashMap<SqlQuery, String> sqlQueries = loadSQLQueries();

        // https://vertx.io/docs/vertx-mysql-postgresql-client/java/
        JsonObject postgreSQLClientConfig = new JsonObject()
                .put("host", "localhost")
                .put("port", 5432)
                .put("maxPoolSize", 30)
                .put("username", "postgres")
                .put("password", "passw0rd")
                .put("database", "my_database");
        postgreSQLClient = PostgreSQLClient.createShared(vertx, postgreSQLClientConfig, PostgreSQL_POOL_NAME);


        DatabaseService.create(postgreSQLClient, sqlQueries, ready -> {
            if (ready.succeeded()) {
                ProxyHelper.registerService(DatabaseService.class, vertx, ready.result(), CONFIG_QUEUE);
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
            queriesInputStream = getClass().getResourceAsStream("/db_queries.properties");
        }

        Properties queriesProps = new Properties();
        queriesProps.load(queriesInputStream);
        queriesInputStream.close();


        HashMap<SqlQuery, String> sqlQueries = new HashMap<>();
        sqlQueries.put(SqlQuery.GET, queriesProps.getProperty("get-query"));
        sqlQueries.put(SqlQuery.PUT, queriesProps.getProperty("put-query"));
        // ... save other queries from the file

        System.out.println(SqlQuery.GET.name()+" => "+queriesProps.getProperty("get-query"));
        System.out.println(SqlQuery.PUT.name()+" => "+queriesProps.getProperty("put-query"));

        return sqlQueries;
    }

}
