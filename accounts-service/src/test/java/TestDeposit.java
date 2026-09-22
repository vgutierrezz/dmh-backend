import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestDeposit {

    private static final String BASE_URI =
            "http://localhost:8080";

    private static final String EMAIL =
            "valentina@test.com";

    private static final String PASSWORD =
            "Valen1234";

    private static final Long USER_ID = 11L;

    private static final Long NON_EXISTING_USER_ID =
            999999L;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = BASE_URI;
    }

    @DisplayName("CP-DEP-001 - Depositar dinero correctamente")
    @Order(1)
    @Test
    void shouldDepositMoneySuccessfully() {

        String token = loginAndGetToken();

        String request = """
                {
                    "amount":100,
                    "type":"Deposit",
                    "description":"Depósito con tarjeta"
                }
                """;

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .log().ifValidationFails()
                .when()
                .post(
                        "/api/accounts/user/"
                                + USER_ID
                                + "/deposit"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(201)
                .body("id", notNullValue())
                .body("amount", equalTo(100.0f))
                .body("type", equalTo("DEPOSIT"));
    }

    @DisplayName("CP-DEP-002 - Verificar incremento de saldo")
    @Order(2)
    @Test
    void shouldIncreaseAccountBalance() {

        String token = loginAndGetToken();

        Number initialBalanceValue =
                given()
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .get("/api/accounts/user/" + USER_ID)
                        .then()
                        .statusCode(200)
                        .extract()
                        .path("balance");

        double initialBalance =
                initialBalanceValue.doubleValue();

        String request = """
                {
                    "amount":100,
                    "type":"Deposit",
                    "description":"Depósito con tarjeta"
                }
                """;

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(
                        "/api/accounts/user/"
                                + USER_ID
                                + "/deposit"
                )
                .then()
                .statusCode(201);

        Number finalBalanceValue =
                given()
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .get("/api/accounts/user/" + USER_ID)
                        .then()
                        .statusCode(200)
                        .extract()
                        .path("balance");

        double finalBalance =
                finalBalanceValue.doubleValue();

        Assertions.assertEquals(
                initialBalance + 100.0,
                finalBalance,
                0.01
        );
    }

    @DisplayName("CP-DEP-003 - Verificar actividad generada")
    @Order(3)
    @Test
    void shouldGenerateDepositActivity() {

        String token = loginAndGetToken();

        String request = """
                {
                    "amount":75,
                    "type":"Deposit",
                    "description":"Depósito con tarjeta"
                }
                """;

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(
                        "/api/accounts/user/"
                                + USER_ID
                                + "/deposit"
                )
                .then()
                .statusCode(201);

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get(
                        "/api/accounts/user/"
                                + USER_ID
                                + "/activity"
                )
                .then()
                .statusCode(200)
                .body("[0].type", equalTo("DEPOSIT"));
    }

    @DisplayName("CP-DEP-004 - Depositar monto cero")
    @Order(4)
    @Test
    void shouldRejectZeroAmount() {

        String token = loginAndGetToken();

        String request = """
                {
                    "amount":0,
                    "type":"Deposit",
                    "description":"Depósito con tarjeta"
                }
                """;

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(
                        "/api/accounts/user/"
                                + USER_ID
                                + "/deposit"
                )
                .then()
                .statusCode(400);
    }

    @DisplayName("CP-DEP-005 - Depositar monto negativo")
    @Order(5)
    @Test
    void shouldRejectNegativeAmount() {

        String token = loginAndGetToken();

        String request = """
                {
                    "amount":-100,
                    "type":"Deposit",
                    "description":"Depósito con tarjeta"
                }
                """;

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(
                        "/api/accounts/user/"
                                + USER_ID
                                + "/deposit"
                )
                .then()
                .statusCode(400);
    }

    @DisplayName("CP-DEP-006 - Depositar en cuenta inexistente")
    @Order(6)
    @Test
    void shouldReturn404WhenAccountDoesNotExist() {

        String token = loginAndGetToken();

        String request = """
                {
                    "amount":100,
                    "type":"Deposit",
                    "description":"Depósito con tarjeta"
                }
                """;

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(
                        "/api/accounts/user/"
                                + NON_EXISTING_USER_ID
                                + "/deposit"
                )
                .then()
                .statusCode(404);
    }

    @DisplayName("CP-DEP-007 - Depositar sin token")
    @Order(7)
    @Test
    void shouldRejectDepositWithoutToken() {

        String request = """
                {
                    "amount":100,
                    "type":"Deposit",
                    "description":"Depósito con tarjeta"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(
                        "/api/accounts/user/"
                                + USER_ID
                                + "/deposit"
                )
                .then()
                .statusCode(anyOf(
                        is(401),
                        is(403)
                ));
    }

    @DisplayName("CP-DEP-008 - Campos obligatorios vacíos")
    @Order(8)
    @Test
    void shouldRejectEmptyRequest() {

        String token = loginAndGetToken();

        String request = """
                {}
                """;

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(
                        "/api/accounts/user/"
                                + USER_ID
                                + "/deposit"
                )
                .then()
                .statusCode(400);
    }

    private String loginAndGetToken() {

        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "email":"%s",
                          "password":"%s"
                        }
                        """.formatted(
                        EMAIL,
                        PASSWORD
                ))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .extract()
                .path("token");
    }
}