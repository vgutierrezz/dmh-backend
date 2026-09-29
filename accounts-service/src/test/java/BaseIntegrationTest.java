import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.atomic.AtomicLong;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class BaseIntegrationTest {

    protected static final String BASE_URI = "http://localhost:8080";

    protected static final AtomicLong UNIQUE_SEQUENCE =
            new AtomicLong(
                    System.currentTimeMillis()
            );

    @BeforeEach
    void setupBase() {
        RestAssured.baseURI = BASE_URI;
    }

    /**
     * Registra un usuario nuevo y luego realiza el login.
     * El correo se genera dinámicamente, por lo que el método puede
     * ejecutarse varias veces sin provocar correos duplicados.
     */
    protected TestUser createAndAuthenticateUser() {

        long uniqueValue = UNIQUE_SEQUENCE.incrementAndGet();

        String firstName = "Valentina";
        String lastName = "Gutierrez";
        String email = "valentina.card." + uniqueValue + "@test.com";
        String password = "Valen1234";
        String dni = generateUniqueDni(uniqueValue);
        String phone = generateUniquePhone(uniqueValue);

        Long userId = registerUser(
                firstName,
                lastName,
                email,
                password,
                phone,
                dni
        );

        String token = loginAndGetToken(email, password);

        TestUser user = new TestUser(
                userId,
                firstName,
                lastName,
                email,
                password,
                token
        );

        waitUntilAccountIsCreated(user);

        return user;
    }

    /**
     * Registra un usuario y devuelve el ID generado.
     */
    protected Long registerUser(
            String firstName,
            String lastName,
            String email,
            String password,
            String phone,
            String dni
    ) {

        String requestBody = """
            {
              "firstName": "%s",
              "lastName": "%s",
              "email": "%s",
              "password": "%s",
              "phone": "%s",
              "dni": "%s"
            }
            """.formatted(
                firstName,
                lastName,
                email,
                password,
                phone,
                dni
        );

        Response response =
                given()
                        .contentType(ContentType.JSON)
                        .body(requestBody)
                        .when()
                        .post("/api/users/register")
                        .then()
                        .log().ifValidationFails()
                        .statusCode(anyOf(is(200), is(201)))
                        .body("id", notNullValue())
                        .extract()
                        .response();

        Long userId = response.jsonPath().getLong("id");

        assertNotNull(userId);

        return userId;
    }

    /**
     * Inicia sesión y devuelve el JWT.
     */
    protected String loginAndGetToken(
            String email,
            String password
    ) {

        String requestBody = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);

        String token =
                given()
                        .contentType(ContentType.JSON)
                        .body(requestBody)
                        .when()
                        .post("/api/auth/login")
                        .then()
                        .statusCode(200)
                        .body("token", notNullValue())
                        .extract()
                        .path("token");

        assertNotNull(token);

        return token;
    }
    /**
     * Registra una tarjeta y devuelve el ID generado.
     */
    protected Long createCard(
            TestUser user,
            String cardNumber
    ) {

        String cardId =
                given()
                        .header(
                                "Authorization",
                                bearerToken(user.token())
                        )
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
                        .body("id", notNullValue())
                        .body("number", equalTo(cardNumber))
                        .body("name", equalTo(user.fullName()))
                        .body("type", notNullValue())
                        .extract()
                        .path("id");

        assertNotNull(cardId);

        return Long.valueOf(cardId);
    }


    protected String createCardRequest(
            String cardNumber,
            String cardholderName
    ) {
        return """
                {
                  "number": "%s",
                  "name": "%s",
                  "expiration": "12/30",
                  "cvc": "123"
                }
                """.formatted(
                cardNumber,
                cardholderName
        );
    }

    /**
     * Genera un número distinto de 16 dígitos para cada prueba.
     */
    protected String generateUniqueCardNumber() {

        long uniqueValue = UNIQUE_SEQUENCE.incrementAndGet();

        String suffix = String.format(
                "%012d",
                uniqueValue % 1_000_000_000_000L
        );

        return "4111" + suffix;
    }

    protected String bearerToken(String token) {
        return "Bearer " + token;
    }

    /**
     * Contiene los datos creados dinámicamente para cada prueba.
     */
    protected record TestUser(
            Long id,
            String firstName,
            String lastName,
            String email,
            String password,
            String token
    ) {
        String fullName() {
            return firstName + " " + lastName;
        }
    }

    protected void waitUntilAccountIsCreated(TestUser user) {

        int maximumAttempts = 10;
        long delayInMilliseconds = 500;

        for (int attempt = 1; attempt <= maximumAttempts; attempt++) {

            Response response =
                    given()
                            .header(
                                    "Authorization",
                                    bearerToken(user.token())
                            )
                            .pathParam("userId", user.id())
                            .when()
                            .get("/api/accounts/user/{userId}/cards");

            /*
             * Una cuenta recién creada y sin tarjetas debe responder 200
             * con una lista vacía.
             */
            if (response.statusCode() == 200) {
                return;
            }

            if (attempt < maximumAttempts) {
                sleep(delayInMilliseconds);
            }
        }

        throw new AssertionError(
                "La cuenta del usuario " + user.id()
                        + " no fue creada después de "
                        + maximumAttempts + " intentos"
        );
    }

    protected void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "La espera para la creación de la cuenta fue interrumpida",
                    exception
            );
        }
    }

    protected String generateUniqueDni(long uniqueValue) {

        long normalizedValue =
                Math.abs(uniqueValue % 90_000_000L);

        return String.valueOf(10_000_000L + normalizedValue);
    }

    protected String generateUniquePhone(long uniqueValue) {

        long normalizedValue =
                Math.abs(uniqueValue % 100_000_000L);

        return "11" + String.format("%08d", normalizedValue);
    }

    protected Long createDeposit(
            TestUser user,
            String amount,
            String description
    ) {

        String requestBody = """
            {
              "amount": %s,
              "type": "Deposit",
              "description": "%s"
            }
            """.formatted(
                amount,
                description
        );

        Response response =
                given()
                        .header(
                                "Authorization",
                                bearerToken(user.token())
                        )
                        .contentType(ContentType.JSON)
                        .pathParam(
                                "userId",
                                user.id()
                        )
                        .body(requestBody)
                        .when()
                        .post(
                                "/api/accounts/user/{userId}/deposit"
                        )
                        .then()
                        .log().ifValidationFails()
                        .statusCode(201)
                        .body("id", notNullValue())
                        .extract()
                        .response();

        Long activityId =
                response.jsonPath().getLong("id");

        assertNotNull(activityId);

        return activityId;
    }

    protected String generateUniqueAlias() {

        long value =
                UNIQUE_SEQUENCE.incrementAndGet();

        return "alias."
                + value
                + ".prueba";
    }
}