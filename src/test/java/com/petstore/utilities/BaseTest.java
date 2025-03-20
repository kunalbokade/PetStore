package com.petstore.utilities;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.github.javafaker.Faker;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.ITestResult;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class BaseTest implements ITestListener {

    public static ExtentSparkReporter reporter;
    public static ExtentReports extent;
    public static ExtentTest test;
    private static final String API_KEY = "special-key";
    public static String path;
    public static RequestSpecification req;
    public static String baseURI;
    protected static final Faker faker = new Faker();
    public static String env;

    public static RequestSpecification getRequestSpec() throws FileNotFoundException {
        if (req == null) {
            path = System.getProperty("user.dir") + "\\Logs\\Request_Response.log";
            PrintStream logFile = new PrintStream(new FileOutputStream(path));

            req = new RequestSpecBuilder()
                    .setBaseUri(baseURI)
                    .setRelaxedHTTPSValidation()
                    .addHeader("api_key", API_KEY)
                    .setContentType(ContentType.JSON)
                    .addFilter(RequestLoggingFilter.logRequestTo(logFile))
                    .addFilter(ResponseLoggingFilter.logResponseTo(logFile))
                    .log(LogDetail.ALL)
                    .build();
            return req;
        }
        return req;
    }

    public static ResponseSpecification getResponseSpec() {
        return new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectContentType(ContentType.JSON)
                .log(LogDetail.ALL)
                .build();
    }

    public static <T> T getJsonPath(Response response, String key, Class<T> type) {
        JsonPath js = response.jsonPath();
        return js.getObject(key, type);
    }

    protected int getRandomId() {
        return faker.number().numberBetween(1000, 9999);
    }

    protected String getRandomName() {
        return faker.name().firstName();
    }

    protected String getRandomStatus() {
        String[] statuses = {"available", "pending", "sold"};
        return statuses[faker.random().nextInt(statuses.length)];
    }

    protected String getRandomText() {
        return faker.lorem().sentence();
    }

    @BeforeSuite
    public static void setupExtendAndEnv() {

        // need to provide env from jenkins and through maven command
        // ex : "mvn clean test -Denv=Dev"  and if we didn't  provide any env then it will auto select to QA env.

        env = System.getProperty("env", "QA");
        switch (env) {
            case "Dev":
                baseURI = "Dev env URL";
                break;
            case "Staging":
                baseURI = "Staging env URL";
                break;
            case "OAT":
                baseURI = "OAT env URL";
                break;
            case "Prod":
                baseURI = "Prod env URL";
                break;
            default:
                baseURI = "https://petstore.swagger.io/v2"; // default QA env URL
        }

        String reportpath = System.getProperty("user.dir") + "\\ExtentReport\\Report.html";
        reporter = new ExtentSparkReporter(reportpath);
        reporter.config().setReportName("PET STORE API AUTOMATION");
        reporter.config().setDocumentTitle("Test Results");
        reporter.config().setTheme(Theme.STANDARD);
        extent = new ExtentReports();
        extent.attachReporter(reporter);
        extent.setSystemInfo("Application", "Pet Store");
        extent.setSystemInfo("Environment", "QA");
        extent.setSystemInfo("Operating System", System.getProperty("os.name"));
        extent.setSystemInfo("User Name", System.getProperty("user.name"));
    }

    @AfterSuite
    public void flushExtent() {
        extent.flush();
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        test.assignCategory(result.getMethod().getGroups());
        test.createNode(result.getName());
        test.log(Status.PASS, "Test Case is Passed");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        test.assignCategory(result.getMethod().getGroups());
        test.createNode(result.getName());
        test.log(Status.FAIL, "Test Case is Failed");
        test.log(Status.FAIL, result.getThrowable().getMessage());
        test.fail(result.getThrowable());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        test.assignCategory(result.getMethod().getGroups());
        test.createNode(result.getName());
        test.log(Status.SKIP, "Test Case is Skipped");
        test.log(Status.SKIP, result.getThrowable().getMessage());

    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        ITestListener.super.onTestFailedButWithinSuccessPercentage(result);
    }

    @Override
    public void onTestFailedWithTimeout(ITestResult result) {
        ITestListener.super.onTestFailedWithTimeout(result);
    }

    @Override
    public void onStart(ITestContext context) {
    }

    @Override
    public void onFinish(ITestContext context) {
        ITestListener.super.onFinish(context);
    }

    @Override
    public void onTestStart(ITestResult result) {
        ITestListener.super.onTestStart(result);
    }

}

