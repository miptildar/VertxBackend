import com.seeu.database.PostgreSQLVerticle;
import com.seeu.database.DatabaseService;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(VertxUnitRunner.class)
public class DatabaseTest {

    private Vertx vertx;

    private DatabaseService service;


    @Before
    public void prepare(TestContext context) throws InterruptedException {
        vertx = Vertx.vertx();

        JsonObject conf = new JsonObject(); // empty config

        vertx.deployVerticle(new PostgreSQLVerticle(), new DeploymentOptions().setConfig(conf),
                context.asyncAssertSuccess(id -> {
                    service = DatabaseService.createProxy(vertx, PostgreSQLVerticle.CONFIG_QUEUE);
                })

        );
    }

    @After
    public void finish(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }


    @Test
    public void put_get_data(TestContext context) {
        Async async = context.async();

        // Since "id" column is a PRIMARY KEY, is has to be unique in each test!
        String idColumnValue = "000", nameColumnValue = "Text text";


        // Params instead of "?" in the SQL queries
        JsonArray put_params = new JsonArray()
                .add(idColumnValue)     // column1 value
                .add(nameColumnValue);  // column2 value

        JsonArray get_params = new JsonArray()
                .add(idColumnValue);    // column value



        // This is a special construction for database testing:
        service.simplePut(put_params, context.asyncAssertSuccess(putResultHandler -> {
                    service.simpleGet(get_params, context.asyncAssertSuccess(getResultHandler -> {
                        String extractedFromDBName = getResultHandler.getString("name");
                        context.assertEquals(nameColumnValue, extractedFromDBName);
                        async.complete();
                    }));
                })
        );


        async.awaitSuccess(5000);


        // For more information, see https://vertx.io/docs/guide-for-java-devs/#_testing_vert_x_code
    }

}
