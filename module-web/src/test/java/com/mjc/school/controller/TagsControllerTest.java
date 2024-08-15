package com.mjc.school.controller;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;


@SpringBootTest
public class TagsControllerTest {
    @BeforeEach
    public void setup() {
        RestAssured.baseURI = "http://localhost:8082/api/v1";
        RestAssured.port = 8082;
    }

    @Test
    public void readAllCommentTest() {
        given()
                .contentType("application/json")
                .param("page", 0)
                .param("size", 5)
                .param("sortBy", "name,desc")
                .when()
                .request("GET", "/tag")
                .then()
                .statusCode(200);
    }

    @Test
    @Transactional
    @Rollback
    public void readTagByIdTest() {
        Response response = createTagExample();
        given()
                .contentType("application/json")
                .request("GET", "/tag/" + response.jsonPath().getInt("id"))
                .then()
                .statusCode(200)
                .body("name", equalTo(response.jsonPath().getString("name")));
        deleteTagExample(response);

    }

    @Test
    @Transactional
    @Rollback
    public void createTagTest() {
        Response response = RestAssured.given()
                .contentType("application/json")
                .body("{ \"name\": \"business\" }")
                .when()
                .request("POST", "/tag")
                .then()
                .statusCode(201)
                .body("name", equalTo("business"))
                .extract().response();
        deleteTagExample(response);
    }

    @Test
    @Transactional
    @Rollback
    public void updateTagTest() {
        Response response = createTagExample();
        Response responseUpdatedTag = RestAssured.given()
                .contentType("application/json")
                .body("{ \"name\": \"application\" }")
                .when()
                .request("PATCH", "/tag/" + response.jsonPath().getInt("id"))
                .then()
                .statusCode(200).extract().response();
        given()
                .contentType("application/json")
                .request("GET", "/tag/" + response.jsonPath().getInt("id"))
                .then()
                .statusCode(200)
                .body("name", equalTo(responseUpdatedTag.jsonPath().getString("name")));
        deleteTagExample(response);
    }

    @Test
    @Transactional
    @Rollback
    public void deleteTagTest() {
        Response response = RestAssured.given()
                .contentType("application/json")
                .body("{ \"name\": \"programming\" }")
                .when()
                .request("POST", "/tag")
                .then()
                .statusCode(201)
                .extract().response();
        deleteTagExample(response);

    }

    public Response createTagExample() {
        return RestAssured.given()
                .contentType("application/json")
                .body("{ \"name\": \"business\" }")
                .when()
                .request("POST", "/tag")
                .then()
                .statusCode(201)
                .body("name", equalTo("business"))
                .extract().response();

    }

    public void deleteTagExample(Response response) {
        given()
                .contentType("application/json")
                .request("DELETE", "/tag/" + response.jsonPath().getInt("id"));
    }
}
