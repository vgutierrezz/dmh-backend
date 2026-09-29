import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestTransfers extends BaseIntegrationTest {

    private static final long NON_EXISTING_USER_ID = 999999999L;

    private static final String NON_EXISTING_CVU = "9999999999999999999999";

    private static final double TRANSFER_AMOUNT = 100.0;


    @DisplayName("CP-TRF-001 - Realizar transferencia válida")
    @Order(1)
    @Test
    void shouldCreateTransferSuccessfully() {

        TestUser originUser =
                createAndAuthenticateUser();

        TestUser destinationUser =
                createAndAuthenticateUser();

        createDeposit(
                originUser,
                "500.00",
                "Preparación de transferencia"
        );

        String destinationCvu =
                getAccountCvu(destinationUser);

        String request =
                createTransferRequest(
                        TRANSFER_AMOUNT,
                        destinationCvu
                );

        given()
                .header(
                        "Authorization",
                        bearerToken(originUser.token())
                )
                .contentType(ContentType.JSON)
                .pathParam(
                        "userId",
                        originUser.id()
                )
                .body(request)
                .when()
                .post(
                        "/api/accounts/user/{userId}/transfers"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(200);
    }


    @DisplayName("CP-TRF-002 - Transferir a cuenta inexistente")
    @Order(2)
    @Test
    void shouldReturn404WhenDestinationAccountDoesNotExist() {

        TestUser originUser =
                createAndAuthenticateUser();

        createDeposit(
                originUser,
                "200.00",
                "Preparación de transferencia"
        );

        String request =
                createTransferRequest(
                        TRANSFER_AMOUNT,
                        NON_EXISTING_CVU
                );

        given()
                .header(
                        "Authorization",
                        bearerToken(originUser.token())
                )
                .contentType(ContentType.JSON)
                .pathParam(
                        "userId",
                        originUser.id()
                )
                .body(request)
                .when()
                .post(
                        "/api/accounts/user/{userId}/transfers"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(404);
    }


    @DisplayName("CP-TRF-003 - Transferir sin fondos suficientes")
    @Order(3)
    @Test
    void shouldRejectTransferWhenFundsAreInsufficient() {

        TestUser originUser =
                createAndAuthenticateUser();

        TestUser destinationUser =
                createAndAuthenticateUser();

        String destinationCvu =
                getAccountCvu(destinationUser);

        double currentBalance =
                getBalance(originUser);

        double excessiveAmount =
                currentBalance + 1_000_000.0;

        String request =
                createTransferRequest(
                        excessiveAmount,
                        destinationCvu
                );

        given()
                .header(
                        "Authorization",
                        bearerToken(originUser.token())
                )
                .contentType(ContentType.JSON)
                .pathParam(
                        "userId",
                        originUser.id()
                )
                .body(request)
                .when()
                .post(
                        "/api/accounts/user/{userId}/transfers"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(410);
    }


    @DisplayName("CP-TRF-004 - Transferir monto negativo")
    @Order(4)
    @Test
    void shouldRejectNegativeTransferAmount() {

        TestUser originUser =
                createAndAuthenticateUser();

        TestUser destinationUser =
                createAndAuthenticateUser();

        createDeposit(
                originUser,
                "200.00",
                "Preparación de transferencia"
        );

        String destinationCvu =
                getAccountCvu(destinationUser);

        String request =
                createTransferRequest(
                        -100.0,
                        destinationCvu
                );

        given()
                .header(
                        "Authorization",
                        bearerToken(originUser.token())
                )
                .contentType(ContentType.JSON)
                .pathParam(
                        "userId",
                        originUser.id()
                )
                .body(request)
                .when()
                .post(
                        "/api/accounts/user/{userId}/transfers"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }


    @DisplayName("CP-TRF-005 - Transferir monto cero")
    @Order(5)
    @Test
    void shouldRejectZeroTransferAmount() {

        TestUser originUser =
                createAndAuthenticateUser();

        TestUser destinationUser =
                createAndAuthenticateUser();

        String destinationCvu =
                getAccountCvu(destinationUser);

        String request =
                createTransferRequest(
                        0.0,
                        destinationCvu
                );

        given()
                .header(
                        "Authorization",
                        bearerToken(originUser.token())
                )
                .contentType(ContentType.JSON)
                .pathParam(
                        "userId",
                        originUser.id()
                )
                .body(request)
                .when()
                .post(
                        "/api/accounts/user/{userId}/transfers"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }


    @DisplayName("CP-TRF-006 - Transferir sin token")
    @Order(6)
    @Test
    void shouldRejectTransferWithoutToken() {

        TestUser originUser =
                createAndAuthenticateUser();

        TestUser destinationUser =
                createAndAuthenticateUser();

        String destinationCvu =
                getAccountCvu(destinationUser);

        String request =
                createTransferRequest(
                        TRANSFER_AMOUNT,
                        destinationCvu
                );

        given()
                .contentType(ContentType.JSON)
                .pathParam(
                        "userId",
                        originUser.id()
                )
                .body(request)
                .when()
                .post(
                        "/api/accounts/user/{userId}/transfers"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(403);
    }


    @DisplayName("CP-TRF-007 - Verificar descuento de saldo origen")
    @Order(7)
    @Test
    void shouldDecreaseOriginAccountBalance() {

        TestUser originUser =
                createAndAuthenticateUser();

        TestUser destinationUser =
                createAndAuthenticateUser();

        createDeposit(
                originUser,
                "500.00",
                "Preparación de transferencia"
        );

        String destinationCvu =
                getAccountCvu(destinationUser);

        double initialOriginBalance =
                getBalance(originUser);

        String request =
                createTransferRequest(
                        TRANSFER_AMOUNT,
                        destinationCvu
                );

        given()
                .header(
                        "Authorization",
                        bearerToken(originUser.token())
                )
                .contentType(ContentType.JSON)
                .pathParam(
                        "userId",
                        originUser.id()
                )
                .body(request)
                .when()
                .post(
                        "/api/accounts/user/{userId}/transfers"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(200);

        double finalOriginBalance =
                getBalance(originUser);

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

        TestUser originUser =
                createAndAuthenticateUser();

        TestUser destinationUser =
                createAndAuthenticateUser();

        createDeposit(
                originUser,
                "500.00",
                "Preparación de transferencia"
        );

        String destinationCvu =
                getAccountCvu(destinationUser);

        double initialDestinationBalance =
                getBalance(destinationUser);

        String request =
                createTransferRequest(
                        TRANSFER_AMOUNT,
                        destinationCvu
                );

        given()
                .header(
                        "Authorization",
                        bearerToken(originUser.token())
                )
                .contentType(ContentType.JSON)
                .pathParam(
                        "userId",
                        originUser.id()
                )
                .body(request)
                .when()
                .post(
                        "/api/accounts/user/{userId}/transfers"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(200);

        double finalDestinationBalance =
                getBalance(destinationUser);

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

        TestUser originUser =
                createAndAuthenticateUser();

        TestUser destinationUser =
                createAndAuthenticateUser();

        createDeposit(
                originUser,
                "500.00",
                "Preparación de transferencia"
        );

        String destinationCvu =
                getAccountCvu(destinationUser);

        String request =
                createTransferRequest(
                        TRANSFER_AMOUNT,
                        destinationCvu
                );

        given()
                .header(
                        "Authorization",
                        bearerToken(originUser.token())
                )
                .contentType(ContentType.JSON)
                .pathParam(
                        "userId",
                        originUser.id()
                )
                .body(request)
                .when()
                .post(
                        "/api/accounts/user/{userId}/transfers"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(200);

        given()
                .header(
                        "Authorization",
                        bearerToken(originUser.token())
                )
                .pathParam(
                        "userId",
                        originUser.id()
                )
                .when()
                .get(
                        "/api/accounts/user/{userId}/activity"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("[0].type", equalTo("TRANSFER"))
                .body(
                        "[0].amount",
                        equalTo((float) TRANSFER_AMOUNT)
                )
                .body("[0].id", notNullValue());
    }


    @DisplayName("CP-TRF-010 - Transferencia con monto nulo")
    @Order(10)
    @Test
    void shouldRejectTransferWithoutAmount() {

        TestUser originUser =
                createAndAuthenticateUser();

        TestUser destinationUser =
                createAndAuthenticateUser();

        String destinationCvu =
                getAccountCvu(destinationUser);

        String request = """
                {
                  "destination": "%s",
                  "origin": "Cuenta origen",
                  "type": "TRANSFER"
                }
                """.formatted(destinationCvu);

        given()
                .header(
                        "Authorization",
                        bearerToken(originUser.token())
                )
                .contentType(ContentType.JSON)
                .pathParam(
                        "userId",
                        originUser.id()
                )
                .body(request)
                .when()
                .post(
                        "/api/accounts/user/{userId}/transfers"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }


    @DisplayName("CP-TRF-011 - Transferir desde cuenta inexistente")
    @Order(11)
    @Test
    void shouldReturn404WhenOriginAccountDoesNotExist() {

        /*
         * Se crea un usuario para obtener un token válido
         * y otro para disponer de una cuenta destino válida.
         */
        TestUser authenticatedUser =
                createAndAuthenticateUser();

        TestUser destinationUser =
                createAndAuthenticateUser();

        String destinationCvu =
                getAccountCvu(destinationUser);

        String request =
                createTransferRequest(
                        TRANSFER_AMOUNT,
                        destinationCvu
                );

        given()
                .header(
                        "Authorization",
                        bearerToken(authenticatedUser.token())
                )
                .contentType(ContentType.JSON)
                .pathParam(
                        "userId",
                        NON_EXISTING_USER_ID
                )
                .body(request)
                .when()
                .post(
                        "/api/accounts/user/{userId}/transfers"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(404);
    }

    // ============================================================
    // MÉTODOS AUXILIARES ESPECÍFICOS DE TRANSFERENCIAS
    // ============================================================

    private double getBalance(
            TestUser user
    ) {

        Number balance =
                given()
                        .header(
                                "Authorization",
                                bearerToken(user.token())
                        )
                        .pathParam(
                                "userId",
                                user.id()
                        )
                        .when()
                        .get(
                                "/api/accounts/user/{userId}"
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

    private Map<String, Object> getAccount(
            TestUser user
    ) {

        Map<String, Object> account =
                given()
                        .header(
                                "Authorization",
                                bearerToken(user.token())
                        )
                        .pathParam(
                                "userId",
                                user.id()
                        )
                        .when()
                        .get(
                                "/api/accounts/user/{userId}"
                        )
                        .then()
                        .log().ifValidationFails()
                        .statusCode(200)
                        .body("id", notNullValue())
                        .body("userId", notNullValue())
                        .body("balance", notNullValue())
                        .body("cvu", notNullValue())
                        .body("alias", notNullValue())
                        .extract()
                        .jsonPath()
                        .getMap("$");

        assertNotNull(
                account,
                "La cuenta no debe ser null"
        );

        assertEquals(
                user.id().toString(),
                account.get("userId").toString(),
                "La cuenta debe pertenecer al usuario indicado"
        );

        return account;
    }

    private String getAccountCvu(
            TestUser user
    ) {

        Map<String, Object> account =
                getAccount(user);

        Object cvu = account.get("cvu");

        assertNotNull(
                cvu,
                "La cuenta debe tener un CVU"
        );

        String cvuValue =
                cvu.toString();

        assertEquals(
                22,
                cvuValue.length(),
                "El CVU debe tener 22 dígitos"
        );

        return cvuValue;
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
