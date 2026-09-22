import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestCard {
    @DisplayName("CP-CARD-001 - Consultar tarjetas asociadas")
    @Test
    void shouldGetCardsByUserIdSuccessfully() {

        String token = loginAndGetToken();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/accounts/user/11/cards")
                .then()
                .statusCode(200);
    }

    @DisplayName("CP-CARD-002 - Usuario sin tarjetas")
    @Test
    void shouldReturnEmptyListWhenUserHasNoCards() {

        String token = loginAndGetToken();

        Long userId = 36L; // ejemplo

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/accounts/user/" + userId + "/cards")
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
    }

    @DisplayName("CP-CARD-003 - Obtener tarjetas existentes")
    @Test
    void shouldReturnCardList() {

        String token = loginAndGetToken();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/accounts/user/11/cards")
                .then()
                .statusCode(200)
                .body("$", notNullValue());
    }

    @DisplayName("CP-CARD-004 - Usuario inexistente")
    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() {

        String token = loginAndGetToken();

        given()
                .header("Authorization", "Bearer " + token)
                .log().all()
                .when()
                .get("/api/accounts/user/999999/cards")
                .then()
                .log().all()
                .statusCode(404);
    }

    @DisplayName("CP-CARD-005 - Registrar tarjeta correctamente")
    @Test
    void shouldCreateCardSuccessfully() {

        String token = loginAndGetToken();

        String cardNumber =
                "411111" + String.valueOf(System.nanoTime()).substring(0, 10);

        String cardRequest = createCardRequest(cardNumber);

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(cardRequest)
                .log().all()
                .when()
                .post("/api/accounts/user/" + USER_ID + "/cards")
                .then()
                .log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .body("number", equalTo(cardNumber))
                .body("name", equalTo("Valentina Gutierrez"))
                .body("type", notNullValue());
    }

    @DisplayName("CP-CARD-006 - Tarjeta duplicada")
    @Test
    void shouldRejectAlreadyAssociatedCard() {

        String token = loginAndGetToken();
        String cardNumber = generateUniqueCardNumber();
        String cardRequest = createCardRequest(cardNumber);

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(cardRequest)
                .when()
                .post("/api/accounts/user/" + USER_ID + "/cards")
                .then()
                .statusCode(201);

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(cardRequest)
                .log().all()
                .when()
                .post("/api/accounts/user/" + USER_ID + "/cards")
                .then()
                .log().all()
                .statusCode(409);
    }

    @DisplayName("CP-CARD-007 - Campos obligatorios vacíos")
    @Test
    void shouldRejectCardWithMissingFields() {

        String token = loginAndGetToken();

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .post("/api/accounts/user/11/cards")
                .then()
                .statusCode(400);
    }

    @DisplayName("CP-CARD-008 - Eliminar tarjeta correctamente")
    @Test
    void shouldDeleteCardSuccessfully() {

        String token = loginAndGetToken();
        String cardNumber = generateUniqueCardNumber();
        String cardRequest = createCardRequest(cardNumber);

        String cardId =
                given()
                        .header("Authorization", "Bearer " + token)
                        .contentType(ContentType.JSON)
                        .body(cardRequest)
                        .when()
                        .post("/api/accounts/user/" + USER_ID + "/cards")
                        .then()
                        .statusCode(201)
                        .extract()
                        .path("id");

        assertNotNull(cardId);

        given()
                .header("Authorization", "Bearer " + token)
                .log().all()
                .when()
                .delete(
                        "/api/accounts/user/"
                                + USER_ID
                                + "/cards/"
                                + cardId
                )
                .then()
                .log().all()
                .statusCode(204);
    }

    @DisplayName("CP-CARD-009 - Eliminar tarjeta inexistente")
    @Test
    void shouldReturnNotFoundWhenCardDoesNotExist() {

        String token = loginAndGetToken();

        given()
                .header("Authorization", "Bearer " + token)
                .log().all()
                .when()
                .delete(
                        "/api/accounts/user/"
                                + USER_ID
                                + "/cards/999999"
                )
                .then()
                .log().all()
                .statusCode(404);
    }

    private static final String BASE_URI = "http://localhost:8080";
    private static final Long USER_ID = 11L;

    private String loginAndGetToken() {

        RestAssured.baseURI = BASE_URI;

        return given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                      "email": "valentina@test.com",
                      "password": "Valen1234"
                    }
                    """)
                .log().ifValidationFails()
                .when()
                .post("/api/auth/login")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("token", notNullValue())
                .extract()
                .path("token");
    }

    private String createCardRequest(String cardNumber) {
        return """
            {
              "number": "%s",
              "name": "Valentina Gutierrez",
              "expiration": "12/30",
              "cvc": "123"
            }
            """.formatted(cardNumber);
    }

    private String generateUniqueCardNumber() {

        String timestamp = String.valueOf(System.currentTimeMillis());

        return "4111" +
                timestamp.substring(
                        timestamp.length() - 12
                );
    }
}
