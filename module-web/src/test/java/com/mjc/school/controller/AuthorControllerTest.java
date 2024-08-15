package com.mjc.school.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.restassured.response.Response;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;


import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest
public class AuthorControllerTest {
    private static final String BASE_URI = "http://localhost:8082/api/v1";
    private static final int PORT = 8082;
    private final String APPLICATION_JSON = "application/json";

    @BeforeEach
    public void setup() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.port = PORT;
    }

    @Test
    public void readAllAuthorTest() {
        given()
                .contentType("application/json")
                .param("page", 0)
                .param("size", 5)
                .param("sortBy", "name,dsc")
                .when()
                .request("GET", "/author")
                .then()
                .statusCode(200);
    }

    @Test
    @Transactional
    @Rollback
    public void readAuthorByIdTest() {
        Response authResponse = RestAssured.given()
                .contentType("application/json")
                .body("{ \"name\": \"Roberto\" }")
                .when()
                .request("POST", "/author")
                .then()
                .statusCode(201)
                .extract().response();

        String authorName = authResponse.jsonPath().getString("name");


        given()
                .contentType("application/json")
                .body(authResponse.jsonPath().getLong("id"))
                .when()
                .request("GET", "/author/" + authResponse.jsonPath().getLong("id"))
                .then()
                .statusCode(200)
                .body("name", equalTo(authorName));

        deleteAuthorExample(authResponse);

    }

    @Test
    @Transactional
    @Rollback
    public void createAuthorTest() {
        Response response = given()
                .contentType("application/json")
                .body("{ \"name\": \"Roberto\" }")
                .when()
                .request("POST", "/author")
                .then()
                .statusCode(201)
                .body("name", equalTo("Roberto"))
                .extract().response();

        deleteAuthorExample(response);
    }

    @Test
    @Transactional
    @Rollback
    public void updateAuthorTest() {
        Response response = RestAssured.given()
                .contentType("application/json")
                .body("{ \"name\": \"Roberto\" }")
                .when()
                .request("POST", "/author")
                .then()
                .statusCode(201)
                .body("name", equalTo("Roberto"))
                .extract().response();

        Integer authorId = response.jsonPath().getInt("id");

        given()
                .contentType(APPLICATION_JSON)
                .body("{ \"name\": \"T.Tvardo\" }")
                .when()
                .request("PATCH", "/author/" + authorId)
                .then()
                .statusCode(200)
                .body("name", equalTo("T.Tvardo"))
                .body("id", equalTo(authorId));

        deleteAuthorExample(response);
    }

    @Test
    @Transactional
    @Rollback
    public void deleteAuthorTest() {
        Response response = RestAssured.given()
                .contentType("application/json")
                .body("{ \"name\": \"Roberto\" }")
                .when()
                .request("POST", "/author/")
                .then()
                .statusCode(201)
                .body("name", equalTo("Roberto"))
                .extract().response();
        Long authorId = response.jsonPath().getLong("id");

        given()
                .contentType(APPLICATION_JSON)
                .body(authorId)
                .when()
                .request("DELETE", "/author/" + authorId)
                .then()
                .statusCode(204);
    }

    @Test
    @Transactional
    @Rollback
    public void createdAuthorFailedTest() {
        given()
                .contentType(APPLICATION_JSON)
                .body("{ \"name\": \"Ro\" }")
                .when()
                .request("POST", "/author")
                .then()
                .statusCode(400);
    }
    public void deleteAuthorExample(Response authResponse) {
        given()
                .request("DELETE", "/author/" + authResponse.jsonPath().getLong("id"))
                .then()
                .statusCode(204);}

}

