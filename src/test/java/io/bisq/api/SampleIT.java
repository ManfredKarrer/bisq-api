package io.bisq.api;

import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;

public class SampleIT {

    final int ALICE_API_PORT = 8080;

    final int BOB_API_PORT = 8081;

    @Test
    public void getSwagger() throws InterruptedException
    {
        given().port(ALICE_API_PORT).when().get("/swagger").then().statusCode(200).and().body(containsString("<title>Swagger UI</title>"));
    }

    @Test
    public void getAccountList() throws InterruptedException
    {
        given().port(BOB_API_PORT).when().get("/api/v1/account_list").then().statusCode(200).and().body(equalToIgnoringWhiteSpace("[]"));
    }
}
