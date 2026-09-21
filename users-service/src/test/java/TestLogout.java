import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class TestLogout {

    @DisplayName("CP-OUT-001 - Logout exitoso")
    @Test
    void shouldLogoutSuccessfully_whenTokenIsValid() {

        String token = loginAndGetToken();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .post("/api/auth/logout")
                .then()
                .statusCode(200)
                .body("message", equalTo("Logout exitoso"));
    }

    @DisplayName("CP-OUT-002 - Invalidar token luego del logout")
    @Test
    void shouldInvalidateToken_whenLogoutSucceeds() {

        String token = loginAndGetToken();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .post("/api/auth/logout")
                .then()
                .statusCode(200);

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .post("/api/auth/logout")
                .then()
                .statusCode(anyOf(is(400), is(401), is(403)));
    }

    @DisplayName("CP-OUT-003 - Logout sin token")
    @Test
    void shouldRejectLogout_whenTokenIsMissing() {

        given()
                .when()
                .post("/api/auth/logout")
                .then()
                .statusCode(400);
    }

    @DisplayName("CP-OUT-004 - Logout con token inválido")
    @Test
    void shouldRejectLogout_whenTokenIsInvalid() {

        given()
                .header(
                        "Authorization",
                        "Bearer token-invalido"
                )
                .when()
                .post("/api/auth/logout")
                .then()
                .statusCode(400);
    }

    @DisplayName("CP-OUT-005 - Logout repetido")
    @Test
    void shouldHandleRepeatedLogoutGracefully() {

        String token = loginAndGetToken();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .post("/api/auth/logout")
                .then()
                .statusCode(200);

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .post("/api/auth/logout")
                .then()
                .statusCode(anyOf(is(200), is(400)));
    }

    private String loginAndGetToken() {

        RestAssured.baseURI = "http://localhost:8080";

        return given()
                .contentType(ContentType.JSON)
                .body("""
                {
                  "email":"valentina@test.com",
                  "password":"Valen1234"
                }
                """)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");
    }
}
