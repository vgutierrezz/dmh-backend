import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class TestActivity {

    private static final String BASE_URI = "http://localhost:8080";

    private static final String EMAIL = "valentina@test.com";

    private static final String PASSWORD = "Valen1234";

    private static final Long USER_ID = 11L;

    private static final Long NON_EXISTING_USER_ID = 999999L;

    private static final Long NON_EXISTING_ACTIVITY_ID = 999999L;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = BASE_URI;
    }

    @DisplayName("CP-ACT-001 - Consultar historial de actividades")
    @Order(1)
    @Test
    void shouldGetAccountActivitySuccessfully() {

        String token = loginAndGetToken();

        given()
                .header("Authorization", "Bearer " + token)
                .log().ifValidationFails()
                .when()
                .get(
                        "/api/accounts/user/"
                                + USER_ID
                                + "/activity"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("$", notNullValue())
                .body("size()", greaterThan(0));
    }

    @DisplayName("CP-ACT-002 - Verificar estructura de las actividades")
    @Order(2)
    @Test
    void shouldReturnExpectedActivityStructure() {

        String token = loginAndGetToken();

        List<Map<String, Object>> activities =
                given()
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
                        .when()
                        .get(
                                "/api/accounts/user/"
                                        + USER_ID
                                        + "/activity"
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

    @DisplayName("CP-ACT-003 - Verificar actividades ordenadas por fecha descendente")
    @Order(3)
    @Test
    void shouldReturnActivitiesOrderedByDateDesc() {

        String token = loginAndGetToken();

        Response response =
                given()
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
                        .log().ifValidationFails()
                        .when()
                        .get(
                                "/api/accounts/user/"
                                        + USER_ID
                                        + "/activity"
                        );

        response.then()
                .log().ifValidationFails()
                .statusCode(200);

        List<String> dateValues =
                response.jsonPath()
                        .getList("dated", String.class);

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

    /*
     * Este caso requiere una cuenta existente sin movimientos.
     * Sustituye el ID cuando tengas disponible ese dato de prueba.
     *
     * Si todavía no existe una cuenta sin actividades,
     * mantenlo deshabilitado o marcado como BLOCKED en la planilla.
     */


    @DisplayName("CP-ACT-004 - Cuenta sin actividades")
    @Order(4)
    @Test
    void shouldReturnEmptyListWhenAccountHasNoActivities() {

        String token = loginAndGetToken();

        Long userWithoutActivityId = 38L;

        given()
                .header("Authorization", "Bearer " + token)
        .when()
                .get(
                        "/api/accounts/user/"
                                + userWithoutActivityId
                                + "/activity"
                )
        .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("$", empty());
    }


    @DisplayName("CP-ACT-005 - Consultar historial de cuenta inexistente")
    @Order(5)
    @Test
    void shouldReturn400WhenUserAccountDoesNotExist() {

        String token = loginAndGetToken();

        given()
                .header("Authorization", "Bearer " + token)
                .log().ifValidationFails()
                .when()
                .get(
                        "/api/accounts/user/"
                                + NON_EXISTING_USER_ID
                                + "/activity"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

    @DisplayName("CP-ACT-006 - Consultar historial sin token")
    @Order(6)
    @Test
    void shouldRejectActivityHistoryWithoutToken() {

        given()
                .log().ifValidationFails()
                .when()
                .get(
                        "/api/accounts/user/"
                                + USER_ID
                                + "/activity"
                )
                .then()
                .log().ifValidationFails()
                .statusCode(anyOf(
                        is(401),
                        is(403)
                ));
    }

    @DisplayName("CP-ACT-007 - Consultar detalle de actividad")
    @Order(7)
    @Test
    void shouldGetActivityDetailSuccessfully() {

        String token = loginAndGetToken();
        Long activityId = getExistingActivityId(token);

        given()
                .header("Authorization", "Bearer " + token)
                .log().ifValidationFails()
                .when()
                .get(
                        "/api/accounts/activity/"
                                + activityId
                )
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("id", is(activityId.intValue()))
                .body("amount", notNullValue())
                .body("name", notNullValue())
                .body("dated", notNullValue())
                .body("type", notNullValue())
                .body("origin", notNullValue())
                .body("destination", notNullValue());
    }

    @DisplayName("CP-ACT-008 - Verificar estructura del detalle de actividad")
    @Order(8)
    @Test
    void shouldReturnExpectedActivityDetailStructure() {

        String token = loginAndGetToken();
        Long activityId = getExistingActivityId(token);

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get(
                        "/api/accounts/activity/"
                                + activityId
                )
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("$", hasKey("id"))
                .body("$", hasKey("amount"))
                .body("$", hasKey("name"))
                .body("$", hasKey("dated"))
                .body("$", hasKey("type"))
                .body("$", hasKey("origin"))
                .body("$", hasKey("destination"));
    }

    @DisplayName("CP-ACT-009 - Consultar actividad inexistente")
    @Order(9)
    @Test
    void shouldReturn404WhenActivityDoesNotExist() {

        String token = loginAndGetToken();

        given()
                .header("Authorization", "Bearer " + token)
                .log().ifValidationFails()
                .when()
                .get(
                        "/api/accounts/activity/"
                                + NON_EXISTING_ACTIVITY_ID
                )
                .then()
                .log().ifValidationFails()
                .statusCode(404);
    }

    @DisplayName("CP-ACT-010 - Consultar detalle sin token")
    @Order(10)
    @Test
    void shouldRejectActivityDetailWithoutToken() {

        String preparationToken = loginAndGetToken();
        Long activityId = getExistingActivityId(preparationToken);

        given()
                .log().ifValidationFails()
                .when()
                .get("/api/accounts/activity/" + activityId)
                .then()
                .log().ifValidationFails()
                .statusCode(anyOf(
                        is(401),
                        is(403)
                ));
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

    private Long getExistingActivityId(String token) {

        List<Integer> activityIds =
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
                        .extract()
                        .jsonPath()
                        .getList("id", Integer.class);

        assertNotNull(
                activityIds,
                "La lista de IDs no debe ser null"
        );

        assertFalse(
                activityIds.isEmpty(),
                "Debe existir al menos una actividad"
        );

        return activityIds.get(0).longValue();
    }
}