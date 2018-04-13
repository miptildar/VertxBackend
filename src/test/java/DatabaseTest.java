import com.seeu.PostgreSQLVerticle;
import com.seeu.database.DatabaseService;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;


@RunWith(VertxUnitRunner.class)
public class DatabaseTest {

    private Vertx vertx;

    private DatabaseService service;


    @Before
    public void prepare(TestContext context) throws InterruptedException {
        vertx = Vertx.vertx();

        JsonObject conf = new JsonObject()
                .put(PostgreSQLVerticle.CONFIG_JDBC_URL_KEY, "jdbc:hsqldb:mem:testdb;shutdown=true")
                .put(PostgreSQLVerticle.CONFIG_JDBC_MAX_POOL_SIZE_KEY, 4);

        vertx.deployVerticle(new PostgreSQLVerticle(), new DeploymentOptions().setConfig(conf),
                context.asyncAssertSuccess(id -> {
                    service = DatabaseService.createProxy(vertx, PostgreSQLVerticle.CONFIG_QUEUE_KEY);
                })

        );
    }

    @After
    public void finish(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

}
