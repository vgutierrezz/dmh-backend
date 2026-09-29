import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestCard extends BaseIntegrationTest {

    private static final String BASE_URI = "http://localhost:8080";

    /*
     * Se utiliza para generar correos y números de tarjeta únicos,
     * incluso cuando las pruebas se ejecutan rápidamente.
     */

    @BeforeAll
    static void configureRestAssured() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @DisplayName("CP-CARD-001 - Consultar tarjetas de una cuenta sin tarjetas")
    @Order(1)
    @Test
    void shouldReturnEmptyListWhenAccountHasNoCards() {

        TestUser user = createAndAuthenticateUser();

        given()
                .header("Authorization", bearerToken(user.token()))
                .pathParam("userId", user.id())
                .when()
                .get("/api/accounts/user/{userId}/cards")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasSize(0));
    }

    @DisplayName("CP-CARD-002 - Registrar tarjeta correctamente")
    @Order(2)
    @Test
    void shouldCreateCardSuccessfully() {

        TestUser user = createAndAuthenticateUser();
        String cardNumber = generateUniqueCardNumber();

        given()
                .header("Authorization", bearerToken(user.token()))
                .contentType(ContentType.JSON)
                .pathParam("userId", user.id())
                .body(
                        createCardRequest(
                                cardNumber,
                                user.fullName()
                        )
                )
                .when()
                .post("/api/accounts/user/{userId}/cards")
                .then()
                .log().ifValidationFails()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("id", notNullValue())
                .body("number", equalTo(cardNumber))
                .body("name", equalTo(user.fullName()))
                .body("type", notNullValue());
    }

    @DisplayName("CP-CARD-003 - Consultar tarjetas asociadas")
    @Order(3)
    @Test
    void shouldReturnAssociatedCards() {

        TestUser user = createAndAuthenticateUser();
        String cardNumber = generateUniqueCardNumber();

        createCard(user, cardNumber);

        given()
                .header("Authorization", bearerToken(user.token()))
                .pathParam("userId", user.id())
                .when()
                .get("/api/accounts/user/{userId}/cards")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasSize(1))
                .body("[0].id", notNullValue())
                .body("[0].number", equalTo(cardNumber))
                .body("[0].name", equalTo(user.fullName()))
                .body("[0].type", notNullValue());
    }


    @DisplayName("CP-CARD-004 - Consultar tarjetas de usuario inexistente")
    @Order(4)
    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() {

        TestUser authenticatedUser = createAndAuthenticateUser();
        long nonexistentUserId = 999999999L;

        given()
                .header(
                        "Authorization",
                        bearerToken(authenticatedUser.token())
                )
                .pathParam("userId", nonexistentUserId)
                .when()
                .get("/api/accounts/user/{userId}/cards")
                .then()
                .log().ifValidationFails()
                .statusCode(404);
    }


    @DisplayName("CP-CARD-005 - Rechazar tarjeta duplicada")
    @Order(5)
    @Test
    void shouldRejectAlreadyAssociatedCard() {

        TestUser user = createAndAuthenticateUser();
        String cardNumber = generateUniqueCardNumber();

        String requestBody = createCardRequest(
                cardNumber,
                user.fullName()
        );

        given()
                .header("Authorization", bearerToken(user.token()))
                .contentType(ContentType.JSON)
                .pathParam("userId", user.id())
                .body(requestBody)
                .when()
                .post("/api/accounts/user/{userId}/cards")
                .then()
                .log().ifValidationFails()
                .statusCode(201);

        given()
                .header("Authorization", bearerToken(user.token()))
                .contentType(ContentType.JSON)
                .pathParam("userId", user.id())
                .body(requestBody)
                .when()
                .post("/api/accounts/user/{userId}/cards")
                .then()
                .log().ifValidationFails()
                .statusCode(409);
    }

    @DisplayName("CP-CARD-006 - Rechazar tarjeta con campos obligatorios vacíos")
    @Order(6)
    @Test
    void shouldRejectCardWithMissingRequiredFields() {

        TestUser user = createAndAuthenticateUser();

        given()
                .header("Authorization", bearerToken(user.token()))
                .contentType(ContentType.JSON)
                .pathParam("userId", user.id())
                .body("{}")
                .when()
                .post("/api/accounts/user/{userId}/cards")
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }


    @DisplayName("CP-CARD-007 - Rechazar tarjeta para usuario inexistente")
    @Order(7)
    @Test
    void shouldReturnNotFoundWhenCreatingCardForNonexistentUser() {

        TestUser authenticatedUser = createAndAuthenticateUser();
        long nonexistentUserId = 999999999L;

        given()
                .header(
                        "Authorization",
                        bearerToken(authenticatedUser.token())
                )
                .contentType(ContentType.JSON)
                .pathParam("userId", nonexistentUserId)
                .body(
                        createCardRequest(
                                generateUniqueCardNumber(),
                                authenticatedUser.fullName()
                        )
                )
                .when()
                .post("/api/accounts/user/{userId}/cards")
                .then()
                .log().ifValidationFails()
                .statusCode(404);
    }


    @DisplayName("CP-CARD-008 - Eliminar tarjeta correctamente")
    @Order(8)
    @Test
    void shouldDeleteCardSuccessfully() {

        TestUser user = createAndAuthenticateUser();
        Long cardId = createCard(
                user,
                generateUniqueCardNumber()
        );

        assertNotNull(cardId);

        given()
                .header("Authorization", bearerToken(user.token()))
                .pathParam("userId", user.id())
                .pathParam("cardId", cardId)
                .when()
                .delete(
                        "/api/accounts/user/{userId}/cards/{cardId}"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(200);

        /*
         * La guía exige 200 para una eliminación correcta.
         * Esta segunda petición comprueba además que la tarjeta
         * efectivamente dejó de estar asociada.
         */
        given()
                .header("Authorization", bearerToken(user.token()))
                .pathParam("userId", user.id())
                .when()
                .get("/api/accounts/user/{userId}/cards")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("$", hasSize(0));
    }

    @DisplayName("CP-CARD-009 - Eliminar tarjeta inexistente")
    @Order(9)
    @Test
    void shouldReturnNotFoundWhenCardDoesNotExist() {

        TestUser user = createAndAuthenticateUser();
        long nonexistentCardId = 999999999L;

        given()
                .header("Authorization", bearerToken(user.token()))
                .pathParam("userId", user.id())
                .pathParam("cardId", nonexistentCardId)
                .when()
                .delete(
                        "/api/accounts/user/{userId}/cards/{cardId}"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(404);
    }


    @DisplayName("CP-CARD-010 - Consultar tarjetas sin token")
    @Order(10)
    @Test
    void shouldRejectCardQueryWithoutToken() {

        TestUser user = createAndAuthenticateUser();

        given()
                .pathParam("userId", user.id())
                .when()
                .get("/api/accounts/user/{userId}/cards")
                .then()
                .log().ifValidationFails()
                .statusCode(403);
    }


    @DisplayName("CP-CARD-011 - Registrar tarjeta sin token")
    @Order(11)
    @Test
    void shouldRejectCardCreationWithoutToken() {

        TestUser user = createAndAuthenticateUser();

        given()
                .contentType(ContentType.JSON)
                .pathParam("userId", user.id())
                .body(
                        createCardRequest(
                                generateUniqueCardNumber(),
                                user.fullName()
                        )
                )
                .when()
                .post("/api/accounts/user/{userId}/cards")
                .then()
                .log().ifValidationFails()
                .statusCode(403);
    }


    @DisplayName("CP-CARD-012 - Eliminar tarjeta sin token")
    @Order(12)
    @Test
    void shouldRejectCardDeletionWithoutToken() {

        TestUser user = createAndAuthenticateUser();

        Long cardId = createCard(
                user,
                generateUniqueCardNumber()
        );

        given()
                .pathParam("userId", user.id())
                .pathParam("cardId", cardId)
                .when()
                .delete(
                        "/api/accounts/user/{userId}/cards/{cardId}"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(403);
    }
}
