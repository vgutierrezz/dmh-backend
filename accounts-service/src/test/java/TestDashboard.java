import io.restassured.http.ContentType;
import io.restassured.response.Response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestDashboard extends BaseIntegrationTest {

    @DisplayName(
            "CP-DASH-001 - Consultar actividad de una cuenta con movimientos"
    )
    @Order(1)
    @Test
    void shouldReturnActivitiesSuccessfully() {

        TestUser user = createAndAuthenticateUser();

        createDeposit(
                user,
                "100.00",
                "Depósito para consultar actividad"
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
                .body("$", hasSize(greaterThan(0)));
    }


    @DisplayName("CP-DASH-002 - Consultar saldo sin token")
    @Order(2)
    @Test
    void shouldRejectAccessWithoutToken() {

        TestUser user = createAndAuthenticateUser();

        given()
                .pathParam("userId", user.id())
                .when()
                .get("/api/accounts/user/{userId}")
                .then()
                .log().ifValidationFails()
                .statusCode(403);
    }


    @DisplayName("CP-DASH-003 - Consultar cuenta inexistente")
    @Order(3)
    @Test
    void shouldReturn404WhenUserAccountDoesNotExist() {

        /*
         * El usuario se crea para obtener un token válido.
         * El ID consultado no corresponde a una cuenta existente.
         */
        TestUser authenticatedUser =
                createAndAuthenticateUser();

        long nonexistentUserId = 999999999L;

        given()
                .header(
                        "Authorization",
                        bearerToken(
                                authenticatedUser.token()
                        )
                )
                .pathParam(
                        "userId",
                        nonexistentUserId
                )
                .when()
                .get("/api/accounts/user/{userId}")
                .then()
                .log().ifValidationFails()
                .statusCode(404);
    }

    @DisplayName("CP-DASH-004 - Consultar movimientos")
    @Order(4)
    @Test
    void shouldGetAccountActivitySuccessfully() {

        TestUser user = createAndAuthenticateUser();

        createDeposit(
                user,
                "150.00",
                "Depósito para consultar movimientos"
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
                .body("$", hasSize(greaterThan(0)));
    }

    @DisplayName("CP-DASH-005 - Cuenta existente sin actividades")
    @Order(5)
    @Test
    void shouldReturnEmptyListWhenAccountHasNoActivities() {

        /*
         * Se crea el usuario y su cuenta, pero no se realizan
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
                .get("/api/accounts/user/{userId}/activity")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasSize(0));
    }


    @DisplayName(
            "CP-DASH-006 - Movimientos ordenados del más reciente al más antiguo"
    )
    @Order(6)
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
                        .pathParam(
                                "userId",
                                user.id()
                        )
                        .when()
                        .get(
                                "/api/accounts/user/{userId}/activity"
                        );

        response.then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasSize(3));

        List<String> dates =
                response.jsonPath()
                        .getList(
                                "dated",
                                String.class
                        );

        assertNotNull(
                dates,
                "La respuesta debe incluir las fechas de las actividades"
        );

        List<String> sortedDates =
                new ArrayList<>(dates);

        sortedDates.sort(
                Comparator.reverseOrder()
        );

        assertEquals(
                sortedDates,
                dates,
                "Las actividades deben estar ordenadas "
                        + "de la más reciente a la más antigua"
        );
    }
}