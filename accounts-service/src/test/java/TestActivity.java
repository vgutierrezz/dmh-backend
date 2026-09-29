import io.restassured.http.ContentType;
import io.restassured.response.Response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(OrderAnnotation.class)
public class TestActivity extends BaseIntegrationTest {

    private static final long NON_EXISTING_USER_ID =
            999999999L;

    private static final long NON_EXISTING_ACTIVITY_ID =
            999999999L;

    // ============================================================
    // CP-ACT-001
    // ============================================================

    @DisplayName("CP-ACT-001 - Consultar historial de actividades")
    @Order(1)
    @Test
    void shouldGetAccountActivitySuccessfully() {

        TestUser user = createAndAuthenticateUser();

        createDeposit(
                user,
                "100.00",
                "Depósito para historial"
        );

        given()
                .header(
                        "Authorization",
                        bearerToken(user.token())
                )
                .pathParam("userId", user.id())
                .when()
                .get("/api/accounts/user/{userId}/activity")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", notNullValue())
                .body("size()", greaterThan(0));
    }

    // ============================================================
    // CP-ACT-002
    // ============================================================

    @DisplayName("CP-ACT-002 - Verificar estructura de las actividades")
    @Order(2)
    @Test
    void shouldReturnExpectedActivityStructure() {

        TestUser user = createAndAuthenticateUser();

        createDeposit(
                user,
                "150.00",
                "Depósito para validar estructura"
        );

        List<Map<String, Object>> activities =
                given()
                        .header(
                                "Authorization",
                                bearerToken(user.token())
                        )
                        .pathParam("userId", user.id())
                        .when()
                        .get(
                                "/api/accounts/user/{userId}/activity"
                        )
                        .then()
                        .log().ifValidationFails()
                        .statusCode(200)
                        .extract()
                        .jsonPath()
                        .getList("$");

        assertNotNull(
                activities,
                "La lista de actividades no debe ser null"
        );

        assertFalse(
                activities.isEmpty(),
                "El usuario debe tener actividades para validar el contrato"
        );

        for (Map<String, Object> activity : activities) {

            assertNotNull(
                    activity.get("id"),
                    "Cada actividad debe incluir id"
            );

            assertNotNull(
                    activity.get("amount"),
                    "Cada actividad debe incluir amount"
            );

            assertNotNull(
                    activity.get("name"),
                    "Cada actividad debe incluir name"
            );

            assertNotNull(
                    activity.get("dated"),
                    "Cada actividad debe incluir dated"
            );

            assertNotNull(
                    activity.get("type"),
                    "Cada actividad debe incluir type"
            );

            assertNotNull(
                    activity.get("origin"),
                    "Cada actividad debe incluir origin"
            );

            assertNotNull(
                    activity.get("destination"),
                    "Cada actividad debe incluir destination"
            );
        }
    }

    // ============================================================
    // CP-ACT-003
    // ============================================================

    @DisplayName(
            "CP-ACT-003 - Verificar actividades ordenadas por fecha descendente"
    )
    @Order(3)
    @Test
    void shouldReturnActivitiesOrderedByDateDesc() {

        TestUser user = createAndAuthenticateUser();

        createDeposit(
                user,
                "100.00",
                "Primer depósito"
        );

        sleep(100);

        createDeposit(
                user,
                "200.00",
                "Segundo depósito"
        );

        sleep(100);

        createDeposit(
                user,
                "300.00",
                "Tercer depósito"
        );

        Response response =
                given()
                        .header(
                                "Authorization",
                                bearerToken(user.token())
                        )
                        .pathParam("userId", user.id())
                        .when()
                        .get(
                                "/api/accounts/user/{userId}/activity"
                        );

        response.then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(3));

        List<String> dateValues =
                response.jsonPath()
                        .getList(
                                "dated",
                                String.class
                        );

        assertNotNull(
                dateValues,
                "La respuesta debe incluir fechas"
        );

        assertFalse(
                dateValues.isEmpty(),
                "Debe existir al menos una actividad"
        );

        List<LocalDateTime> actualDates =
                dateValues.stream()
                        .map(LocalDateTime::parse)
                        .toList();

        List<LocalDateTime> expectedDates =
                new ArrayList<>(actualDates);

        expectedDates.sort(
                Comparator.reverseOrder()
        );

        assertEquals(
                expectedDates,
                actualDates,
                "Las actividades deben estar ordenadas "
                        + "de la más reciente a la más antigua"
        );
    }

    // ============================================================
    // CP-ACT-004
    // ============================================================

    @DisplayName("CP-ACT-004 - Cuenta sin actividades")
    @Order(4)
    @Test
    void shouldReturnEmptyListWhenAccountHasNoActivities() {

        /*
         * Se crea el usuario y la cuenta, pero no se generan
         * depósitos ni transferencias.
         */
        TestUser user = createAndAuthenticateUser();

        given()
                .header(
                        "Authorization",
                        bearerToken(user.token())
                )
                .pathParam("userId", user.id())
                .when()
                .get(
                        "/api/accounts/user/{userId}/activity"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", empty());
    }

    // ============================================================
    // CP-ACT-005
    // ============================================================

    @DisplayName("CP-ACT-005 - Consultar historial de cuenta inexistente")
    @Order(5)
    @Test
    void shouldReturn400WhenUserAccountDoesNotExist() {

        /*
         * Se crea un usuario para obtener un token válido,
         * pero se consulta un ID que no tiene cuenta.
         */
        TestUser authenticatedUser =
                createAndAuthenticateUser();

        given()
                .header(
                        "Authorization",
                        bearerToken(
                                authenticatedUser.token()
                        )
                )
                .pathParam(
                        "userId",
                        NON_EXISTING_USER_ID
                )
                .when()
                .get(
                        "/api/accounts/user/{userId}/activity"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

    // ============================================================
    // CP-ACT-006
    // ============================================================

    @DisplayName("CP-ACT-006 - Consultar historial sin token")
    @Order(6)
    @Test
    void shouldRejectActivityHistoryWithoutToken() {

        /*
         * La cuenta debe existir para que el único error
         * evaluado sea la ausencia del token.
         */
        TestUser user = createAndAuthenticateUser();

        given()
                .pathParam("userId", user.id())
                .when()
                .get(
                        "/api/accounts/user/{userId}/activity"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(403);
    }

    // ============================================================
    // CP-ACT-007
    // ============================================================

    @DisplayName("CP-ACT-007 - Consultar detalle de actividad")
    @Order(7)
    @Test
    void shouldGetActivityDetailSuccessfully() {

        TestUser user = createAndAuthenticateUser();

        Long activityId =
                createDeposit(
                        user,
                        "250.00",
                        "Depósito para consultar detalle"
                );

        given()
                .header(
                        "Authorization",
                        bearerToken(user.token())
                )
                .pathParam(
                        "activityId",
                        activityId
                )
                .when()
                .get(
                        "/api/accounts/activity/{activityId}"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(
                        "id",
                        is(activityId.intValue())
                )
                .body("amount", notNullValue())
                .body("name", notNullValue())
                .body("dated", notNullValue())
                .body("type", notNullValue())
                .body("origin", notNullValue())
                .body("destination", notNullValue());
    }

    // ============================================================
    // CP-ACT-008
    // ============================================================

    @DisplayName(
            "CP-ACT-008 - Verificar estructura del detalle de actividad"
    )
    @Order(8)
    @Test
    void shouldReturnExpectedActivityDetailStructure() {

        TestUser user = createAndAuthenticateUser();

        Long activityId =
                createDeposit(
                        user,
                        "175.00",
                        "Depósito para validar estructura"
                );

        given()
                .header(
                        "Authorization",
                        bearerToken(user.token())
                )
                .pathParam(
                        "activityId",
                        activityId
                )
                .when()
                .get(
                        "/api/accounts/activity/{activityId}"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasKey("id"))
                .body("$", hasKey("amount"))
                .body("$", hasKey("name"))
                .body("$", hasKey("dated"))
                .body("$", hasKey("type"))
                .body("$", hasKey("origin"))
                .body("$", hasKey("destination"));
    }

    // ============================================================
    // CP-ACT-009
    // ============================================================

    @DisplayName("CP-ACT-009 - Consultar actividad inexistente")
    @Order(9)
    @Test
    void shouldReturn404WhenActivityDoesNotExist() {

        TestUser authenticatedUser =
                createAndAuthenticateUser();

        given()
                .header(
                        "Authorization",
                        bearerToken(
                                authenticatedUser.token()
                        )
                )
                .pathParam(
                        "activityId",
                        NON_EXISTING_ACTIVITY_ID
                )
                .when()
                .get(
                        "/api/accounts/activity/{activityId}"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(404);
    }

    // ============================================================
    // CP-ACT-010
    // ============================================================

    @DisplayName("CP-ACT-010 - Consultar detalle sin token")
    @Order(10)
    @Test
    void shouldRejectActivityDetailWithoutToken() {

        /*
         * Primero se genera una actividad válida usando el token.
         * Después se consulta su detalle sin enviar autenticación.
         */
        TestUser user = createAndAuthenticateUser();

        Long activityId =
                createDeposit(
                        user,
                        "125.00",
                        "Depósito para validar seguridad"
                );

        given()
                .pathParam(
                        "activityId",
                        activityId
                )
                .when()
                .get(
                        "/api/accounts/activity/{activityId}"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(403);
    }
}