import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestTransfers {

    private static final String BASE_URI =
            "http://localhost:8080";

    private static final String EMAIL =
            "valentina@test.com";

    private static final String PASSWORD =
            "Valen1234";

    private static final Long ORIGIN_USER_ID = 11L;

    private static final Long NON_EXISTING_USER_ID = 999999L;

    private static final String NON_EXISTING_CVU =
            "9999999999999999999999";

    private static final double TRANSFER_AMOUNT = 100.0;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = BASE_URI;
    }

    @DisplayName("CP-TRF-001 - Realizar transferencia válida")
    @Order(1)
    @Test
    void shouldCreateTransferSuccessfully() {

        String token = loginAndGetToken();

        fundOriginAccount(token, 500.0);

        Map<String, Object> destinationAccount =
                getDestinationAccount(token);

        String destinationCvu =
                destinationAccount.get("cvu").toString();

        String request =
                createTransferRequest(
                        TRANSFER_AMOUNT,
                        destinationCvu
                );

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .log().ifValidationFails()
                .when()
                .post(
                        "/api/accounts/user/"
                                + ORIGIN_USER_ID
                                + "/transfers"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(200);
    }

    @DisplayName("CP-TRF-002 - Transferir a cuenta inexistente")
    @Order(2)
    @Test
    void shouldReturn404WhenDestinationAccountDoesNotExist() {

        String token = loginAndGetToken();

        fundOriginAccount(token, 200.0);

        String request =
                createTransferRequest(
                        TRANSFER_AMOUNT,
                        NON_EXISTING_CVU
                );

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .log().ifValidationFails()
                .when()
                .post(
                        "/api/accounts/user/"
                                + ORIGIN_USER_ID
                                + "/transfers"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(404);
    }

    @DisplayName("CP-TRF-003 - Transferir sin fondos suficientes")
    @Order(3)
    @Test
    void shouldRejectTransferWhenFundsAreInsufficient() {

        String token = loginAndGetToken();

        Map<String, Object> destinationAccount =
                getDestinationAccount(token);

        String destinationCvu =
                destinationAccount.get("cvu").toString();

        double currentBalance =
                getBalance(token, ORIGIN_USER_ID);

        double excessiveAmount =
                currentBalance + 1_000_000.0;

        String request =
                createTransferRequest(
                        excessiveAmount,
                        destinationCvu
                );

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .log().ifValidationFails()
                .when()
                .post(
                        "/api/accounts/user/"
                                + ORIGIN_USER_ID
                                + "/transfers"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(anyOf(
                        is(400),
                        is(410)
                ));
    }

    @DisplayName("CP-TRF-004 - Transferir monto negativo")
    @Order(4)
    @Test
    void shouldRejectNegativeTransferAmount() {

        String token = loginAndGetToken();

        fundOriginAccount(token, 200.0);

        Map<String, Object> destinationAccount =
                getDestinationAccount(token);

        String destinationCvu =
                destinationAccount.get("cvu").toString();

        String request =
                createTransferRequest(
                        -100.0,
                        destinationCvu
                );

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .log().ifValidationFails()
                .when()
                .post(
                        "/api/accounts/user/"
                                + ORIGIN_USER_ID
                                + "/transfers"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

    @DisplayName("CP-TRF-005 - Transferir monto cero")
    @Order(5)
    @Test
    void shouldRejectZeroTransferAmount() {

        String token = loginAndGetToken();

        Map<String, Object> destinationAccount =
                getDestinationAccount(token);

        String destinationCvu =
                destinationAccount.get("cvu").toString();

        String request =
                createTransferRequest(
                        0.0,
                        destinationCvu
                );

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .log().ifValidationFails()
                .when()
                .post(
                        "/api/accounts/user/"
                                + ORIGIN_USER_ID
                                + "/transfers"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

    @DisplayName("CP-TRF-006 - Transferir sin token")
    @Order(6)
    @Test
    void shouldRejectTransferWithoutToken() {

        String preparationToken = loginAndGetToken();

        Map<String, Object> destinationAccount =
                getDestinationAccount(preparationToken);

        String destinationCvu =
                destinationAccount.get("cvu").toString();

        String request =
                createTransferRequest(
                        TRANSFER_AMOUNT,
                        destinationCvu
                );

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .log().ifValidationFails()
                .when()
                .post(
                        "/api/accounts/user/"
                                + ORIGIN_USER_ID
                                + "/transfers"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(anyOf(
                        is(401),
                        is(403)
                ));
    }

    @DisplayName("CP-TRF-007 - Verificar descuento de saldo origen")
    @Order(7)
    @Test
    void shouldDecreaseOriginAccountBalance() {

        String token = loginAndGetToken();

        fundOriginAccount(token, 500.0);

        Map<String, Object> destinationAccount =
                getDestinationAccount(token);

        String destinationCvu =
                destinationAccount.get("cvu").toString();

        double initialOriginBalance =
                getBalance(token, ORIGIN_USER_ID);

        String request =
                createTransferRequest(
                        TRANSFER_AMOUNT,
                        destinationCvu
                );

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(
                        "/api/accounts/user/"
                                + ORIGIN_USER_ID
                                + "/transfers"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(200);

        double finalOriginBalance =
                getBalance(token, ORIGIN_USER_ID);

        assertEquals(
                initialOriginBalance - TRANSFER_AMOUNT,
                finalOriginBalance,
                0.01,
                "El saldo de origen debe disminuir exactamente "
                        + "por el monto transferido"
        );
    }

    @DisplayName("CP-TRF-008 - Verificar acreditación de saldo destino")
    @Order(8)
    @Test
    void shouldIncreaseDestinationAccountBalance() {

        String token = loginAndGetToken();

        fundOriginAccount(token, 500.0);

        Map<String, Object> destinationAccount =
                getDestinationAccount(token);

        Long destinationUserId =
                Long.valueOf(
                        destinationAccount
                                .get("userId")
                                .toString()
                );

        String destinationCvu =
                destinationAccount.get("cvu").toString();

        double initialDestinationBalance =
                getBalance(token, destinationUserId);

        String request =
                createTransferRequest(
                        TRANSFER_AMOUNT,
                        destinationCvu
                );

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(
                        "/api/accounts/user/"
                                + ORIGIN_USER_ID
                                + "/transfers"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(200);

        double finalDestinationBalance =
                getBalance(token, destinationUserId);

        assertEquals(
                initialDestinationBalance + TRANSFER_AMOUNT,
                finalDestinationBalance,
                0.01,
                "El saldo de destino debe aumentar exactamente "
                        + "por el monto transferido"
        );
    }

    @DisplayName("CP-TRF-009 - Verificar actividad generada por transferencia")
    @Order(9)
    @Test
    void shouldGenerateTransferActivity() {

        String token = loginAndGetToken();

        fundOriginAccount(token, 500.0);

        Map<String, Object> destinationAccount =
                getDestinationAccount(token);

        String destinationCvu =
                destinationAccount.get("cvu").toString();

        String request =
                createTransferRequest(
                        TRANSFER_AMOUNT,
                        destinationCvu
                );

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(
                        "/api/accounts/user/"
                                + ORIGIN_USER_ID
                                + "/transfers"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(200);

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get(
                        "/api/accounts/user/"
                                + ORIGIN_USER_ID
                                + "/activity"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("[0].type", equalTo("TRANSFER"))
                .body("[0].amount", notNullValue());
    }

    @DisplayName("CP-TRF-010 - Transferencia con monto nulo")
    @Order(10)
    @Test
    void shouldRejectTransferWithoutAmount() {

        String token = loginAndGetToken();

        Map<String, Object> destinationAccount =
                getDestinationAccount(token);

        String destinationCvu =
                destinationAccount.get("cvu").toString();

        String request = """
                {
                    "destination": "%s",
                    "origin": "Cuenta origen",
                    "type": "TRANSFER"
                }
                """.formatted(destinationCvu);

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .log().ifValidationFails()
                .when()
                .post(
                        "/api/accounts/user/"
                                + ORIGIN_USER_ID
                                + "/transfers"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

    @DisplayName("CP-TRF-011 - Transferir desde cuenta inexistente")
    @Order(11)
    @Test
    void shouldReturn404WhenOriginAccountDoesNotExist() {

        String token = loginAndGetToken();

        Map<String, Object> destinationAccount =
                getDestinationAccount(token);

        String destinationCvu =
                destinationAccount.get("cvu").toString();

        String request =
                createTransferRequest(
                        TRANSFER_AMOUNT,
                        destinationCvu
                );

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .log().ifValidationFails()
                .when()
                .post(
                        "/api/accounts/user/"
                                + NON_EXISTING_USER_ID
                                + "/transfers"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(404);
    }

    private String loginAndGetToken() {

        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "email": "%s",
                          "password": "%s"
                        }
                        """.formatted(
                        EMAIL,
                        PASSWORD
                ))
                .when()
                .post("/api/auth/login")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("token", notNullValue())
                .extract()
                .path("token");
    }

    private void fundOriginAccount(
            String token,
            double amount
    ) {

        String request = """
                {
                    "amount": %s,
                    "type": "Deposit",
                    "description": "Preparación de transferencia"
                }
                """.formatted(amount);

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(
                        "/api/accounts/user/"
                                + ORIGIN_USER_ID
                                + "/deposit"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(201);
    }

    private double getBalance(
            String token,
            Long userId
    ) {

        Number balance =
                given()
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .get(
                                "/api/accounts/user/"
                                        + userId
                        )
                        .then()
                        .log().ifValidationFails()
                        .statusCode(200)
                        .extract()
                        .path("balance");

        assertNotNull(
                balance,
                "El saldo no debe ser null"
        );

        return balance.doubleValue();
    }

    private Map<String, Object> getDestinationAccount(
            String token
    ) {

        List<Map<String, Object>> accounts =
                given()
                        .header("Authorization", "Bearer " + token)
                        .when()
                        .get("/api/accounts")
                        .then()
                        .log().ifValidationFails()
                        .statusCode(200)
                        .extract()
                        .jsonPath()
                        .getList("$");

        assertNotNull(
                accounts,
                "La lista de cuentas no debe ser null"
        );

        Map<String, Object> destinationAccount =
                accounts.stream()
                        .filter(account ->
                                !account.get("userId")
                                        .toString()
                                        .equals(
                                                ORIGIN_USER_ID.toString()
                                        )
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new AssertionError(
                                        "Debe existir una cuenta destino "
                                                + "diferente de la cuenta origen"
                                )
                        );

        assertNotNull(
                destinationAccount.get("cvu"),
                "La cuenta destino debe tener CVU"
        );

        assertNotEquals(
                ORIGIN_USER_ID.toString(),
                destinationAccount.get("userId").toString(),
                "La cuenta destino debe ser distinta "
                        + "de la cuenta origen"
        );

        return destinationAccount;
    }

    private String createTransferRequest(
            double amount,
            String destinationCvu
    ) {

        return """
                {
                    "amount": %s,
                    "destination": "%s",
                    "origin": "Cuenta origen",
                    "type": "TRANSFER"
                }
                """.formatted(
                amount,
                destinationCvu
        );
    }
}