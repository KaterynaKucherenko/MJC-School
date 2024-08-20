package com.mjc.school.controller;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.fail;


@SpringBootTest
public class NewsControllerTest {
    String newsExample = "{ \"authorName\": \"Barbara\", \"content\": \"The Populist Wave and Its Discontents\", \"tagNames\": [ \"military\", \"sensory\", \"guard\" ], \"title\": \"The Integrity\" }";
    String newsExampleWithoutAuthor = "{ \"authorName\": \"\", \"content\": \"The Populist Wave and Its Discontents\", \"tagNames\": [ \"military\", \"sensory\", \"guard\" ], \"title\": \"The Integrity\" }";

    @BeforeEach
    public void setup() {
        RestAssured.baseURI = "http://localhost:8082/api/v1";
        RestAssured.port = 8082;
    }


    @Test
    public void readAllNewsTest() {
        given()
                .contentType("application/json")
                .param("page", 0)
                .param("size", 5)
                .param("sortBy", "createDate,desc")
                .when()
                .request("GET", "/news")
                .then()
                .statusCode(200);
    }

    @Test
    @Transactional
    @Rollback
    public void readNewsByIdTest() {
        Response response = RestAssured.given()
                .contentType("application/json")
                .body(newsExample)
                .when()
                .request("POST", "/news")
                .then()
                .statusCode(201).extract().response();
        given()
                .contentType("application/json")
                .body(response.jsonPath().getLong("id"))
                .when()
                .request("GET", "/news/" + response.jsonPath().getLong("id"))
                .then()
                .statusCode(200);
        deleteTmpInfo(response);

    }

    @Test
    @Transactional
    @Rollback
    public void createNewsTest() {
        Response response = RestAssured.given()
                .contentType("application/json")
                .body(newsExample)
                .when()
                .request("POST", "/news")
                .then()
                .statusCode(201).extract().response();
        deleteTmpInfo(response);


    }

    @Test
    @Transactional
    @Rollback
    public void updateNewsTest() {
        Response response = RestAssured.given()
                .contentType("application/json")
                .body(newsExample)
                .when()
                .request("POST", "/news")
                .then()
                .statusCode(201).extract().response();
        Response updResp = RestAssured.given()
                .contentType("application/json")
                .body("{ \"authorName\": \"Vinsent\", \"content\": \"The Populist Wave and Its Discontents\", \"title\": \"The documentalist\"}")
                .when()
                .request("PATCH", "/news/" + response.jsonPath().getLong("id"))
                .then()
                .statusCode(200)
                .body("title", equalTo("The documentalist")).extract().response();
        deleteTmpInfo(response);
        given().contentType("application/json").request("DELETE", "/author/" + updResp.jsonPath().getLong("authorDtoResponse.id"));
    }

    @Test
    @Transactional
    @Rollback
    public void deleteNewsTest() {
        Response response = RestAssured.given()
                .contentType("application/json")
                .body(newsExample)
                .when()
                .request("POST", "/news")
                .then()
                .statusCode(201).extract().response();
        given()
                .contentType("application/json")
                .when()
                .request("DELETE", "/news/" + response.jsonPath().getLong("id"))
                .then()
                .statusCode(204);
    }

    @Test
    @Transactional
    @Rollback
    public void createdNewsFailedTest() {
        given()
                .contentType("application/json")
                .body("{ \"title\": \"Ro\" }")
                .when()
                .request("POST", "/news")
                .then()
                .statusCode(400);
    }

    @Test
    @Transactional
    @Rollback
    public void getAuthorByNewsIdTest() {
        Response response = RestAssured.given()
                .contentType("application/json")
                .body(newsExample)
                .when()
                .request("POST", "/news")
                .then()
                .statusCode(201).extract().response();

        Integer newsId = response.jsonPath().getInt("id");
        given()
                .contentType("application/json")
                .body(newsId)
                .when()
                .request("GET", "/news/" + newsId + "/author")
                .then()
                .statusCode(200)
                .body("name", equalTo(response.jsonPath().getString("authorDtoResponse.name")));
        deleteTmpInfo(response);
    }

    @Test
    @Transactional
    @Rollback
    public void getCommentByNewsIdTest() {
        Response newsResp = RestAssured.given()
                .contentType("application/json")
                .body(newsExample)
                .when()
                .request("POST", "/news")
                .then()
                .statusCode(201).extract().response();
        Long newsId = newsResp.jsonPath().getLong("id");

        RestAssured.given()
                .contentType("application/json")
                .body("{ \"content\": \"Interesting\", \"newsId\":" + newsId + "}")
                .when()
                .request("POST", "/comment")
                .then()
                .statusCode(201);

        Response contentFromNews = RestAssured.given()
                .contentType("application/json")
                .body(newsId)
                .when()
                .request("GET", "news/" + newsId)
                .then()
                .statusCode(200).extract().response();

        List<String> comment = contentFromNews.jsonPath().getList("commentList.content", String.class);

        given()
                .contentType("application/json")
                .body(newsId)
                .when()
                .request("GET", "/news/" + newsId + "/comment")
                .then()
                .statusCode(200)
                .body("content", equalTo(comment));

        deleteTmpInfo(newsResp);
    }


    @Test
    @Transactional
    @Rollback
    public void getTagByNewsIdTest() {
        Response newsResp = RestAssured.given()
                .contentType("application/json")
                .body(newsExample)
                .when()
                .request("POST", "/news")
                .then()
                .statusCode(201).extract().response();
        Long newsId = newsResp.jsonPath().getLong("id");
        List<String> tagNamesList = newsResp.jsonPath().getList("tagList.name", String.class);


        given()
                .contentType("application/json")
                .when()
                .request("GET", "/news/" + newsId + "/tag")
                .then()
                .statusCode(200)
                .body("name", equalTo(tagNamesList));

        deleteTmpInfo(newsResp);
    }

    @Test
    public void readListOfNewsByParamsTest() {
        Response newsResp = RestAssured.given()
                .contentType("application/json")
                .body(newsExample)
                .when()
                .request("POST", "/news")
                .then()
                .statusCode(201).extract().response();

        String titleForNews = newsResp.jsonPath().getString("title");
        String authorNameForNews = newsResp.jsonPath().getString("authorName");
        String contentForNews = newsResp.jsonPath().getString("content");

        List<Integer> idOfTagForCompare = newsResp.jsonPath().getList("tagList.id", Integer.class);
        Integer firstIdOfTagForCompare = idOfTagForCompare.get(0);

        String tagNameForNews = newsResp.jsonPath().getString("tagName");

        Integer newsId = newsResp.jsonPath().getInt("id");
        List<Integer> idForCompare = new ArrayList<>();
        idForCompare.add(newsId);

        given()
                .contentType("application/json")
                .body("{\"title\":" + titleForNews + "}")
                .when()
                .get("/news/search")
                .then()
                .statusCode(200)
                .body("id", equalTo(idForCompare));

        given()
                .contentType("application/json")
                .body("{\"authorName\":" + authorNameForNews + "}")
                .when()
                .get("/news/search")
                .then()
                .statusCode(200)
                .body("id", equalTo(idForCompare));

        given()
                .contentType("application/json")
                .body("{\"content\":" + contentForNews + "}")
                .when()
                .get("/news/search")
                .then()
                .statusCode(200)
                .body("id", equalTo(idForCompare));
        given()
                .contentType("application/json")
                .body("{\"tagId\":" + firstIdOfTagForCompare + "}")
                .when()
                .get("/news/search")
                .then()
                .statusCode(200)
                .body("id", equalTo(idForCompare));
        given()
                .contentType("application/json")
                .body("{\"tagName\":" + tagNameForNews + "}")
                .when()
                .get("/news/search")
                .then()
                .statusCode(200)
                .body("id", equalTo(idForCompare));

        deleteTmpInfo(newsResp);

    }
    @Test
    @Transactional
    @Rollback
    public void createNewsTestWithoutAuthor() {
        given()
                .contentType("application/json")
                .body(newsExampleWithoutAuthor)
                .when()
                .request("POST", "/news")
                .then()
                .statusCode(400).extract().response();

    }



    public Response createNewsExample() {
        return RestAssured.given()
                .contentType("application/json")
                .body(newsExample)
                .when()
                .request("POST", "/news")
                .then()
                .statusCode(201).extract().response();

    }

    public void deleteTmpInfo(Response response) {
        given()
                .request("DELETE", "/news/" + response.jsonPath().getLong("id"))
                .then().statusCode(204);
        List<Integer> tagIds = response.jsonPath().getList("tagList.id", Integer.class);
        tagIds.forEach(a -> given().contentType("application/json").
                delete("/tag/" + a).
                then().statusCode(204));
        given().contentType("application/json").
                delete("/author/" + response.jsonPath().
                        getLong("authorDtoResponse.id")).
                then().statusCode(204);
    }
}
