import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ApiTest {


    /**
     * https://vertx.io/docs/guide-for-java-devs/#_authenticating_web_api_requests_with_jwt
     * @param context
     */
    @Test
    public void jwt_test(TestContext context){
        System.out.println("Testing JSON Web Token authorization");

    }

    @Test
    public void test(TestContext context){
        Vertx vertx = Vertx.vertx();
    }

}
