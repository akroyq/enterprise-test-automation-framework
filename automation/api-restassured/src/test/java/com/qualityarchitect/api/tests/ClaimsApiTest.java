package com.qualityarchitect.api.tests;

import com.jayway.jsonpath.DocumentContext;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class ClaimsApiTest extends BaseApiTest {

    private static final String CREATE_CLAIM_PAYLOAD_PATH = "testdata/createClaim.json";
    private static final String ASSESSOR_INTEGRATION_PAYLOAD_PATH = "testdata/assessorIntegration.json";

    @Test(
        groups = {"smoke", "regression", "p0", "api", "claim-creation"},
        description = "Validate successful claim creation returns 201 Created and SUBMITTED status"
    )
    public void createClaimShouldReturn201() {
        DocumentContext requestBody = getPayloadContext(CREATE_CLAIM_PAYLOAD_PATH);

        given()
            .contentType("application/json")
            .body(requestBody.jsonString())
        .when()
            .post("/claims")
        .then()
            .statusCode(201)
            .body("id", not(emptyOrNullString()))
            .body("status", equalTo("SUBMITTED"));
    }

    @Test(
        groups = {"regression", "p1", "api", "claim-creation"},
        description = "Validate claim creation with missing required fields returns 400/422"
    )
    public void createClaimMissingRequiredFieldShouldFail() {
        DocumentContext requestBody = getPayloadContext(CREATE_CLAIM_PAYLOAD_PATH);
        requestBody.delete("$.policy_id");

        given()
            .contentType("application/json")
            .body(requestBody.jsonString())
        .when()
            .post("/claims")
        .then()
            .statusCode(anyOf(is(400), is(422)));
    }

    @DataProvider(name = "calculationCases")
    public Object[][] calculationCases() {
        return new Object[][] {
            {1000, 100, 900},
            {100, 100, 0},
            {50, 100, 0}
        };
    }

    @Test(
        dataProvider = "calculationCases",
        groups = {"smoke", "regression", "p0", "api", "calculation"},
        description = "Validate claim settlement calculation against reference business logic"
    )
    public void calculationShouldMatchReferenceData(
            int amount, int deductible, int expected) {

        DocumentContext requestBody = getPayloadContext(CREATE_CLAIM_PAYLOAD_PATH);
        requestBody.set("$.claim_amount", amount);
        requestBody.set("$.deductible", deductible);

        String id = given()
            .contentType("application/json")
            .body(requestBody.jsonString())
        .when()
            .post("/claims")
        .then()
            .statusCode(201)
            .extract().path("id");

        given()
        .when()
            .post("/claims/{id}/calculate", id)
        .then()
            .statusCode(200)
            .body("claim_id", equalTo(id))
            .body("settlement", equalTo(expected));
    }

    @Test(
        groups = {"regression", "p1", "api", "calculation"},
        description = "Validate settlement calculation endpoint idempotency across repeated requests"
    )
    public void calculationShouldBeIdempotent() {
        DocumentContext requestBody = getPayloadContext(CREATE_CLAIM_PAYLOAD_PATH);

        String id = given()
            .contentType("application/json")
            .body(requestBody.jsonString())
        .when()
            .post("/claims")
        .then()
            .statusCode(201)
            .extract().path("id");

        int first = given().post("/claims/{id}/calculate", id)
            .then().statusCode(200).extract().path("settlement");

        int second = given().post("/claims/{id}/calculate", id)
            .then().statusCode(200).extract().path("settlement");

        Assert.assertEquals(second, first, "Repeated calculation changed the settlement");
    }

    @Test(
        groups = {"regression", "p2", "api", "calculation"},
        description = "Validate calculation request for non-existent claim returns 404 Not Found"
    )
    public void unknownClaimShouldReturn404() {
        given()
        .when()
            .post("/claims/does-not-exist/calculate")
        .then()
            .statusCode(404);
    }

    @Test(
        groups = {"smoke", "regression", "p0", "api", "integration"},
        description = "Validate third-party assessor integration accepts valid payload with 202 Accepted"
    )
    public void assessorIntegrationShouldAcceptValidPayload() {
        DocumentContext requestBody = getPayloadContext(ASSESSOR_INTEGRATION_PAYLOAD_PATH);

        given()
            .contentType("application/json")
            .body(requestBody.jsonString())
        .when()
            .post("/integrations/assessor")
        .then()
            .statusCode(202)
            .body("accepted", equalTo(true))
            .body("correlation_id", not(emptyOrNullString()));
    }

    @Test(
        groups = {"regression", "p1", "api", "integration"},
        description = "Validate third-party assessor integration rejects invalid payload with 400/422 error"
    )
    public void assessorIntegrationShouldRejectInvalidPayload() {
        DocumentContext requestBody = getPayloadContext(ASSESSOR_INTEGRATION_PAYLOAD_PATH);
        requestBody.delete("$.claim_id");

        given()
            .contentType("application/json")
            .body(requestBody.jsonString())
        .when()
            .post("/integrations/assessor")
        .then()
            .statusCode(anyOf(is(400), is(422)));
    }
}
