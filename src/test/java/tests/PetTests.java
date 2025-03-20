package tests;

import com.petstore.utilities.BaseTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileNotFoundException;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class PetTests extends BaseTest {

    public int idFromResponse;
    public String nameFromResponse;
    public String statusFromResponse;
    File imageFile;
    String requestBody;

    int randomId = getRandomId();
    String randomName = getRandomName();
    String randomStatus = getRandomStatus();
    String randomText = getRandomText();

    @Test(priority = 1, groups = {"Smoke", "Regression"})
    public void addNewPet() throws FileNotFoundException {
        test = extent.createTest("Add Pet", " Test Description : Testing the addition of a new pet");
        test.info("Adding a new pet");

        requestBody = "{\n" +
                "  \"id\": " + randomId + ",\n" +
                "  \"category\": {\n" +
                "    \"id\": 0,\n" +
                "    \"name\": \"string\"\n" +
                "  },\n" +
                "  \"name\": \"" + randomName + "\",\n" +
                "  \"photoUrls\": [\n" +
                "    \"string\"\n" +
                "  ],\n" +
                "  \"tags\": [\n" +
                "    {\n" +
                "      \"id\": 0,\n" +
                "      \"name\": \"string\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"status\": \"" + randomStatus + "\"\n" +
                "}";

        Response response = given().spec(getRequestSpec())
                .body(requestBody)
                .when()
                .post("/pet")
                .then().spec(getResponseSpec())
                .assertThat()
                .body("name", equalTo(randomName))
                .body("status", equalTo(randomStatus))
                .extract().response();

        idFromResponse = getJsonPath(response, "id", Integer.class);
        nameFromResponse = getJsonPath(response, "name", String.class);
        statusFromResponse = getJsonPath(response, "status", String.class);

        test.info("ID, Name and Status from response are  " + idFromResponse + ", " + nameFromResponse + ", " + statusFromResponse);
        Assert.assertEquals(randomId, idFromResponse);
        test.pass("Test Passed " + response.asString());

    }

    @Test(priority = 2, groups = {"Smoke", "Regression"})
    public void getPetById() throws FileNotFoundException {
        test = extent.createTest("Get Pet by ID", " Test Description : Fetching pet details by ID");

        test.info("Getting pet by id " + idFromResponse);

        given()
                .spec(getRequestSpec())
                .pathParam("petId", idFromResponse)
                .when()
                .get("/pet/{petId}")
                .then()
                .spec(getResponseSpec())
                .assertThat()
                .body("id", equalTo(idFromResponse));

        test.pass("Successfully fetched pet details");

    }


    @Test(priority = 3, groups = {"Regression"})
    public void uploadPetImage() throws FileNotFoundException {
        test = extent.createTest("Upload Pet Image", " Test Description : Uploading an image for a pet");
        imageFile = new File("src/test/resources/PetImages/Dog1.jpg");

        test.info("Image Uploading...");

        given()
                .spec(getRequestSpec())
                .contentType(ContentType.MULTIPART)
                .multiPart("file", imageFile)
                .multiPart("additionalMetadata", randomText)
                .pathParam("petId", idFromResponse)
                .when()
                .post("/pet/{petId}/uploadImage")
                .then()
                .spec(getResponseSpec())
                .assertThat()
                .statusCode(200);

        test.pass("Pet image uploaded successfully");
    }


    @Test(priority = 4, groups = {"Regression"})
    public void updatePetStatus() throws FileNotFoundException {
        test = extent.createTest("Update Pet Status", " Test Description : Updating the status of a pet");

        String updateStatus = getRandomStatus();

        test.info("Updating Pet Status with " + updateStatus);

        String requestBody = "{\n" +
                "  \"id\": " + idFromResponse + ",\n" +
                "  \"category\": {\n" +
                "    \"id\": 0,\n" +
                "    \"name\": \"string\"\n" +
                "  },\n" +
                "  \"name\": \"" + nameFromResponse + "\",\n" +
                "  \"photoUrls\": [\n" +
                "    \"string\"\n" +
                "  ],\n" +
                "  \"tags\": [\n" +
                "    {\n" +
                "      \"id\": 0,\n" +
                "      \"name\": \"string\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"status\": \"" + updateStatus + "\"\n" +
                "}";

        given()
                .spec(getRequestSpec())
                .body(requestBody)
                .when()
                .put("/pet")
                .then()
                .spec(getResponseSpec())
                .assertThat()
                .body("status", equalTo(updateStatus));

        test.pass("Pet status updated successfully");
    }


    @Test(priority = 5, groups = {"Regression"})
    public void updatePetUsingForm() throws FileNotFoundException {

        test = extent.createTest("Update Pet using Form data", " Test Description : Update Pet Using Form data Test");
        test.info("Updating pet via form parameters");

        String updatedName = getRandomName();
        String updatedPetStatus = getRandomStatus();

        test.info("Updating Pet Name with " + updatedName);
        test.info("Updating Pet Status with " + updatedPetStatus);

        Response response = given().spec(getRequestSpec())
                .contentType("application/x-www-form-urlencoded")
                .pathParam("petId", idFromResponse)
                .formParam("name", updatedName)
                .formParam("status", updatedPetStatus)
                .post("/pet/{petId}")
                .then().spec(getResponseSpec())
                .extract().response();

        int actualCode = getJsonPath(response, "code", Integer.class);

        test.info("Response: " + response.getBody().asString());
        test.info("Response Code: " + response.getStatusCode());

        Assert.assertEquals(actualCode, 200);
        test.pass("Test Passed with Updating pet via form parameters");
    }

    @Test(priority = 6, groups = {"Regression"})
    public void deletePet() throws FileNotFoundException {
        test = extent.createTest("Delete Pet", " Test Description : Deleting a pet by ID");

        given()
                .spec(getRequestSpec())
                .pathParam("petId", idFromResponse)
                .when()
                .delete("/pet/{petId}")
                .then()
                .spec(getResponseSpec())
                .assertThat()
                .statusCode(200);

        test.pass("Pet deleted successfully"); //added comment
    }
}
