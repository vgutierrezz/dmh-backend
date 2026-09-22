import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestDashboard {

    private static final String BASE_URI = "http://localhost:8080";

    private static final String EMAIL = "valentina@test.com";
    private static final String PASSWORD = "Valen1234";

    private static final Long USER_ID = 11L;
    private static final Long USUARIO_SIN_MOVIMIENTOS_ID = 95L;
    @BeforeEach
    void setUp() {
        RestAssured.baseURI = BASE_URI;
    }

    @DisplayName("CP-DASH-001 - Consultar saldo disponible")
    @Test
    void shouldGetAccountBalanceSuccessfully() {

        String token = loginAndGetToken();

        Long userId = 11L;

        given()
                .header("Authorization", "Bearer " + token)
                .log().all()
                .when()
                .get("/api/accounts/user/" + userId)
                .then()
                .log().all()
                .statusCode(200)
                .body("id", notNullValue())
                .body("userId", equalTo(userId.toString()))
                .body("balance", notNullValue())
                .body("cvu", notNullValue())
                .body("alias", notNullValue());
    }

    @DisplayName("CP-DASH-002 - Consultar saldo sin token")
    @Test
    void shouldRejectAccessWithoutToken() {

        Long userId = 11L;

        given()
                .log().all()
                .when()
                .get("/api/accounts/user/" + userId)
                .then()
                .log().all()
                .statusCode(403);
    }

    @DisplayName("CP-DASH-003 - Consultar cuenta inexistente")
    @Test
    void shouldReturn404WhenUserAccountDoesNotExist() {

        String token = loginAndGetToken();

        Long userId = 999999L;

        given()
                .header("Authorization", "Bearer " + token)
                .log().all()
                .when()
                .get("/api/accounts/user/" + userId)
                .then()
                .log().all()
                .statusCode(404);
    }

    @DisplayName("CP-DASH-004 - Consultar movimientos")
    @Test
    void shouldGetAccountActivitySuccessfully() {

        String token = loginAndGetToken();
        Long userId = 11L;

        given()
                .header("Authorization", "Bearer " + token)
                .log().all()
                .when()
                .get("/api/accounts/user/" + userId + "/activity")
                .then()
                .log().all()
                .statusCode(200)
                .body("$", notNullValue());
    }

    @DisplayName("CP-DASH-005 - Cuenta sin movimientos")
    @Test
    void shouldReturnEmptyListWhenAccountHasNoActivities() {

        String token = loginAndGetToken();
        Long userId = USUARIO_SIN_MOVIMIENTOS_ID;

        given()
                .header("Authorization", "Bearer " + token)
                .log().all()
                .when()
                .get("/api/accounts/user/" + userId + "/activity")
                .then()
                .log().all()
                .statusCode(200)
                .body("$", hasSize(0));
    }

    @DisplayName("CP-DASH-006 - Movimientos ordenados del más reciente al más antiguo")
    @Test
    void shouldReturnActivitiesOrderedByDateDesc() {

        String token = loginAndGetToken();
        Long userId = 11L;

        Response response =
                given()
                        .header("Authorization", "Bearer " + token)
                        .log().all()
                        .when()
                        .get("/api/accounts/user/" + userId + "/activity");

        response.then()
                .log().all()
                .statusCode(200);

        List<String> dates =
                response.jsonPath()
                        .getList("dated", String.class);

        assertNotNull(
                dates,
                "La respuesta debe incluir las fechas de las actividades"
        );

        List<String> sortedDates = new ArrayList<>(dates);
        sortedDates.sort(Comparator.reverseOrder());

        assertEquals(
                sortedDates,
                dates,
                "Las actividades deben estar ordenadas de la más reciente a la más antigua"
        );
    }

    private String loginAndGetToken() {

        return given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                      "email": "valentina@test.com",
                      "password": "Valen1234"
                    }
                    """)
                .log().all()
                .when()
                .post("/api/auth/login")
                .then()
                .log().all()
                .statusCode(200)
                .body("token", notNullValue())
                .extract()
                .path("token");
    }
}