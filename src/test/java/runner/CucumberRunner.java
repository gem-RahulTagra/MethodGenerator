package runner;


import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
        features = "src/test/resources/features"
        ,glue = "stepdefinition"
        ,tags = "@reg"
)
public class CucumberRunner {

    @BeforeClass
    public static void setupBefClass() {
        System.out.println("---------------------------------------Before Class---------------------------------------");
    }

    @AfterClass
    public static void setupAftClass() {
        System.out.println("---------------------------------------After Class---------------------------------------");
    }
}
