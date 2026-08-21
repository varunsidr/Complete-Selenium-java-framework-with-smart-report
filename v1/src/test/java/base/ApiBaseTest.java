package base;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import io.qameta.allure.Allure;
import io.qameta.allure.testng.AllureTestNg;

@Listeners(AllureTestNg.class)
public class ApiBaseTest {

    @BeforeMethod(alwaysRun = true)
    public void configureAllureApiTest(Method method, Object[] testData) {
        Test annotation = method.getAnnotation(Test.class);
        String scenarioName = scenarioNameFrom(testData);
        String testClassName = method.getDeclaringClass().getSimpleName();
        Allure.label("parentSuite", "API");
        Allure.suite("Notes API");
        Allure.label("suite", endpointGroupFor(testClassName));
        Allure.feature("Notes API");
        Allure.label("layer", "api");
        Allure.label("testClass", method.getDeclaringClass().getName());
        if (annotation != null && annotation.groups().length > 0) {
            Arrays.stream(annotation.groups()).forEach(group -> Allure.label("tag", group));
        }
        Allure.story(storyNameFor(method.getName(), scenarioName));
        if (!scenarioName.isBlank()) {
            Allure.parameter("Scenario", scenarioName);
        }
    }

    private static String endpointGroupFor(String testClassName) {
        String normalized = testClassName == null ? "" : testClassName.toLowerCase(Locale.ROOT);
        if (normalized.contains("login")) {
            return "Authentication";
        }
        if (normalized.contains("health")) {
            return "Health Check";
        }
        return testClassName == null || testClassName.isBlank() ? "API" : testClassName;
    }

    private static String storyNameFor(String methodName, String scenarioName) {
        if (scenarioName != null && !scenarioName.isBlank()) {
            return scenarioName;
        }
        return methodName == null ? "API scenario" : methodName;
    }

    private static String scenarioNameFrom(Object[] testData) {
        if (testData == null || testData.length == 0 || !(testData[0] instanceof String scenarioName)) {
            return "";
        }
        return scenarioName == null ? "" : scenarioName.trim();
    }
}
